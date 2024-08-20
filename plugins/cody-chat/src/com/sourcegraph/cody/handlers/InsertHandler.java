package com.sourcegraph.cody.handlers;

import com.sourcegraph.cody.logging.CodyLogger;
import com.sourcegraph.cody.webview_protocol.WebviewMessage;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.AbstractTextEditor;

public class InsertHandler implements MessageHandler<WebviewMessage.InsertWebviewMessage> {

  private CodyLogger log = new CodyLogger(getClass());

  @Override
  public void doHandle(WebviewMessage.InsertWebviewMessage message) {
    var maybeEditor =
        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
    if (!(maybeEditor instanceof AbstractTextEditor)) return;
    var editor = (AbstractTextEditor) maybeEditor;
    var selection = (ITextSelection) editor.getSelectionProvider().getSelection();
    var document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
    try {
      document.replace(selection.getOffset(), selection.getLength(), message.text);
    } catch (BadLocationException e) {
      log.error("Incorrect insert location", e);
    }
  }
}
