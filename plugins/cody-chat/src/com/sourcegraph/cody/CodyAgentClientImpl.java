package com.sourcegraph.cody;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.sourcegraph.cody.chat.ChatView;
import com.sourcegraph.cody.protocol_generated.*;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class CodyAgentClientImpl implements CodyAgentClient {
  @Override
  public CompletableFuture<String> window_showMessage(ShowWindowMessageParams params) {
    System.out.println(String.format("window_showMessage(%s)", params));
    return null;
  }

  @Override
  public CompletableFuture<Boolean> textDocument_edit(TextDocumentEditParams params) {
    System.out.println(String.format("textDocument_edit(%s)", params));
    return null;
  }

  @Override
  public CompletableFuture<Boolean> textDocument_openUntitledDocument(UntitledTextDocument params) {
    System.out.println(String.format("textDocument_openUntitledDocument(%s)", params));
    return null;
  }

  @Override
  public CompletableFuture<Boolean> textDocument_show(TextDocument_ShowParams params) {
    System.out.println(String.format("textDocument_show(%s)", params));
    return null;
  }

  @Override
  public CompletableFuture<Boolean> workspace_edit(WorkspaceEditParams params) {
    System.out.println(String.format("workspace_edit(%s)", params));
    return null;
  }

  @Override
  public CompletableFuture<Void> webview_create(Webview_CreateParams params) {
    System.out.println(String.format("webview_create(%s)", params));
    return null;
  }

  @Override
  public void debug_message(DebugMessage params) {
    System.out.println(String.format("%s: %s", params.channel, params.message));
  }

  @Override
  public void editTask_didUpdate(EditTask params) {
    System.out.println(String.format("editTask_didUpdate(%s)", params));
  }

  @Override
  public void editTask_didDelete(EditTask params) {
    System.out.println(String.format("editTask_didDelete(%s)", params));
  }

  @Override
  public void codeLenses_display(DisplayCodeLensParams params) {
    System.out.println("codeLenses_display called with params: " + params);
  }

  @Override
  public void ignore_didChange(Void params) {
    System.out.println("ignore_didChange called with params: " + params);
  }

  @Override
  public void webview_postMessage(WebviewPostMessageParams params) {
    System.out.println("webview_postMessage called with params: " + ChatView.gson.toJson(params));
    if (extensionMessageConsumer != null && params.message != null) {
      extensionMessageConsumer.accept(params.message);
    }
  }

  @Override
  public void progress_start(ProgressStartParams params) {
    System.out.println("progress_start called with params: " + params);
  }

  @Override
  public void progress_report(ProgressReportParams params) {
    System.out.println("progress_report called with params: " + params);
  }

  @Override
  public void progress_end(Progress_EndParams params) {
    System.out.println("progress_end called with params: " + params);
  }

  @Override
  public void remoteRepo_didChange(Void params) {
    System.out.println("remoteRepo_didChange called with params: " + params);
  }

  @Override
  public void remoteRepo_didChangeState(RemoteRepoFetchState params) {
    System.out.println("remoteRepo_didChangeState called with params: " + params);
  }

  public Consumer<JsonElement> extensionMessageConsumer;

  @Override
  public void grosshacks_webview_postMessage(ExtensionMessage params) {
    System.out.println(
        String.format("grosshacks/webview/postMessage %s", new Gson().toJson(params)));
    // TODO Auto-generated method stub

  }
}
