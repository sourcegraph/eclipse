package com.sourcegraph.cody.workspace;

import com.sourcegraph.cody.WrappedRuntimeException;
import com.sourcegraph.cody.chat.agent.CodyManager;
import com.sourcegraph.cody.edits.FileEdit;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.PlatformUI;

public class LensDebugHandler extends AbstractHandler {

  CodyManager manager = CodyManager.INSTANCE;

  @Override
  public Object execute(ExecutionEvent event) {
    if (CodyManager.INSTANCE == null) {
      MessageDialog.openError(
          PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
          "Error",
          "First open the chat to make sure that agent is running.");
    }

    CodyManager.INSTANCE.withAgent(
        a -> {
          var editor =
              PlatformUI.getWorkbench()
                  .getActiveWorkbenchWindow()
                  .getActivePage()
                  .getActivePartReference();
          var state = EditorState.from(editor);

          if (state == null) {
            MessageDialog.openError(
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                "Error",
                "No editor focused");
            return;
          }

          var document = state.getDocument();

          try {
            var line3 = document.getLineOffset(3);
            var line6 = document.getLineOffset(6);
            var line9 = document.getLineOffset(9);
            var line12 = document.getLineOffset(12);
            a.editManager.addEdit(state.uri, toUpperCaseEdit(line3, line6 - line3, document));
            a.editManager.addEdit(state.uri, toUpperCaseEdit(line9, line12 - line9, document));
          } catch (BadLocationException e) {
            throw new RuntimeException(e);
          }
        });

    return null;
  }

  private FileEdit toUpperCaseEdit(int offset, int length, IDocument document) {
    try {
      String text = document.get(offset, length).toUpperCase();
      return new FileEdit(offset, length, text);
    } catch (BadLocationException e) {
      throw new WrappedRuntimeException(e);
    }
  }
}
