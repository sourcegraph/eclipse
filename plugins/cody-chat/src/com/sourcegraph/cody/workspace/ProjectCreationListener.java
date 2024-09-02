package com.sourcegraph.cody.workspace;

import com.sourcegraph.cody.chat.agent.CodyAgent;
import org.eclipse.core.resources.*;

public class ProjectCreationListener implements CodyListener, IResourceChangeListener {
  private final CodyAgent agent;

  public ProjectCreationListener(CodyAgent agent) {
    this.agent = agent;
  }

  @Override
  public void install() {
    ResourcesPlugin.getWorkspace()
        .addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
  }

  @Override
  public void dispose() {
    ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
  }

  @Override
  public void resourceChanged(IResourceChangeEvent event) {
    for (var c : event.getDelta().getAffectedChildren()) {
      if (c.getResource() instanceof IProject && c.getKind() == IResourceDelta.ADDED) {
        agent.projectChanged(EditorState.getUri(c.getResource()));
      }
    }
  }
}
