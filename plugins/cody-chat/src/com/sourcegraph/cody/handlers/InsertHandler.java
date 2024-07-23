package com.sourcegraph.cody.handlers;

import com.sourcegraph.cody.protocol_generated.WebviewMessage;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.AbstractTextEditor;

public class InsertHandler implements MessageHandler<WebviewMessage.InsertWebviewMessage> {

  private ILog log = Platform.getLog(getClass());

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
