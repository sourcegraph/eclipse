package com.sourcegraph.cody;

import com.google.gson.GsonBuilder;
import com.sourcegraph.cody.protocol_generated.*;
import com.sourcegraph.cody.workspace.EditorState;
import com.sourcegraph.cody.workspace.WorkspaceListener;
import dev.dirs.ProjectDirectories;
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
import java.util.function.Consumer;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
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

  private static ILog log = Platform.getLog(CodyAgent.class);

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
  public static Path getNodeJsLocation() throws IOException {
    String userProvidedNode = System.getProperty("cody.nodejs-executable");
    if (userProvidedNode != null) {
      Path path = Paths.get(userProvidedNode);
      if (!Files.isExecutable(path)) {
        throw new IllegalArgumentException(
            "not executable: -Dcody.nodejs-executable=" + userProvidedNode);
      }

      return path;
    }

    // We only support Windows at this time. The binary for Node.js is included in the plugin JAR.
    if (Platform.getOS().equals(Platform.OS_WIN32)) {
      String nodeExecutableName = "node-win-x64.exe";
      Path path = getDataDirectory().resolve(nodeExecutableName);
      if (!Files.isRegularFile(path)) {
        copyResourcePath("/resources/node-binaries/" + nodeExecutableName, path);
      }
      return path;
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
      log.error("Cannot shut down the server", e);
    } finally {
      try {
        process.destroy();
      } catch (Exception e) {
        log.error("Cannot shut down the server process", e);
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
      log.error("Cannot start Cody agent", e);
      throw new WrappedRuntimeException(e);
    }
  }

  private static Path agentScript() throws IOException {
    String userProvidedAgentScript = System.getProperty("cody.agent-script-path");
    if (userProvidedAgentScript != null) {
      return Paths.get(userProvidedAgentScript);
    }
    Path dataDir = getDataDirectory();
    String assets = CodyResources.loadResourceString("/resources/cody-agent/assets.txt");
    Files.createDirectories(dataDir);
    for (String asset : assets.split("\n")) {
      copyResourcePath("/resources/cody-agent/" + asset, dataDir.resolve(asset));
    }
    return dataDir.resolve("index.js");
  }

  private static Path getDataDirectory() {
    ProjectDirectories dirs =
        ProjectDirectories.from("com.sourcegraph", "Sourcegraph", "CodyEclipse");
    return Paths.get(dirs.dataDir);
  }

  private static void copyResourcePath(String path, Path target) throws IOException {
    try (InputStream in = CodyAgent.class.getResourceAsStream(path)) {
      if (in == null) {
        throw new IllegalStateException(
            String.format(
                "not found: %s. To fix this problem, "
                    + "run `./scripts/build-agent.sh` and try again.",
                path));
      }
      Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
    }
  }

  private static Path getWorkspaceRoot() throws URISyntaxException {
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

  private static CodyAgent startUnsafe()
      throws IOException,
          InterruptedException,
          ExecutionException,
          TimeoutException,
          URISyntaxException {
    if (AGENT != null && AGENT.isRunning()) {
      return AGENT;
    }

    Path workspaceRoot = getWorkspaceRoot();

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

    var instance = new CodyAgent(listening, server, process);
    instance.discoverWorkbenchState();

    AGENT = instance;
    return instance;
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
      log.error("Problems configuring Gson", e);
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
        log.error("Cannot notify about config change", e);
      }
    }
  }

  private static void initialize(CodyAgentServer server, Path workspaceRoot)
      throws InterruptedException, ExecutionException, TimeoutException {
    ClientInfo clientInfo = new ClientInfo();
    // See
    // https://sourcegraph.com/github.com/sourcegraph/cody/-/blob/agent/src/cli/codyCliClientName.ts
    // for a detailed explanation why we use the name "jetbrains" instead of "eclipse". The short
    // explanation is that
    // we need to wait for enterprise customers to upgrade to a new version that includes the fix
    // from this PR here https://github.com/sourcegraph/sourcegraph/pull/63855.
    clientInfo.name = "jetbrains";
    clientInfo.version = "5.5.20-eclipse"; // Needs to be greater than 5.5.8
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

  private void discoverWorkbenchState() {
    // This can cause UI to hang for a moment after the start of the agent. If that's a problem,
    // split this function into two parts: collecting the state and then notifying the agent. Run
    // the first part with Display.syncExec.
    Display.getDefault()
        .asyncExec(
            () -> {
              var editors =
                  PlatformUI.getWorkbench()
                      .getActiveWorkbenchWindow()
                      .getActivePage()
                      .getEditorReferences();

              for (var editor : editors) {
                var editorState = EditorState.from(editor);
                if (editorState != null) {
                  fileOpened(editorState);
                }
              }

              var activeEditor =
                  PlatformUI.getWorkbench()
                      .getActiveWorkbenchWindow()
                      .getActivePage()
                      .getActivePartReference();

              var activeEditorState = EditorState.from(activeEditor);
              if (activeEditorState != null) {
                focusChanged(activeEditorState);

                // TODO: Refactor this.
                WorkspaceListener.setupSelectionListener(activeEditorState);
                WorkspaceListener.setupContentListener(activeEditorState);
              }
            });
  }

  ////////////////////
  // NOTIFICATIONS //
  ///////////////////

  public void focusChanged(EditorState state) {
    var params = new TextDocument_DidFocusParams();
    params.uri = state.uri;
    server.textDocument_didFocus(params);
  }

  public void fileOpened(EditorState state) {
    var params = new ProtocolTextDocument();
    params.uri = state.uri;
    params.content = state.readContents();
    server.textDocument_didOpen(params);
  }

  public void selectionChanged(EditorState state, Range range) {
    var params = new ProtocolTextDocument();
    params.uri = state.uri;
    params.selection = range;
    server.textDocument_didChange(params);
  }

  public void fileChanged(EditorState state) {
    var params = new ProtocolTextDocument();
    params.uri = state.uri;
    params.content = state.readContents();
    server.textDocument_didChange(params);
  }

  public static void withAgent(Consumer<CodyAgent> callback) {
    var agent = CodyAgent.AGENT;
    if (agent != null && agent.isRunning()) {
      callback.accept(agent);
    }
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
        log.error("Cannot create a trace", e);
      }
    }
    return null;
  }
}
