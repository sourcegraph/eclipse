package com.sourcegraph.cody.chat.agent;

import com.sourcegraph.cody.protocol_generated.*;
import com.sourcegraph.cody.webview_protocol.*;
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
  public CompletableFuture<ProtocolTextDocument> textDocument_openUntitledDocument(
      UntitledTextDocument params) {

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
  public void debug_message(DebugMessage params) {
    // TODO: replace this with proper logging
    System.out.println(params.channel + ": " + params.message);
  }

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

  @Override
  public CompletableFuture<Boolean> env_openExternal(Env_OpenExternalParams params) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void webview_registerWebviewViewProvider(
      Webview_RegisterWebviewViewProviderParams params) {
    // TODO Auto-generated method stub

  }

  @Override
  public void webview_createWebviewPanel(Webview_CreateWebviewPanelParams params) {
    // TODO Auto-generated method stub

  }

  @Override
  public void webview_dispose(Webview_DisposeParams params) {
    // TODO Auto-generated method stub

  }

  @Override
  public void webview_reveal(Webview_RevealParams params) {
    // TODO Auto-generated method stub

  }

  @Override
  public void webview_setTitle(Webview_SetTitleParams params) {
    // TODO Auto-generated method stub

  }

  @Override
  public void webview_setIconPath(Webview_SetIconPathParams params) {
    // TODO Auto-generated method stub

  }

  @Override
  public void webview_setOptions(Webview_SetOptionsParams params) {
    // TODO Auto-generated method stub

  }

  @Override
  public void webview_setHtml(Webview_SetHtmlParams params) {
    // TODO Auto-generated method stub

  }
}
