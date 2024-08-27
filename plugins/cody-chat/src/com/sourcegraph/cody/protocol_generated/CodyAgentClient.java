package com.sourcegraph.cody.protocol_generated;

import java.util.concurrent.CompletableFuture;
import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;

@SuppressWarnings("unused")
public interface CodyAgentClient {
  // ========
  // Requests
  // ========
  @JsonRequest("window/showMessage")
  CompletableFuture<String> window_showMessage(ShowWindowMessageParams params);

  @JsonRequest("window/showSaveDialog")
  CompletableFuture<String> window_showSaveDialog(SaveDialogOptionsParams params);

  @JsonRequest("textDocument/edit")
  CompletableFuture<Boolean> textDocument_edit(TextDocumentEditParams params);

  @JsonRequest("textDocument/openUntitledDocument")
  CompletableFuture<ProtocolTextDocument> textDocument_openUntitledDocument(
      UntitledTextDocument params);

  @JsonRequest("textDocument/show")
  CompletableFuture<Boolean> textDocument_show(TextDocument_ShowParams params);

  @JsonRequest("workspace/edit")
  CompletableFuture<Boolean> workspace_edit(WorkspaceEditParams params);

  @JsonRequest("uri/readUTF8")
  CompletableFuture<Uri_ReadUTF8Result> uri_readUTF8(Uri_ReadUTF8Params params);

  @JsonRequest("env/openExternal")
  CompletableFuture<Boolean> env_openExternal(Env_OpenExternalParams params);

  // =============
  // Notifications
  // =============
  @JsonNotification("debug/message")
  void debug_message(DebugMessage params);

  @JsonNotification("editTask/didUpdate")
  void editTask_didUpdate(EditTask params);

  @JsonNotification("editTask/didDelete")
  void editTask_didDelete(EditTask params);

  @JsonNotification("codeLenses/display")
  void codeLenses_display(DisplayCodeLensParams params);

  @JsonNotification("ignore/didChange")
  void ignore_didChange(Void params);

  @JsonNotification("webview/postMessageStringEncoded")
  void webview_postMessageStringEncoded(Webview_PostMessageStringEncodedParams params);

  @JsonNotification("progress/start")
  void progress_start(ProgressStartParams params);

  @JsonNotification("progress/report")
  void progress_report(ProgressReportParams params);

  @JsonNotification("progress/end")
  void progress_end(Progress_EndParams params);

  @JsonNotification("remoteRepo/didChange")
  void remoteRepo_didChange(Void params);

  @JsonNotification("remoteRepo/didChangeState")
  void remoteRepo_didChangeState(RemoteRepoFetchState params);

  @JsonNotification("webview/registerWebviewViewProvider")
  void webview_registerWebviewViewProvider(Webview_RegisterWebviewViewProviderParams params);

  @JsonNotification("webview/createWebviewPanel")
  void webview_createWebviewPanel(Webview_CreateWebviewPanelParams params);

  @JsonNotification("webview/dispose")
  void webview_dispose(Webview_DisposeParams params);

  @JsonNotification("webview/reveal")
  void webview_reveal(Webview_RevealParams params);

  @JsonNotification("webview/setTitle")
  void webview_setTitle(Webview_SetTitleParams params);

  @JsonNotification("webview/setIconPath")
  void webview_setIconPath(Webview_SetIconPathParams params);

  @JsonNotification("webview/setOptions")
  void webview_setOptions(Webview_SetOptionsParams params);

  @JsonNotification("webview/setHtml")
  void webview_setHtml(Webview_SetHtmlParams params);

  @JsonNotification("window/didChangeContext")
  void window_didChangeContext(Window_DidChangeContextParams params);
}
