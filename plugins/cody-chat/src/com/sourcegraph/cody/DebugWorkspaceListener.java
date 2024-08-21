package com.sourcegraph.cody;

import com.sourcegraph.cody.logging.CodyLogger;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;

class DebugWorkspaceListener implements IPartListener2 {
  private CodyLogger log = new CodyLogger(getClass());

  @Override
  public void partActivated(IWorkbenchPartReference iWorkbenchPartReference) {
    log.info(
        "partActivated "
            + iWorkbenchPartReference.getTitle()
            + " "
            + iWorkbenchPartReference.getPart(false));
  }

  @Override
  public void partBroughtToTop(IWorkbenchPartReference iWorkbenchPartReference) {
    log.info(
        "partBroughtToTop "
            + iWorkbenchPartReference.getTitle()
            + " "
            + iWorkbenchPartReference.getPart(false));
  }

  @Override
  public void partClosed(IWorkbenchPartReference iWorkbenchPartReference) {
    log.info(
        "partClosed "
            + iWorkbenchPartReference.getTitle()
            + " "
            + iWorkbenchPartReference.getPart(false));
  }

  @Override
  public void partDeactivated(IWorkbenchPartReference iWorkbenchPartReference) {
    log.info(
        "partDeactivated "
            + iWorkbenchPartReference.getTitle()
            + " "
            + iWorkbenchPartReference.getPart(false));
  }

  @Override
  public void partOpened(IWorkbenchPartReference iWorkbenchPartReference) {
    log.info(
        "partOpened "
            + iWorkbenchPartReference.getTitle()
            + " "
            + iWorkbenchPartReference.getPart(false));
  }

  @Override
  public void partHidden(IWorkbenchPartReference iWorkbenchPartReference) {
    log.info(
        "partHidden "
            + iWorkbenchPartReference.getTitle()
            + " "
            + iWorkbenchPartReference.getPart(false));
  }

  @Override
  public void partVisible(IWorkbenchPartReference iWorkbenchPartReference) {
    log.info(
        "partVisible "
            + iWorkbenchPartReference.getTitle()
            + " "
            + iWorkbenchPartReference.getPart(false));
  }

  @Override
  public void partInputChanged(IWorkbenchPartReference iWorkbenchPartReference) {
    log.info(
        "partInputChanged "
            + iWorkbenchPartReference.getTitle()
            + " "
            + iWorkbenchPartReference.getPart(false));
  }
}
