package com.sourcegraph.cody.workspace;

import com.sourcegraph.cody.chat.agent.CodyAgent;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.swt.widgets.Display;

public class CodyContentListener implements CodyListener, IDocumentListener {
  private final CodyAgent agent;
  private final EditorState editorState;

  public CodyContentListener(CodyAgent agent, EditorState editorState) {
    this.agent = agent;
    this.editorState = editorState;
  }

  @Override
  public void install() {
    agent.fileOpened(editorState);
    editorState.getDocument().addDocumentListener(this);
  }

  @Override
  public void dispose() {
    Display.getDefault().execute(() -> editorState.getDocument().removeDocumentListener(this));
  }

  @Override
  public void documentAboutToBeChanged(DocumentEvent documentEvent) {}

  @Override
  public void documentChanged(DocumentEvent documentEvent) {
    agent.fileChanged(editorState);
  }
}
