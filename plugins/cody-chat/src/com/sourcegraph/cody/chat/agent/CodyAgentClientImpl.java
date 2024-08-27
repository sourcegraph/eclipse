package com.sourcegraph.cody.chat.agent;

import com.sourcegraph.cody.CodyResources;
import com.sourcegraph.cody.logging.CodyLogger;
import com.sourcegraph.cody.protocol_generated.*;
import com.sourcegraph.cody.webview_protocol.*;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

public class CodyAgentClientImpl implements CodyAgentClient {
  private final CodyLogger log = new CodyLogger(getClass());

  @Override
  public CompletableFuture<String> window_showMessage(ShowWindowMessageParams params) {

    return null;
  }

  @Override
  public CompletableFuture<String> window_showSaveDialog(SaveDialogOptionsParams params) {
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
    CompletableFuture<Boolean> result = new CompletableFuture<>();
    Display.getDefault()
        .asyncExec(
            () -> {
              IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
              if (window != null) {
                IWorkbenchPage page = window.getActivePage();
                if (page != null) {
                  try {
                    // Construct a URI from the params.uri string
                    IFileStore fileStore = EFS.getStore(new URI(params.uri));
                    var editor = IDE.openEditorOnFileStore(page, fileStore);
                    if (editor instanceof ITextEditor
                        && params.options != null
                        && params.options.selection != null) {
                      ITextEditor textEditor = (ITextEditor) editor;
                      IDocument document =
                          textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput());

                      TextSelection selection =
                          new TextSelection(
                              document,
                              getOffset(document, params.options.selection.start),
                              getOffset(document, params.options.selection.end)
                                  - getOffset(document, params.options.selection.start));

                      textEditor.selectAndReveal(selection.getOffset(), selection.getLength());
                    }
                    result.complete(true);
                  } catch (Exception e) {
                    // Handle other potential exceptions
                    log.error("Error opening file", e);
                    result.complete(false);
                  }
                }
              }
            });
    return result;
  }

  private int getOffset(IDocument document, Position pos) throws BadLocationException {
    // Convert line and column to offset
    return document.getLineOffset(pos.line.intValue()) + pos.character.intValue();
  }

  @Override
  public CompletableFuture<Boolean> workspace_edit(WorkspaceEditParams params) {

    return null;
  }

  @Override
  public CompletableFuture<Uri_ReadUTF8Result> uri_readUTF8(Uri_ReadUTF8Params params) {
    var webviewProtocol = Constants.webviewasset + ":";
    if (!params.uri.startsWith(webviewProtocol)) {
      throw new RuntimeException(
          "Invalid URI. Only " + webviewProtocol + " is supported. Received: " + params.uri);
    }
    return CompletableFuture.supplyAsync(
        () -> {
          var trimmedName = params.uri.replace(webviewProtocol, "");
          try {
            var resource = CodyResources.loadWebviewBytes(trimmedName);
            var result = new Uri_ReadUTF8Result();
            result.text = new String(resource, StandardCharsets.UTF_8);
            return result;
          } catch (IOException e) {
            log.error("Error loading webview asset", e);
            return null;
          }
        });
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
    CodyResources.setIndexHTML(params.html.getBytes());
  }

  @Override
  public void window_didChangeContext(Window_DidChangeContextParams params) {}
}
