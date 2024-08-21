package com.sourcegraph.cody.chat.agent;

import com.sourcegraph.cody.logging.CodyLogger;
import com.sourcegraph.cody.protocol_generated.CodyAgentServer;
import com.sourcegraph.cody.protocol_generated.ProtocolTextDocument;
import com.sourcegraph.cody.protocol_generated.Range;
import com.sourcegraph.cody.protocol_generated.TextDocument_DidFocusParams;
import com.sourcegraph.cody.workspace.EditorState;
import com.sourcegraph.cody.workspace.WorkbenchListener;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class CodyAgent implements Disposable {

  private final Future<Void> listening;
  public final CodyAgentServer server;
  public final int webviewPort;
  public final CodyAgentClientImpl client;
  private final Process process;
  private final CodyManager manager;
  private final WorkbenchListener workbenchListener;

  private final CodyLogger log = new CodyLogger(getClass());

  CodyAgent(
      Future<Void> listening,
      CodyAgentServer server,
      int webviewPort,
      CodyAgentClientImpl client,
      Process process,
      CodyManager manager) {
    this.listening = listening;
    this.server = server;
    this.webviewPort = webviewPort;
    this.client = client;
    this.process = process;
    this.manager = manager;
    workbenchListener = new WorkbenchListener(this);
  }

  public void runChecked(OnFailure onFailure, Consumer<CodyAgent> action) {
    RuntimeException failure = null;
    if (listening.isDone()) {
      failure = new IllegalStateException("Connection closed");
    } else {
      try {
        action.accept(this);
      } catch (RuntimeException e) {
        failure = e;
      }
    }

    if (failure == null) return;

    switch (onFailure) {
      case IGNORE:
        break;
      case LOG:
        dispose();
        log.error("Error running agent action", failure);
        break;
      case THROW:
        dispose();
        throw failure;
      case RETRY:
        log.warn("Error running agent action, retrying", failure);
        dispose();
        manager.withAgent(OnFailure.THROW, action);
        break;
    }
  }

  public void postCreate() {
    workbenchListener.install();
  }

  ////////////////////
  // NOTIFICATIONS //
  ///////////////////

  public void focusChanged(EditorState state) {
    var params = new TextDocument_DidFocusParams();
    params.uri = state.uri;
    if (!listening.isDone()) {
      server.textDocument_didFocus(params);
    }
  }

  public void fileOpened(EditorState state) {
    var params = new ProtocolTextDocument();
    params.uri = state.uri;
    params.content = state.readContents();
    if (!listening.isDone()) {
      server.textDocument_didOpen(params);
    }
  }

  public void selectionChanged(EditorState state, Range range) {
    var params = new ProtocolTextDocument();
    params.uri = state.uri;
    params.selection = range;
    if (!listening.isDone()) {
      server.textDocument_didChange(params);
    }
  }

  public void fileChanged(EditorState state) {
    var params = new ProtocolTextDocument();
    params.uri = state.uri;
    params.content = state.readContents();
    if (!listening.isDone()) {
      server.textDocument_didChange(params);
    }
  }

  public void dispose() {
    workbenchListener.dispose();
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
    manager.agentDisposed();
  }
}
