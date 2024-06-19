package com.sourcegraph.cody;

import com.sourcegraph.cody.protocol_generated.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class CodyAgentClientImpl implements CodyAgentClient {
  @Override
  public CompletableFuture<String> window_showMessage(ShowWindowMessageParams params) {

    return null;
  }

  @Override
  public CompletableFuture<Boolean> textDocument_edit(TextDocumentEditParams params) {

    return null;
  }

  @Override
  public CompletableFuture<Boolean> textDocument_openUntitledDocument(UntitledTextDocument params) {

    return null;
  }

  @Override
  public CompletableFuture<Boolean> textDocument_show(TextDocument_ShowParams params) {

    return null;
  }

  @Override
  public CompletableFuture<Boolean> workspace_edit(WorkspaceEditParams params) {

    return null;
  }

  @Override
  public CompletableFuture<Void> webview_create(Webview_CreateParams params) {

    return null;
  }

  @Override
  public void debug_message(DebugMessage params) {}

  @Override
  public void editTask_didUpdate(EditTask params) {}

  @Override
  public void editTask_didDelete(EditTask params) {}

  @Override
  public void codeLenses_display(DisplayCodeLensParams params) {}

  @Override
  public void ignore_didChange(Void params) {}

  @Override
  public void webview_postMessageStringEncoded(Webview_PostMessageStringEncodedParams params) {

    if (extensionMessageConsumer != null && params.stringEncodedMessage != null) {
      extensionMessageConsumer.accept(params.stringEncodedMessage);
    }
  }

  @Override
  public void webview_postMessage(WebviewPostMessageParams params) {
    throw new IllegalStateException(
        "webview/postMessage got called when webview/postMessageString was expected. To fix this"
            + " problem, make sure the webviewMessages client capability is set to 'string'.");
  }

  @Override
  public void progress_start(ProgressStartParams params) {}

  @Override
  public void progress_report(ProgressReportParams params) {}

  @Override
  public void progress_end(Progress_EndParams params) {}

  @Override
  public void remoteRepo_didChange(Void params) {}

  @Override
  public void remoteRepo_didChangeState(RemoteRepoFetchState params) {}

  public Consumer<String> extensionMessageConsumer;
}
