package com.sourcegraph.cody.chat.agent;

import com.google.gson.GsonBuilder;
import com.sourcegraph.cody.CodyPaths;
import com.sourcegraph.cody.CodyResources;
import com.sourcegraph.cody.chat.access.TokenStorage;
import com.sourcegraph.cody.logging.CodyLogger;
import com.sourcegraph.cody.protocol_generated.ClientCapabilities;
import com.sourcegraph.cody.protocol_generated.ClientInfo;
import com.sourcegraph.cody.protocol_generated.CodyAgentServer;
import com.sourcegraph.cody.protocol_generated.ProtocolTypeAdapters;
import com.sourcegraph.cody.protocol_generated.WebviewNativeConfigParams;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.lsp4j.jsonrpc.Launcher;

public class StartAgentJob extends Job {
  private final CodyManager manager;
  private final CompletableFuture<CodyAgent> agent;
  private final CompletableFuture<Integer> webserverPort;
  private final TokenStorage tokenStorage;
  private final MultiConsumer<String> webviewConsumer;

  private final CodyLogger log = new CodyLogger(getClass());

  public StartAgentJob(
      CodyManager manager,
      CompletableFuture<CodyAgent> agent,
      CompletableFuture<Integer> webserverPort,
      TokenStorage tokenStorage,
      MultiConsumer<String> webviewConsumer) {
    super("Starting Cody...");
    this.manager = manager;
    this.agent = agent;
    this.webserverPort = webserverPort;
    this.tokenStorage = tokenStorage;
    this.webviewConsumer = webviewConsumer;
  }

  @Override
  protected IStatus run(IProgressMonitor monitor) {
    try {
      int port = webserverPort.get(5, TimeUnit.SECONDS);
      agent.complete(startUnsafe(port));
    } catch (Exception e) {
      log.error("Cannot start agent", e);
      agent.completeExceptionally(e);
      manager.agentDisposed();
      return Status.CANCEL_STATUS;
    }

    return Status.OK_STATUS;
  }

  private CodyAgent startUnsafe(int port)
      throws URISyntaxException,
          IOException,
          ExecutionException,
          InterruptedException,
          TimeoutException {

    Path workspaceRoot = getWorkspaceRoot();
    var dataDir = CodyPaths.dataDir();
    // Copy all necessary resources to data directory. By default this will be
    // ~/AppData/Roaming/Sourcegraph/CodyEclipse/data on Windows.
    manager.resources =
        new CodyResources(
            new CodyResources.DestinationsBuilder()
                .withAgent(dataDir)
                .withWebviews(CodyPaths.codyDir())
                .withNode(dataDir)
                .build());

    ArrayList<String> arguments = new ArrayList<>();
    arguments.add(manager.resources.getNodeJSLocation().toString());
    arguments.add("--enable-source-maps");
    arguments.add(CodyPaths.agentScript(manager.resources).toString());
    arguments.add("api");
    arguments.add("jsonrpc-stdio");
    ProcessBuilder processBuilder =
        new ProcessBuilder(arguments)
            .directory(workspaceRoot.toFile())
            .redirectError(ProcessBuilder.Redirect.INHERIT);

    processBuilder.environment().put("CODY_AGENT_TRACE_PATH", CodyPaths.serverTracePath());

    CodyAgentClientImpl client = new CodyAgentClientImpl(tokenStorage, webviewConsumer);
    Process process = processBuilder.start();

    Launcher<CodyAgentServer> launcher =
        new Launcher.Builder<CodyAgentServer>()
            .setRemoteInterface(CodyAgentServer.class)
            .traceMessages(traceWriter())
            .setExecutorService(manager.executorService)
            .setInput(process.getInputStream())
            .setOutput(process.getOutputStream())
            .setLocalService(client)
            .configureGson(this::configureGson)
            .create();

    Future<Void> listening = launcher.startListening();
    CodyAgentServer server = launcher.getRemoteProxy();
    initialize(server, workspaceRoot);

    var instance = new CodyAgent(listening, server, port, client, process, manager);
    instance.postCreate();
    return instance;
  }

  private void initialize(CodyAgentServer server, Path workspaceRoot)
      throws InterruptedException, ExecutionException, TimeoutException {
    ClientInfo clientInfo = new ClientInfo();
    // See
    // https://sourcegraph.com/github.com/sourcegraph/cody/-/blob/agent/src/cli/codyCliClientName.ts
    // for a detailed explanation why we use the name "jetbrains" instead of "eclipse". The short
    // explanation is that
    // we need to wait for enterprise customers to upgrade to a new version that includes the fix
    // from this PR here https://github.com/sourcegraph/sourcegraph/pull/63855.
    clientInfo.name = "eclipse";
    clientInfo.legacyNameForServerIdentification = "jetbrains";
    clientInfo.version = "5.5.21-eclipse"; // Needs to be greater than 5.5.8
    clientInfo.workspaceRootUri = workspaceRoot.toUri().toString();
    ClientCapabilities capabilities = new ClientCapabilities();
    capabilities.secrets = ClientCapabilities.SecretsEnum.Client_managed;
    capabilities.chat = ClientCapabilities.ChatEnum.Streaming;
    capabilities.showDocument = ClientCapabilities.ShowDocumentEnum.Enabled;
    // Enable string-encoding for webview messages.
    capabilities.webviewMessages = ClientCapabilities.WebviewMessagesEnum.String_encoded;
    capabilities.webview = ClientCapabilities.WebviewEnum.Native;
    WebviewNativeConfigParams webviewConfig = new WebviewNativeConfigParams();
    webviewConfig.cspSource = "'self'";
    webviewConfig.view = WebviewNativeConfigParams.ViewEnum.Single;
    webviewConfig.webviewBundleServingPrefix = CodyPaths.codyDir().resolve("dist").toUri().toString();
    webviewConfig.injectScript = CodyResources.loadInjectedJS();
    webviewConfig.injectStyle = CodyResources.loadInjectedCSS();
    capabilities.webviewNativeConfig = webviewConfig;
    capabilities.globalState = ClientCapabilities.GlobalStateEnum.Server_managed;
    clientInfo.capabilities = capabilities;

    var serverInfo = server.initialize(clientInfo).get(20, TimeUnit.SECONDS);
    CodyLogger.onEndpointChange(serverInfo.authStatus.endpoint);
    server.initialized(null);
  }

  private Path getWorkspaceRoot() throws URISyntaxException {
    // Pick the first open project as the workspace root.
    for (var project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
      if (project.isOpen()) {
        return project.getLocation().toFile().toPath();
      }
    }

    // This path is 100% wrong. The main problem with workspace root in Eclipse is that
    // it's common to have multiple projects with different workspace roots. The agent server
    // doesn't support multi-root workspaces at this time.
    return Paths.get(Platform.getInstanceLocation().getURL().toURI());
  }

  private void configureGson(GsonBuilder builder) {
    // LSP4J registers a type adapter that emits numbers for all enums, ignoring
    // `@SerializedName` annotations.
    // https://sourcegraph.com/github.com/eclipse-lsp4j/lsp4j/-/blob/org.eclipse.lsp4j.jsonrpc/src/main/java/org/eclipse/lsp4j/jsonrpc/json/adapters/EnumTypeAdapter.java?L30:14-30:29
    // See CODY-2427 for follow-up issue to remove this ugly runtime reflection hack.
    try {
      // HACK: lsp4j adds customizations to the gson builder that break serialization
      // of enums. We can work around this issue by removing these customizations.
      var factoryField = builder.getClass().getDeclaredField("factories");
      factoryField.setAccessible(true);
      @SuppressWarnings("unchecked")
      var factories = (ArrayList<Object>) factoryField.get(builder);
      for (Object factory : factories) {
        // Using `getClas().getName()` because `instanceof`
        // didn't work on the first try.
        if (factory.getClass().getName().contains("EnumTypeAdapter")) {
          factories.remove(factory);
        }
      }
    } catch (Exception e) {
      log.error("Problems configuring Gson", e);
    }

    ProtocolTypeAdapters.register(builder);
  }

  private PrintWriter traceWriter() {
    Path trace = Paths.get(CodyPaths.clientTracePath());
    try {
      Files.createDirectories(trace.getParent());
      return new PrintWriter(
          Files.newOutputStream(
              trace, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING));
    } catch (IOException e) {
      log.error("Cannot create a trace", e);
      return null;
    }
  }
}
