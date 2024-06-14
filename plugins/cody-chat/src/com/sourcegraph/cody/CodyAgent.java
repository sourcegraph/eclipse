package com.sourcegraph.cody;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.sourcegraph.cody.protocol_generated.ClientCapabilities;
import com.sourcegraph.cody.protocol_generated.ClientInfo;
import com.sourcegraph.cody.protocol_generated.CodyAgentServer;
import com.sourcegraph.cody.protocol_generated.ExtensionConfiguration;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.ui.services.IDisposable;

public class CodyAgent implements IDisposable {

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

  private static CodyAgent startUnsafe()
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    if (AGENT != null && AGENT.isRunning()) {
      return AGENT;
    }
    System.out.println("Cody is starting");
    String workingDirectory = System.getProperty("user.dir");
    System.out.println("WORKING DIR " + workingDirectory);
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
    Process process =
        new ProcessBuilder(arguments)
            .directory(Paths.get(workingDirectory).toFile())
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start();

    Launcher<CodyAgentServer> launcher =
        new Launcher.Builder<CodyAgentServer>()
            .setRemoteInterface(CodyAgentServer.class)
            .traceMessages(traceWriter())
            .setExecutorService(executorService)
            .setInput(process.getInputStream())
            .setOutput(process.getOutputStream())
            .setLocalService(CLIENT)
            .create();
    Future<Void> listening = launcher.startListening();
    CodyAgentServer server = launcher.getRemoteProxy();
    initialize(server);
    return new CodyAgent(listening, server, process);
  }

  private static void initialize(CodyAgentServer server)
      throws InterruptedException, ExecutionException, TimeoutException, IOException {
    ClientInfo clientInfo = new ClientInfo();
    clientInfo.name = "cody-eclipse";
    clientInfo.version = "0.1.0-SNAPSHOT";
    clientInfo.workspaceRootUri =
        Paths.get(System.getProperty("user.dir"))
            .resolve("dev")
            .resolve("sourcegraph")
            .resolve("scip-typescript")
            .toUri()
            .toString();
    ClientCapabilities capabilities = new ClientCapabilities();
    capabilities.chat = ClientCapabilities.ChatEnum.Streaming;
    clientInfo.capabilities = capabilities;
    ExtensionConfiguration configuration = new ExtensionConfiguration();
    configuration.accessToken =
        Files.readString(
            Paths.get(System.getProperty("user.home"))
                .resolve(".sourcegraph")
                .resolve("access_token.txt"));
    configuration.serverEndpoint = "https://sourcegraph.com";
    configuration.customConfiguration = new HashMap<>();

    clientInfo.extensionConfiguration = configuration;
    server.initialize(clientInfo).get(10, TimeUnit.SECONDS);
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
