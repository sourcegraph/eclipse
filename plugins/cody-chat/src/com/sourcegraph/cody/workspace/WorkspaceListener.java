package com.sourcegraph.cody.workspace;

import static com.sourcegraph.cody.CodyAgent.withAgent;

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
  public void partOpened(IWorkbenchPartReference iWorkbenchPartReference) {
    var state = EditorState.from(iWorkbenchPartReference);
    if (state != null) {
      withAgent(
          agent -> {
            agent.focusChanged(state);
          });
    }
  }
}
