package com.sourcegraph.cody.handlers;

import com.sourcegraph.cody.webview_protocol.WebviewMessage;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.dialogs.SaveAsDialog;

public class NewFileHandler implements MessageHandler<WebviewMessage.NewFileWebviewMessage> {

  @Override
  public void doHandle(WebviewMessage.NewFileWebviewMessage message) {
    var display = Display.getDefault();
    display.asyncExec(
        () -> {
          var dialog = new SaveAsDialog(display.getActiveShell());
          dialog.open();
          dialog.setBlockOnOpen(true);
          var result = dialog.getResult();

          if (result != null) {
            var file = ResourcesPlugin.getWorkspace().getRoot().getFile(result);
            var contents = new ByteArrayInputStream(message.text.getBytes(StandardCharsets.UTF_8));
            try {
              file.create(contents, true, new NullProgressMonitor());
            } catch (CoreException e) {
              throw new RuntimeException(e);
            }
          }
        });
  }
}
