package com.sourcegraph.cody.workspace;

import com.sourcegraph.cody.chat.agent.CodyAgent;
import com.sourcegraph.cody.edits.FileEditManager;
import java.lang.ref.WeakReference;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.swt.widgets.Display;

public class CodyContentListener implements CodyListener, IDocumentListener {
  private final CodyAgent agent;
  private final EditorState editorState;

  private WeakReference<FileEditManager> fileEditManager;

  public CodyContentListener(CodyAgent agent, EditorState editorState) {
    this.agent = agent;
    this.editorState = editorState;
  }

  @Override
  public void install() {
    agent.fileOpened(editorState);
    editorState.getDocument().addDocumentListener(this);

    var manager = new FileEditManager(editorState, agent.editManager);
    fileEditManager = new WeakReference<FileEditManager>(manager);
    manager.install();
  }

  @Override
  public void dispose() {
    Display.getDefault().execute(() -> editorState.getDocument().removeDocumentListener(this));

    var manager = fileEditManager.get();
    if (manager != null) {
      manager.dispose();
    }
  }

  @Override
  public void documentAboutToBeChanged(DocumentEvent documentEvent) {}

  @Override
  public void documentChanged(DocumentEvent documentEvent) {
    agent.fileChanged(editorState);
  }
}
