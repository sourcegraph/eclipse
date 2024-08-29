package com.sourcegraph.cody.workspace;

import com.sourcegraph.cody.chat.agent.CodyAgent;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;

public class WorkbenchListener implements IPartListener2, CodyListener {

  private final CodyAgent agent;
  private final Set<CodyListener> children;

  public WorkbenchListener(CodyAgent agent) {
    this.agent = agent;
    children = Collections.newSetFromMap(new WeakHashMap<>());
  }

  @Override
  public void install() {
    // This can cause UI to hang for a moment after the start of the agent. If that's a problem,
    // split this function into two parts: collecting the state and then notifying the agent. Run
    // the first part with Display.syncExec.
    Display.getDefault()
        .asyncExec(
            () -> {
              var editors =
                  PlatformUI.getWorkbench()
                      .getActiveWorkbenchWindow()
                      .getActivePage()
                      .getEditorReferences();

              for (var editor : editors) {
                var editorState = EditorState.from(editor);
                if (editorState != null) {
                  var contentListener = new CodyContentListener(agent, editorState);
                  children.add(contentListener);
                  contentListener.install();
                  var selectionListener = new CodySelectionListener(agent, editorState);
                  children.add(selectionListener);
                  selectionListener.install();
                }
              }

              var activeEditor =
                  PlatformUI.getWorkbench()
                      .getActiveWorkbenchWindow()
                      .getActivePage()
                      .getActivePartReference();

              var activeEditorState = EditorState.from(activeEditor);
              if (activeEditorState != null) {
                agent.focusChanged(activeEditorState);
              }

              PlatformUI.getWorkbench()
                  .getActiveWorkbenchWindow()
                  .getPartService()
                  .addPartListener(this);
            });
  }

  @Override
  public void partActivated(IWorkbenchPartReference iWorkbenchPartReference) {
    var state = EditorState.from(iWorkbenchPartReference);
    if (state != null) {
      agent.focusChanged(state);
    }
  }

  @Override
  public void partOpened(IWorkbenchPartReference partReference) {
    var state = EditorState.from(partReference);
    if (state != null) {
      var contentListener = new CodyContentListener(agent, state);
      children.add(contentListener);
      contentListener.install();

      var selectionListener = new CodySelectionListener(agent, state);
      children.add(selectionListener);
      selectionListener.install();

      agent.focusChanged(state);
    }
  }

  @Override
  public void dispose() {
    Display.getDefault()
        .execute(
            () -> {
              for (var child : children) {
                child.dispose();
              }
              PlatformUI.getWorkbench()
                  .getActiveWorkbenchWindow()
                  .getPartService()
                  .removePartListener(this);
            });
  }
}
