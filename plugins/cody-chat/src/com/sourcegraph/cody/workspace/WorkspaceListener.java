package com.sourcegraph.cody.workspace;

import static com.sourcegraph.cody.CodyAgent.withAgent;

import com.sourcegraph.cody.WrappedRuntimeException;
import com.sourcegraph.cody.protocol_generated.Position;
import com.sourcegraph.cody.protocol_generated.Range;
import org.eclipse.jface.text.*;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;

public class WorkspaceListener implements IPartListener2 {
  @Override
  public void partActivated(IWorkbenchPartReference partReference) {
    var state = EditorState.from(partReference);
    if (state != null) {
      withAgent(
          agent -> {
            agent.focusChanged(state);
          });
    }
  }

  @Override
  public void partOpened(IWorkbenchPartReference partReference) {
    var state = EditorState.from(partReference);
    if (state != null) {
      withAgent(
          agent -> {
            agent.fileOpened(state);
          });

      setupSelectionListener(state);
      setupContentListener(state);
    }
  }

  public static void setupSelectionListener(EditorState state) {
    state
        .editor
        .getSelectionProvider()
        .addSelectionChangedListener(
            event -> {
              // We are sure it is a ITextEditor so it *should* use ITextSelection
              var selection = ((ITextSelection) event.getSelection());
              var document = state.getDocument();
              var range = new Range();
              range.start = positionFor(selection.getOffset(), document);
              range.end = positionFor(selection.getOffset() + selection.getLength(), document);

              withAgent(
                  agent -> {
                    agent.selectionChanged(state, range);
                  });
            });
  }

  public static void setupContentListener(EditorState state) {
    state
        .editor
        .getDocumentProvider()
        .getDocument(state.editor.getEditorInput())
        .addDocumentListener(new IDocumentListener() {
          @Override
          public void documentAboutToBeChanged(DocumentEvent documentEvent) {}

          @Override
          public void documentChanged(DocumentEvent documentEvent) {
            withAgent(
                agent -> {
                  agent.fileChanged(state);
                });
            }
        });
  }

  private static Position positionFor(int offset, IDocument document) {
    try {
      var line = document.getLineOfOffset(offset);
      var lineOffset = document.getLineOffset(line);
      var character = offset - lineOffset;
      var position = new Position();
      position.line = line;
      position.character = character;
      return position;
    } catch (BadLocationException e) {
      throw new WrappedRuntimeException(e);
    }
  }
}
