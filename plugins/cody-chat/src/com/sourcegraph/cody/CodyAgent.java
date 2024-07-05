package com.sourcegraph.cody;

import com.google.gson.GsonBuilder;
import com.sourcegraph.cody.protocol_generated.ClientCapabilities;
import com.sourcegraph.cody.protocol_generated.ClientInfo;
import com.sourcegraph.cody.protocol_generated.CodyAgentServer;
import com.sourcegraph.cody.protocol_generated.ExtensionConfiguration;
import com.sourcegraph.cody.protocol_generated.ProtocolTypeAdapters;
import dev.dirs.ProjectDirectories;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.ui.services.IDisposable;

// @Creatable
// @Singleton
public class CodyAgent implements IDisposable {

  //	@Inject
  //	IWorkbench workbench;

  private final Future<Void> listening;
  public static final CodyAgentClientImpl CLIENT = new CodyAgentClientImpl();
  public final CodyAgentServer server;
  private final Process process;

  public CodyAgent(Future<Void> listening, CodyAgentServer server, Process process) {
    this.listening = listening;
    this.server = server;
    this.process = process;
  }

  @Nullable public static volatile CodyAgent AGENT = null;

  public static ExecutorService executorService = Executors.newCachedThreadPool();

  /**
   * Returns the path to a Node.js executable to run the agent.
   *
   * <p>Modern versions of Eclipse include a Node.js installation and this method detects those. In
   * other cases, users can provide the location through the system property code.nodejs-executable.
   * Down the road, we may consider publishing separate plugin jars for each OS (cody-win.jar,
   * cody-macos.jar, etc).
   *
   * <p>Importantly, we don't try to just shell out to "node". While this may work in some cases, we
   * have no guarantee what Node version this gives us (and the agent requires Node >=v18).
   */
  public static Path getNodeJsLocation() throws URISyntaxException {
    String userProvidedNode = System.getProperty("cody.nodejs-executable");
    if (userProvidedNode != null) {
      Path path = Paths.get(userProvidedNode);
      if (!Files.isExecutable(path)) {
        throw new IllegalArgumentException(
            "not executable: -Dcody.nodejs-executable=" + userProvidedNode);
      }

      return path;
    }
    // Get the Node.js installation directory
    Path nodeJsDir = Paths.get(Platform.getInstallLocation().getURL().toURI()).resolve(".node");
    if (Files.isDirectory(nodeJsDir)) {
      for (File subdir : nodeJsDir.toFile().listFiles()) {
        String nodeExecutable = Platform.getOS().equals(Platform.OS_WIN32) ? "node.exe" : "node";
        Path nodejsBinary = subdir.toPath().resolve(nodeExecutable);
        if (Files.isRegularFile(nodejsBinary)) {
          return nodejsBinary;
        }
      }
    }
    throw new IllegalStateException(
        "Unable to infer the location of a Node.js installation. To fix this problem, set the VM"
            + " argument -Dcody.nodejs-executable and restart Eclipse.");
  }

  public static void stop() {

    if (AGENT != null) {
      AGENT.dispose();
    }
  }

  private boolean isRunning() {
    return !listening.isDone();
  }

  @Override
  public void dispose() {
    if (!isRunning()) {
      return;
    }
    try {
      server.shutdown(null).get(1, TimeUnit.SECONDS);
      server.exit(null);
      listening.cancel(true);
    } catch (Exception e) {

      e.printStackTrace();
    } finally {
      try {
        process.destroy();
      } catch (Exception e) {

        e.printStackTrace();
      }
    }
  }

  public static CodyAgent restart() throws IOException {
    stop();
    return start();
  }

  public static CodyAgent start() {
    try {
      return startUnsafe();
    } catch (Exception e) {
      e.printStackTrace();
      throw new WrappedRuntimeException(e);
    }
  }

  private static Path agentScript() throws IOException {
    String userProvidedAgentScript = System.getProperty("cody.agent-script-path");
    if (userProvidedAgentScript != null) {
      return Paths.get(userProvidedAgentScript);
    }
    ProjectDirectories dirs =
        dev.dirs.ProjectDirectories.from("com.sourcegraph", "Sourcegraph", "CodyEclipse");
    Path dataDir = Paths.get(dirs.dataDir);
    String assets = CodyResources.loadResourceString("/resources/cody-agent/assets.txt");
    Files.createDirectories(dataDir);
    for (String asset : assets.split("\n")) {
      String path = "/resources/cody-agent/" + asset;
      try (InputStream in = CodyAgent.class.getResourceAsStream(path)) {
        if (in == null) {
          throw new IllegalStateException(
              String.format(
                  "not found: %s. To fix this problem, "
                      + "run `./scripts/build-agent.sh` and try again.",
                  path));
        }
        Files.copy(in, dataDir.resolve(asset), StandardCopyOption.REPLACE_EXISTING);
      }
    }
    return dataDir.resolve("index.js");
  }

  private static CodyAgent startUnsafe()
      throws IOException,
          InterruptedException,
          ExecutionException,
          TimeoutException,
          URISyntaxException {
    if (AGENT != null && AGENT.isRunning()) {
      return AGENT;
    }

    Path workspaceRoot = Paths.get(Platform.getInstanceLocation().getURL().toURI());
    Path nodeExecutable = getNodeJsLocation();
    ArrayList<String> arguments = new ArrayList<>();
    arguments.add(nodeExecutable.toString());
    arguments.add("--enable-source-maps");
    arguments.add(agentScript().toString());
    arguments.add("api");
    arguments.add("jsonrpc-stdio");
    ProcessBuilder processBuilder =
        new ProcessBuilder(arguments)
            .directory(workspaceRoot.toFile())
            .redirectError(ProcessBuilder.Redirect.INHERIT);
    String agentTracePath = System.getProperty("cody.debug-trace-path");

    if (agentTracePath != null) {
      processBuilder.environment().put("CODY_AGENT_TRACE_PATH", agentTracePath);
    }

    Process process = processBuilder.start();

    Launcher<CodyAgentServer> launcher =
        new Launcher.Builder<CodyAgentServer>()
            .setRemoteInterface(CodyAgentServer.class)
            .traceMessages(traceWriter())
            .setExecutorService(executorService)
            .setInput(process.getInputStream())
            .setOutput(process.getOutputStream())
            .setLocalService(CLIENT)
            .configureGson(CodyAgent::configureGson)
            .create();

    Future<Void> listening = launcher.startListening();
    CodyAgentServer server = launcher.getRemoteProxy();
    initialize(server, workspaceRoot);
    return new CodyAgent(listening, server, process);
  }

  public static void configureGson(GsonBuilder builder) {
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
        if (factory.getClass().getName().indexOf("EnumTypeAdapter") >= 0) {
          factories.remove(factory);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    ProtocolTypeAdapters.register(builder);
  }

  private static ExtensionConfiguration config;

  public static void onConfigChange(ExtensionConfiguration config) {
    CodyAgent.config = config;
    if (CodyAgent.AGENT != null && CodyAgent.AGENT.isRunning()) {
      try {
        CodyAgent.AGENT.server.extensionConfiguration_didChange(config);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private static void initialize(CodyAgentServer server, Path workspaceRoot)
      throws InterruptedException,
          ExecutionException,
          TimeoutException,
          IOException,
          URISyntaxException {
    ClientInfo clientInfo = new ClientInfo();
    clientInfo.name = "cody-eclipse";
    clientInfo.version = "0.1.1";
    clientInfo.workspaceRootUri = workspaceRoot.toUri().toString();
    ClientCapabilities capabilities = new ClientCapabilities();
    capabilities.chat = ClientCapabilities.ChatEnum.Streaming;
    // Enable string-encoding for webview messages.
    capabilities.webviewMessages = ClientCapabilities.WebviewMessagesEnum.String_encoded;
    clientInfo.capabilities = capabilities;

    clientInfo.extensionConfiguration = CodyAgent.config;
    server.initialize(clientInfo).get(20, TimeUnit.SECONDS);
    server.initialized(null);
  }

  private static PrintWriter traceWriter() {
    String tracePath = System.getProperty("cody-agent.trace-path", "");

    if (!tracePath.isEmpty()) {
      Path trace = Paths.get(tracePath);
      try {
        Files.createDirectories(trace.getParent());
        return new PrintWriter(
            Files.newOutputStream(
                trace, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING));
      } catch (IOException e) {

        e.printStackTrace();
      }
    }
    return null;
  }
}
