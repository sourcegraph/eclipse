package com.sourcegraph.cody.chat;

import static java.lang.System.out;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
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
    addLogInAction();
    addRestartCodyAction();
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

    CodyAgent agent = CodyAgent.start();
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

    browser.get().setUrl("http://localhost:8000/cody-index.html");
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
              //              browser.execute("eclipse_receiveMessage(\"received: " + arguments[0] +
              // "\");");
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
