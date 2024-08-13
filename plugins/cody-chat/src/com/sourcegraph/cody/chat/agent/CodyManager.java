package com.sourcegraph.cody.chat.agent;

import com.sourcegraph.cody.logging.CodyLogger;
import com.sourcegraph.cody.protocol_generated.ExtensionConfiguration;
import jakarta.inject.Singleton;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.eclipse.e4.core.di.annotations.Creatable;

@Creatable
@Singleton
public class CodyManager {
  public ExecutorService executorService = Executors.newCachedThreadPool();
  public ExtensionConfiguration config;

  // null when not started, pending CompletableFuture when starting, completed when started
  private AtomicReference<CompletableFuture<CodyAgent>> agentHolder = new AtomicReference<>(null);
  private AtomicReference<CompletableFuture<Integer>> webserverPortHolder =
      new AtomicReference<>(null);

  private final CodyLogger log = new CodyLogger(getClass());

  /**
   * This method assures that actions are run only when the webserver is running and the agent is
   * started. One webserver can be used by subsequent agents. On the other hand if webserver is
   * disposed we are also disposing the running agent
   *
   * @param onFailure what to do if the agent fails during the execution
   * @param action action to perform
   */
  public void withAgent(OnFailure onFailure, Consumer<CodyAgent> action) {
    // Check if there is a webserver running or starting
    if (webserverPortHolder.compareAndSet(null, new CompletableFuture<>())) {
      log.info("No asset webserver, starting a new one");
      // start webserver
      new StartWebserverJob(this, webserverPortHolder.get()).schedule();
      // dispose old agent to restart it
      var oldAgentFuture = agentHolder.getAndSet(null);
      if (oldAgentFuture != null) {
        oldAgentFuture.thenAccept(CodyAgent::dispose);
      }
    }

    // Check if there is an agent running or starting
    if (agentHolder.compareAndSet(null, new CompletableFuture<>())) {
      log.info("No Cody agent, starting a new one");
      new StartAgentJob(this, agentHolder.get(), webserverPortHolder.get()).schedule();
    }

    agentHolder.get().thenAccept((agent) -> agent.runChecked(onFailure, action));
  }

  public void withAgent(Consumer<CodyAgent> action) {
    withAgent(OnFailure.LOG, action);
  }

  public void onConfigChange(ExtensionConfiguration config) {
    this.config = config;
    if (agentHolder.get() != null) {
      // Notify agent about config change
      withAgent(OnFailure.LOG, agent -> agent.server.extensionConfiguration_didChange(config));
    }
  }

  public void webserverDisposed() {
    var oldFuture = webserverPortHolder.getAndSet(null);
    if (!oldFuture.isDone()) {
      throw new AssertionError(
          "Webserver disposed before being started. This should never have happened.");
    }
  }

  public void agentDisposed() {
    var oldFuture = agentHolder.getAndSet(null);
    if (!oldFuture.isDone()) {
      throw new AssertionError(
          "Agent disposed before being started. This should never have happened.");
    }
  }
}
