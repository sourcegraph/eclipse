package com.sourcegraph.cody;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.sourcegraph.cody.protocol_generated.ClientInfo;
import com.sourcegraph.cody.protocol_generated.CodyAgentServer;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.ui.services.IDisposable;

public class CodyAgent implements IDisposable {

  private final Future<Void> listening;
  private final CodyAgentClientImpl client;
  private final CodyAgentServer server;
  private final Process process;

  public CodyAgent(
      Future<Void> listening, CodyAgentClientImpl client, CodyAgentServer server, Process process) {
    this.listening = listening;
    this.client = client;
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
      server.shutdown().get(1, TimeUnit.SECONDS);
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

  public static CodyAgent start() throws IOException {
    if (AGENT != null && AGENT.isRunning()) {
      return AGENT;
    }
    System.out.println("Cody is starting");
    CodyAgentClientImpl client = new CodyAgentClientImpl();
    String workingDirectory = System.getProperty("user.dir");
    System.out.println("WORKING DIR " + workingDirectory);
    Path agentPath =
        Paths.get(System.getProperty("user.home"))
            .resolve("dev")
            .resolve("sourcegraph")
            .resolve("eclipse")
            .resolve("cody")
            .resolve("agent")
            .resolve("dist")
            .resolve("index.js");
    ArrayList<String> arguments = new ArrayList<>();
    arguments.add("node");
    arguments.add("--enable-source-maps");
    arguments.add(agentPath.toString());
    Process process =
        new ProcessBuilder(arguments).directory(Paths.get(workingDirectory).toFile()).start();

    Launcher<CodyAgentServer> launcher =
        new Launcher.Builder<CodyAgentServer>()
            .setRemoteInterface(CodyAgentServer.class)
            .traceMessages(traceWriter())
            .setExecutorService(executorService)
            .setInput(process.getInputStream())
            .setOutput(process.getOutputStream())
            .setLocalService(client)
            .create();
    Future<Void> listening = launcher.startListening();
    CodyAgentServer server = launcher.getRemoteProxy();
    return new CodyAgent(listening, client, server, process);
  }

  private static void initialize(CodyAgentServer server) {
    new ClientInfo();
    server.initialize(clientInfo);
    server.initialized();
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
        System.out.println("unable to trace JSON-RPC debugging information to path " + tracePath);
        e.printStackTrace();
      }
    }
    return null;
  }
}
