package com.sourcegraph.cody;

import java.util.concurrent.CompletableFuture;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

public class StartAgentJob extends Job {

  public CompletableFuture<CodyAgent> agent = new CompletableFuture<>();
  public int webserverPort = 0;

  public StartAgentJob() {
    super("Starting Cody...");
  }

  @Override
  protected IStatus run(IProgressMonitor monitor) {
    try {
      var server = new WebviewServer();
      webserverPort = server.start();
    } catch (Exception e) {
      e.printStackTrace();
      return Status.CANCEL_STATUS;
    }

    try {
      agent.complete(CodyAgent.start());

    } catch (Exception e) {
      e.printStackTrace();
      agent.completeExceptionally(e);
      return Status.CANCEL_STATUS;
    }

    return Status.OK_STATUS;
  }
}
