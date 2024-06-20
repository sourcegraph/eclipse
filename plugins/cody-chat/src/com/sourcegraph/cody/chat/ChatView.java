package com.sourcegraph.cody.chat;

import static java.lang.System.out;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sourcegraph.cody.CodyAgent;
import com.sourcegraph.cody.WebviewServer;
import com.sourcegraph.cody.chat.access.AddProfileAction;
import com.sourcegraph.cody.chat.access.TokenSelectionView;
import com.sourcegraph.cody.chat.access.TokenStorage;
import com.sourcegraph.cody.protocol_generated.ProtocolTypeAdapters;
import com.sourcegraph.cody.protocol_generated.Webview_ReceiveMessageStringEncodedParams;

import jakarta.inject.Inject;

public class ChatView extends ViewPart {
  public static final String ID = "com.sourcegraph.cody.chat.ChatView";

  @Inject private Display display;

  @Inject private TokenStorage tokenStorage;

  @Inject private IWorkbenchPage page;

  @Inject IEclipseContext context;

  public static Gson gson = ChatView.createGson();

  private static Gson createGson() {
    GsonBuilder builder = new GsonBuilder();
    configureGson(builder);
    return builder.create();
  }

  private static void configureGson(GsonBuilder builder) {
    ProtocolTypeAdapters.register(builder);
  }

  private void doPostMessage(Browser browser, String message) {
    String stringifiedMessage = gson.toJson(message);
    browser.execute("eclipse_postMessage(" + stringifiedMessage + ");");
  }

  @Override
  public void createPartControl(Composite parent) {
    try {
      addLogInAction();
      addRestartCodyAction();
      if (tokenStorage.getActiveProfileName().isPresent()) {
        addWebview(parent);
      } else {
        addLoginButton(parent);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  Button loginButton;

  public void addLoginButton(Composite parent) {
    var button = new Button(parent, SWT.NONE);
    button.setText("Log in");
    button.setToolTipText("Log into your Cody account");
    button.addSelectionListener(
        new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            var isDone = new AtomicBoolean(false);
            tokenStorage.addCallback(
                () -> {
                  display.asyncExec(
                      () -> {
                        if (isDone.compareAndSet(false, true)) {
                          button.dispose();
                          addWebview(parent);
                        }
                      });
                });
            var action = new AddProfileAction(context);
            action.run();
          }
        });
  }

  public void addWebview(Composite parent) {
    AtomicReference<Browser> browser = new AtomicReference<>();
    ArrayList<String> pendingExtensionMessages = new ArrayList<>();
    AtomicReference<String> chatId = new AtomicReference<>("");
    CodyAgent.CLIENT.extensionMessageConsumer =
        (message) -> {
          display.asyncExec(
              () -> {
                System.out.println("WEBVIEW/POST_MESSAGE " + message);
                if (browser.get() != null && pendingExtensionMessages.isEmpty()) {
                  doPostMessage(browser.get(), message);
                } else {
                  pendingExtensionMessages.add(message);
                }
              });
        };
    tokenStorage.updateCodyAgentConfiguration();

    CodyAgent agent;
    try {
      agent = CodyAgent.start();
    } catch (Exception e) {
      e.printStackTrace();
      return;
    }
    var b = new Browser(parent, SWT.EDGE);
    browser.set(b);
    try {
      System.out.println("CHAT/NEW");
      chatId.set(agent.server.chat_new(null).get(5, TimeUnit.SECONDS));
      System.out.println("DONE - CHAT/NEW " + chatId);
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      e.printStackTrace();
    }
    createCallbacks(browser.get(), agent, chatId.get());

    try {

      for (var message : pendingExtensionMessages) {
        doPostMessage(browser.get(), message);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    try {

      var server = new WebviewServer();
      var port = server.start();
      browser.get().setUrl(String.format("http://localhost:%d", port));
    } catch (Exception e) {
      System.out.println("Failed to start webview server");
      e.printStackTrace();
    }
  }

  private void createCallbacks(Browser browser, CodyAgent agent, String chatId) {
    new BrowserFunction(browser, "eclipse_receiveMessage") {
      @Override
      public Object function(Object[] arguments) {
        display.asyncExec(
            () -> {
              String message = (String) arguments[0];
              System.out.println("SERVER - eclipse_receiveMessage: " + message);
              Webview_ReceiveMessageStringEncodedParams params =
                  new Webview_ReceiveMessageStringEncodedParams();
              params.id = chatId;
              params.messageStringEncoded = message;
              agent.server.webview_receiveMessageStringEncoded(params);
            });
        return null;
      }
      ;
    };

    new BrowserFunction(browser, "eclipse_log") {
      @Override
      public Object function(Object[] arguments) {
        out.println("SERVER - eclipse_log: " + arguments[0]);
        return null;
      }
      ;
    };

    new BrowserFunction(browser, "eclipse_getToken") {
      @Override
      public Object function(Object[] arguments) {
        return tokenStorage.getActiveProfileName().map(tokenStorage::getToken).orElse("");
      }
      ;
    };
  }

  private void addLogInAction() {
    var action =
        new Action() {
          @Override
          public void run() {
            try {
              page.showView(TokenSelectionView.ID);
            } catch (PartInitException e) {
              e.printStackTrace();
            }
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

  private void addRestartCodyAction() {
    var action =
        new Action() {
          @Override
          public void run() {
            try {
              CodyAgent.restart();
            } catch (IOException e) {
              e.printStackTrace();
            }
          }
        };

    action.setText("Restart Cody");
    action.setToolTipText("Restart Cody Agent");
    action.setImageDescriptor(
        PlatformUI.getWorkbench()
            .getSharedImages()
            .getImageDescriptor(ISharedImages.IMG_ELCL_SYNCED));

    var toolBar = getViewSite().getActionBars().getToolBarManager();
    toolBar.add(action);
  }

  @Override
  public void setFocus() {}
}
