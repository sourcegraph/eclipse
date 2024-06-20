package com.sourcegraph.cody.chat.access;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Shell;

import jakarta.inject.Inject;

public class AddProfileAction extends Action {

  IEclipseContext context;
  @Inject Shell shell;

  public AddProfileAction(IEclipseContext context) {
    this.context = context;
    ContextInjectionFactory.inject(this, context);
  }

  public void run() {
    var profile = NewTokenDialog.ask(shell);
    if (profile.isPresent()) {
      new LogInJob(context, profile.get().name, profile.get().url).schedule();
    }
  }
}
