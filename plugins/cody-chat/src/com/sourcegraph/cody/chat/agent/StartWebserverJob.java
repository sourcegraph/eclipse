package com.sourcegraph.cody.chat.agent;

import com.sourcegraph.cody.WebviewServer;
import java.util.concurrent.CompletableFuture;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;

public class StartWebserverJob extends Job {
  private final CodyManager codyManager;
  private final CompletableFuture<Integer> webserverPort;

  private final ILog log = Platform.getLog(getClass());

  public StartWebserverJob(CodyManager codyManager, CompletableFuture<Integer> webserverPort) {
    super("Starting Cody...");
    this.codyManager = codyManager;
    this.webserverPort = webserverPort;
  }

  @Override
  protected IStatus run(IProgressMonitor iProgressMonitor) {
    try {
      int port = new WebviewServer(codyManager).start();
      webserverPort.complete(port);
      return Status.OK_STATUS;
    } catch (Exception e) {
      log.error("Cannot start WebviewServer", e);
      return Status.CANCEL_STATUS;
    }
  }
}
