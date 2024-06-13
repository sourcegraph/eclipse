package com.sourcegraph.cody.chat;

import static java.lang.System.out;

import com.sourcegraph.cody.chat.access.LogInJob;
import com.sourcegraph.cody.chat.access.TokenStorage;
import jakarta.inject.Inject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

public class ChatView extends ViewPart {
  public static final String ID = "com.sourcegraph.cody.chat.ChatView";

  @Inject private Display display;

  @Inject private IEclipseContext context;

  @Inject private TokenStorage tokenStorage;

  @Override
  public void createPartControl(Composite parent) {
    addLogInAction();

    var browser = new Browser(parent, SWT.EDGE);
    browser.setText(loadIndex());

    createCallbacks(browser);
  }

  private void createCallbacks(Browser browser) {
    new BrowserFunction(browser, "postMessage") {
      @Override
      public Object function(Object[] arguments) {
        display.asyncExec(
            () -> {
              browser.execute("receiveMessage(\"received: " + arguments[0] + "\");");
            });
        return null;
      }
      ;
    };

    new BrowserFunction(browser, "logInEclipse") {
      @Override
      public Object function(Object[] arguments) {
        out.println("From webview: " + arguments[0]);
        return null;
      }
      ;
    };

    new BrowserFunction(browser, "getToken") {
      @Override
      public Object function(Object[] arguments) {
        try {
          return tokenStorage.get();
        } catch (StorageException e) {
          e.printStackTrace();
          return null;
        }
      }
      ;
    };
  }

  private void addLogInAction() {
    var action =
        new Action() {
          @Override
          public void run() {
            var job = new LogInJob(context);
            job.schedule();
          }
        };

    action.setText("Log in");
    action.setToolTipText("Log in using GitHub");
    action.setImageDescriptor(
        PlatformUI.getWorkbench()
            .getSharedImages()
            .getImageDescriptor(ISharedImages.IMG_TOOL_NEW_WIZARD));

    var toolBar = getViewSite().getActionBars().getToolBarManager();
    toolBar.add(action);
  }

  @Override
  public void setFocus() {}

  private String loadIndex() {
    try (var stream = getClass().getResourceAsStream("/resources/index.html")) {
      return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      e.printStackTrace();
      return "<h1> Cannot load index.html </h1>";
    }
  }
}
