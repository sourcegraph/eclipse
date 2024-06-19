package com.sourcegraph.cody;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

import com.google.gson.GsonBuilder;
import com.sourcegraph.cody.protocol_generated.ClientCapabilities;
import com.sourcegraph.cody.protocol_generated.ClientInfo;
import com.sourcegraph.cody.protocol_generated.CodyAgentServer;
import com.sourcegraph.cody.protocol_generated.ExtensionConfiguration;
import com.sourcegraph.cody.protocol_generated.ProtocolTypeAdapters;

import dev.dirs.ProjectDirectories;

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

  private static ExecutorService executorService = Executors.newCachedThreadPool();

  public static void stop() {
    System.out.println("Cody is stopping");
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
      System.out.println("server.shutdown():");
      e.printStackTrace();
    } finally {
      try {
        process.destroy();
      } catch (Exception e) {
        System.out.println("Process.destroy():");
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
      throw new RuntimeException(e);
    }
  }

  private static Path agentScript() throws IOException {
    ProjectDirectories dirs =
        dev.dirs.ProjectDirectories.from("com.sourcegraph", "Sourcegraph", "Cody Eclipse");
    System.out.println("DIRS " + dirs);
    Path out = Paths.get(dirs.dataDir).resolve("index.js");
    try (InputStream in = CodyAgent.class.getResourceAsStream("/resources/agent/index.js")) {
      Files.copy(in, out);
    }
    return out;
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
    System.out.println("Cody is starting22");
    Path workspaceRoot = Paths.get(Platform.getInstanceLocation().getURL().toURI());
    // TODO: temp directory
    Path agentPath =
        Paths.get(System.getProperty("user.home"))
            .resolve("dev")
            .resolve("sourcegraph")
            .resolve("cody")
            .resolve("agent")
            .resolve("dist")
            .resolve("index.js");
    ArrayList<String> arguments = new ArrayList<>();
    arguments.add("node");
    arguments.add("--enable-source-maps");
    arguments.add(agentPath.toString());
    ProcessBuilder processBuilder =
        new ProcessBuilder(arguments)
            .directory(workspaceRoot.toFile())
            .redirectError(ProcessBuilder.Redirect.INHERIT);
    processBuilder
        .environment()
        .put(
            "CODY_AGENT_TRACE_PATH",
            "C:\\Users\\olafu\\dev\\sourcegraph\\cody\\agent\\dist\\trace.json");
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
      // TODO: send config change notification
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
    clientInfo.version = "0.1.0";
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
    System.out.println("tracepath " + tracePath);
    if (!tracePath.isEmpty()) {
      Path trace = Paths.get(tracePath);
      try {
        Files.createDirectories(trace.getParent());
        return new PrintWriter(
            Files.newOutputStream(
                trace, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING));
      } catch (IOException e) {
        System.out.println("unable to trace JSON-RPC debugging information to path " + tracePath);
        e.printStackTrace();
      }
    }
    return null;
  }
}
