package com.sourcegraph.cody;

import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;

import static java.lang.System.out;

class DebugWorkspaceListener implements IPartListener2 {
  @Override
  public void partActivated(IWorkbenchPartReference iWorkbenchPartReference) {
    out.println(
        "partActivated "
            + iWorkbenchPartReference.getTitle()
            + " "
            + iWorkbenchPartReference.getPart(false));
  }

  @Override
  public void partBroughtToTop(IWorkbenchPartReference iWorkbenchPartReference) {
    out.println(
        "partBroughtToTop "
            + iWorkbenchPartReference.getTitle()
            + " "
            + iWorkbenchPartReference.getPart(false));
  }

  @Override
  public void partClosed(IWorkbenchPartReference iWorkbenchPartReference) {
    out.println(
        "partClosed "
            + iWorkbenchPartReference.getTitle()
            + " "
            + iWorkbenchPartReference.getPart(false));
  }

  @Override
  public void partDeactivated(IWorkbenchPartReference iWorkbenchPartReference) {
    out.println(
        "partDeactivated "
            + iWorkbenchPartReference.getTitle()
            + " "
            + iWorkbenchPartReference.getPart(false));
  }

  @Override
  public void partOpened(IWorkbenchPartReference iWorkbenchPartReference) {
    out.println(
        "partOpened "
            + iWorkbenchPartReference.getTitle()
            + " "
            + iWorkbenchPartReference.getPart(false));
  }

  @Override
  public void partHidden(IWorkbenchPartReference iWorkbenchPartReference) {
    out.println(
        "partHidden "
            + iWorkbenchPartReference.getTitle()
            + " "
            + iWorkbenchPartReference.getPart(false));
  }

  @Override
  public void partVisible(IWorkbenchPartReference iWorkbenchPartReference) {
    out.println(
        "partVisible "
            + iWorkbenchPartReference.getTitle()
            + " "
            + iWorkbenchPartReference.getPart(false));
  }

  @Override
  public void partInputChanged(IWorkbenchPartReference iWorkbenchPartReference) {
    out.println(
        "partInputChanged "
            + iWorkbenchPartReference.getTitle()
            + " "
            + iWorkbenchPartReference.getPart(false));
  }
}
