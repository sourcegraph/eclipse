package com.sourcegraph.cody;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.ui.services.IDisposable;

public class CodyAgent implements IDisposable {

  private static ExecutorService executorService = Executors.newCachedThreadPool();

  public void stop() {
    System.out.println("Cody is stopping");
  }

  public static CodyAgent start() throws IOException {
    CodyAgentClient client = new CodyAgentClient();
    Process process = new ProcessBuilder("node", "/Users").start();
    new Launcher.Builder<CodyAgentServer>()
        .setRemoteInterface(CodyAgentServer.class)
        .traceMessages(traceWriter())
        .setExecutorService(executorService)
        .setInput(process.getInputStream())
        .setOutput(process.getOutputStream())
        .setLocalService(client)
        .create();
    System.out.println("Cody is starting2");
    return new CodyAgent();
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

  @Override
  public void dispose() {
    // TODO Auto-generated method stub

  }
}
