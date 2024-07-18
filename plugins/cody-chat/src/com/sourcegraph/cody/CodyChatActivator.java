package com.sourcegraph.cody;

import static java.lang.System.out;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.*;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class CodyChatActivator extends AbstractUIPlugin {

  @Override
  public void start(BundleContext bundleContext) throws Exception {
    // async exec to make sure the UI is initialized
    Display.getDefault()
        .asyncExec(
            () -> {
              IWorkbenchWindow workbenchWindow =
                  PlatformUI.getWorkbench().getActiveWorkbenchWindow();
              var partService = workbenchWindow.getPartService();
              partService.addPartListener(new DebugListener());
              partService.addPartListener(new WorkspaceListener());

              var selectionListener = workbenchWindow.getSelectionService();
              selectionListener.addPostSelectionListener(new SelectionListener());
            });
  }

  static class DebugListener implements IPartListener2 {

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

  private static class SelectionListener implements ISelectionListener {
    @Override
    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
      out.println("selection changed: " + selection);
    }
  }
}
