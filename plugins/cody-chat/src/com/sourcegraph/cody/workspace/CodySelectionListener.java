package com.sourcegraph.cody.workspace;

import com.sourcegraph.cody.chat.agent.CodyAgent;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;

public class CodySelectionListener implements CodyListener, ISelectionChangedListener {
  private final CodyAgent agent;
  private final EditorState editorState;

  public CodySelectionListener(CodyAgent agent, EditorState editorState) {
    this.agent = agent;
    this.editorState = editorState;
  }

  @Override
  public void install() {
    var selectionProvider = editorState.editor.getSelectionProvider();
    // We are sure it is a ITextEditor so it *should* use ITextSelection
    var selection = (ITextSelection) selectionProvider.getSelection();
    agent.selectionChanged(
        editorState, editorState.rangeFor(selection.getOffset(), selection.getLength()));
    selectionProvider.addSelectionChangedListener(this);
  }

  @Override
  public void selectionChanged(SelectionChangedEvent selectionChangedEvent) {
    var selection = (ITextSelection) selectionChangedEvent.getSelection();
    agent.selectionChanged(
        editorState, editorState.rangeFor(selection.getOffset(), selection.getLength()));
  }

  @Override
  public void dispose() {
    editorState.editor.getSelectionProvider().removeSelectionChangedListener(this);
  }
}
