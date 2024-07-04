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

  @JsonRequest("textDocument/edit")
  CompletableFuture<Boolean> textDocument_edit(TextDocumentEditParams params);

  @JsonRequest("textDocument/openUntitledDocument")
  CompletableFuture<Boolean> textDocument_openUntitledDocument(UntitledTextDocument params);

  @JsonRequest("textDocument/show")
  CompletableFuture<Boolean> textDocument_show(TextDocument_ShowParams params);

  @JsonRequest("workspace/edit")
  CompletableFuture<Boolean> workspace_edit(WorkspaceEditParams params);

  @JsonRequest("webview/create")
  CompletableFuture<Void> webview_create(Webview_CreateParams params);

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

  @JsonNotification("webview/postMessage")
  void webview_postMessage(WebviewPostMessageParams params);

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
}
