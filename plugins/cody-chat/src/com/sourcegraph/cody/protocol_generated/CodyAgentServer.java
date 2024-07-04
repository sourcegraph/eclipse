package com.sourcegraph.cody.protocol_generated;

import java.util.concurrent.CompletableFuture;
import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;

@SuppressWarnings("unused")
public interface CodyAgentServer {
  // ========
  // Requests
  // ========
  @JsonRequest("initialize")
  CompletableFuture<ServerInfo> initialize(ClientInfo params);

  @JsonRequest("shutdown")
  CompletableFuture<Void> shutdown(Void params);

  @JsonRequest("chat/new")
  CompletableFuture<String> chat_new(Void params);

  @JsonRequest("chat/web/new")
  CompletableFuture<Chat_Web_NewResult> chat_web_new(Void params);

  @JsonRequest("chat/delete")
  CompletableFuture<java.util.List<ChatExportResult>> chat_delete(Chat_DeleteParams params);

  @JsonRequest("chat/restore")
  CompletableFuture<String> chat_restore(Chat_RestoreParams params);

  @JsonRequest("chat/models")
  CompletableFuture<Chat_ModelsResult> chat_models(Chat_ModelsParams params);

  @JsonRequest("chat/export")
  CompletableFuture<java.util.List<ChatExportResult>> chat_export(Chat_ExportParams params);

  @JsonRequest("chat/remoteRepos")
  CompletableFuture<Chat_RemoteReposResult> chat_remoteRepos(Chat_RemoteReposParams params);

  @JsonRequest("chat/submitMessage")
  CompletableFuture<ExtensionMessage> chat_submitMessage(Chat_SubmitMessageParams params);

  @JsonRequest("chat/editMessage")
  CompletableFuture<ExtensionMessage> chat_editMessage(Chat_EditMessageParams params);

  @JsonRequest("commands/explain")
  CompletableFuture<String> commands_explain(Void params);

  @JsonRequest("commands/test")
  CompletableFuture<String> commands_test(Void params);

  @JsonRequest("commands/smell")
  CompletableFuture<String> commands_smell(Void params);

  @JsonRequest("commands/custom")
  CompletableFuture<CustomCommandResult> commands_custom(Commands_CustomParams params);

  @JsonRequest("customCommands/list")
  CompletableFuture<java.util.List<CodyCommand>> customCommands_list(Void params);

  @JsonRequest("editCommands/code")
  CompletableFuture<EditTask> editCommands_code(EditCommands_CodeParams params);

  @JsonRequest("editCommands/test")
  CompletableFuture<EditTask> editCommands_test(Void params);

  @JsonRequest("editCommands/document")
  CompletableFuture<EditTask> editCommands_document(Void params);

  @JsonRequest("editTask/accept")
  CompletableFuture<Void> editTask_accept(EditTask_AcceptParams params);

  @JsonRequest("editTask/undo")
  CompletableFuture<Void> editTask_undo(EditTask_UndoParams params);

  @JsonRequest("editTask/cancel")
  CompletableFuture<Void> editTask_cancel(EditTask_CancelParams params);

  @JsonRequest("editTask/getFoldingRanges")
  CompletableFuture<GetFoldingRangeResult> editTask_getFoldingRanges(GetFoldingRangeParams params);

  @JsonRequest("command/execute")
  CompletableFuture<Object> command_execute(ExecuteCommandParams params);

  @JsonRequest("codeActions/provide")
  CompletableFuture<CodeActions_ProvideResult> codeActions_provide(
      CodeActions_ProvideParams params);

  @JsonRequest("codeActions/trigger")
  CompletableFuture<EditTask> codeActions_trigger(CodeActions_TriggerParams params);

  @JsonRequest("autocomplete/execute")
  CompletableFuture<AutocompleteResult> autocomplete_execute(AutocompleteParams params);

  @JsonRequest("graphql/getRepoIds")
  CompletableFuture<Graphql_GetRepoIdsResult> graphql_getRepoIds(Graphql_GetRepoIdsParams params);

  @JsonRequest("graphql/currentUserId")
  CompletableFuture<String> graphql_currentUserId(Void params);

  @JsonRequest("graphql/currentUserIsPro")
  CompletableFuture<Boolean> graphql_currentUserIsPro(Void params);

  @JsonRequest("featureFlags/getFeatureFlag")
  CompletableFuture<Boolean> featureFlags_getFeatureFlag(FeatureFlags_GetFeatureFlagParams params);

  @JsonRequest("graphql/getCurrentUserCodySubscription")
  CompletableFuture<CurrentUserCodySubscription> graphql_getCurrentUserCodySubscription(
      Void params);

  @JsonRequest("graphql/logEvent")
  CompletableFuture<Void> graphql_logEvent(Event params);

  @JsonRequest("telemetry/recordEvent")
  CompletableFuture<Void> telemetry_recordEvent(TelemetryEvent params);

  @JsonRequest("graphql/getRepoIdIfEmbeddingExists")
  CompletableFuture<String> graphql_getRepoIdIfEmbeddingExists(
      Graphql_GetRepoIdIfEmbeddingExistsParams params);

  @JsonRequest("graphql/getRepoId")
  CompletableFuture<String> graphql_getRepoId(Graphql_GetRepoIdParams params);

  @JsonRequest("git/codebaseName")
  CompletableFuture<String> git_codebaseName(Git_CodebaseNameParams params);

  @JsonRequest("webview/didDispose")
  CompletableFuture<Void> webview_didDispose(Webview_DidDisposeParams params);

  @JsonRequest("webview/receiveMessage")
  CompletableFuture<Void> webview_receiveMessage(Webview_ReceiveMessageParams params);

  @JsonRequest("webview/receiveMessageStringEncoded")
  CompletableFuture<Void> webview_receiveMessageStringEncoded(
      Webview_ReceiveMessageStringEncodedParams params);

  @JsonRequest("diagnostics/publish")
  CompletableFuture<Void> diagnostics_publish(Diagnostics_PublishParams params);

  @JsonRequest("testing/progress")
  CompletableFuture<Testing_ProgressResult> testing_progress(Testing_ProgressParams params);

  @JsonRequest("testing/networkRequests")
  CompletableFuture<Testing_NetworkRequestsResult> testing_networkRequests(Void params);

  @JsonRequest("testing/requestErrors")
  CompletableFuture<Testing_RequestErrorsResult> testing_requestErrors(Void params);

  @JsonRequest("testing/closestPostData")
  CompletableFuture<Testing_ClosestPostDataResult> testing_closestPostData(
      Testing_ClosestPostDataParams params);

  @JsonRequest("testing/memoryUsage")
  CompletableFuture<Testing_MemoryUsageResult> testing_memoryUsage(Void params);

  @JsonRequest("testing/awaitPendingPromises")
  CompletableFuture<Void> testing_awaitPendingPromises(Void params);

  @JsonRequest("testing/workspaceDocuments")
  CompletableFuture<GetDocumentsResult> testing_workspaceDocuments(GetDocumentsParams params);

  @JsonRequest("testing/diagnostics")
  CompletableFuture<Testing_DiagnosticsResult> testing_diagnostics(
      Testing_DiagnosticsParams params);

  @JsonRequest("testing/progressCancelation")
  CompletableFuture<Testing_ProgressCancelationResult> testing_progressCancelation(
      Testing_ProgressCancelationParams params);

  @JsonRequest("testing/reset")
  CompletableFuture<Void> testing_reset(Void params);

  @JsonRequest("extensionConfiguration/change")
  CompletableFuture<AuthStatus> extensionConfiguration_change(ExtensionConfiguration params);

  @JsonRequest("extensionConfiguration/status")
  CompletableFuture<AuthStatus> extensionConfiguration_status(Void params);

  @JsonRequest("textDocument/change")
  CompletableFuture<TextDocument_ChangeResult> textDocument_change(ProtocolTextDocument params);

  @JsonRequest("attribution/search")
  CompletableFuture<Attribution_SearchResult> attribution_search(Attribution_SearchParams params);

  @JsonRequest("ignore/test")
  CompletableFuture<Ignore_TestResult> ignore_test(Ignore_TestParams params);

  @JsonRequest("testing/ignore/overridePolicy")
  CompletableFuture<Void> testing_ignore_overridePolicy(ContextFilters params);

  @JsonRequest("remoteRepo/has")
  CompletableFuture<RemoteRepo_HasResult> remoteRepo_has(RemoteRepo_HasParams params);

  @JsonRequest("remoteRepo/list")
  CompletableFuture<RemoteRepo_ListResult> remoteRepo_list(RemoteRepo_ListParams params);

  // =============
  // Notifications
  // =============
  @JsonNotification("initialized")
  void initialized(Void params);

  @JsonNotification("exit")
  void exit(Void params);

  @JsonNotification("extensionConfiguration/didChange")
  void extensionConfiguration_didChange(ExtensionConfiguration params);

  @JsonNotification("textDocument/didOpen")
  void textDocument_didOpen(ProtocolTextDocument params);

  @JsonNotification("textDocument/didChange")
  void textDocument_didChange(ProtocolTextDocument params);

  @JsonNotification("textDocument/didFocus")
  void textDocument_didFocus(TextDocument_DidFocusParams params);

  @JsonNotification("textDocument/didSave")
  void textDocument_didSave(TextDocument_DidSaveParams params);

  @JsonNotification("textDocument/didClose")
  void textDocument_didClose(ProtocolTextDocument params);

  @JsonNotification("workspace/didDeleteFiles")
  void workspace_didDeleteFiles(DeleteFilesParams params);

  @JsonNotification("workspace/didCreateFiles")
  void workspace_didCreateFiles(CreateFilesParams params);

  @JsonNotification("workspace/didRenameFiles")
  void workspace_didRenameFiles(RenameFilesParams params);

  @JsonNotification("$/cancelRequest")
  void cancelRequest(CancelParams params);

  @JsonNotification("autocomplete/clearLastCandidate")
  void autocomplete_clearLastCandidate(Void params);

  @JsonNotification("autocomplete/completionSuggested")
  void autocomplete_completionSuggested(CompletionItemParams params);

  @JsonNotification("autocomplete/completionAccepted")
  void autocomplete_completionAccepted(CompletionItemParams params);

  @JsonNotification("progress/cancel")
  void progress_cancel(Progress_CancelParams params);
}
