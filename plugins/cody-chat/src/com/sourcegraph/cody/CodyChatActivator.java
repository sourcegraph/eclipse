package com.sourcegraph.cody;

import com.sourcegraph.cody.workspace.WorkspaceListener;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class CodyChatActivator extends AbstractUIPlugin {

  @Override
  public void start(BundleContext bundleContext) throws Exception {
    // async exec to make sure the UI is initialized
    Display.getDefault()
        .asyncExec(
            () -> {
              @Nullable
              IWorkbenchWindow workbenchWindow =
                  PlatformUI.getWorkbench().getActiveWorkbenchWindow();

              if (workbenchWindow == null) {
                System.out.println("No active workbench window");
                return;
              }

              var partService = workbenchWindow.getPartService();

              partService.addPartListener(new WorkspaceListener());

              // Uncomment below to debug the workspace listener.
              // partService.addPartListener(new DebugListener());
            });
  }
}
