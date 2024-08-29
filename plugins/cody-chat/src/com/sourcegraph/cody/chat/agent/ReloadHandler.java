package com.sourcegraph.cody.chat.agent;

import com.sourcegraph.cody.chat.ChatView;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PlatformUI;

public class ReloadHandler extends AbstractHandler {

  @Override
  public Object execute(ExecutionEvent event) throws ExecutionException {
    var view =
        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(ChatView.ID);

    if (view instanceof ChatView) {
      ((ChatView) view).reload();
    }
    return null;
  }
}
