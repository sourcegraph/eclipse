package com.sourcegraph.cody.chat;

import static java.lang.System.out;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

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
import com.sourcegraph.cody.StartAgentJob;
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

  final StartAgentJob job = new StartAgentJob();
  private volatile String chatId = "";

  public static Gson gson = ChatView.createGson();

  private static Gson createGson() {
    GsonBuilder builder = new GsonBuilder();
    configureGson(builder);
    return builder.create();
  }

  private static void configureGson(GsonBuilder builder) {
    ProtocolTypeAdapters.register(builder);
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

  private final ArrayList<String> pendingExtensionMessages = new ArrayList<>();

  public void addWebview(Composite parent) {
    final Browser browser = new Browser(parent, SWT.EDGE);
    CodyAgent.CLIENT.extensionMessageConsumer =
        (message) ->
            display.asyncExec(
                () -> {
                  System.out.println("WEBVIEW/POST_MESSAGE " + message);
                  if (pendingExtensionMessages.isEmpty()) {
                    doPostMessage(browser, message);
                  } else {
                    pendingExtensionMessages.add(message);
                  }
                });
    job.schedule();
    addStartNewChatAction(browser);
    onStartNewChat(browser);
  }

  private void doPostMessage(Browser browser, String message) {
    String stringifiedMessage = gson.toJson(message);
    browser.execute("eclipse_postMessage(" + stringifiedMessage + ");");
  }

  private void onStartNewChat(Browser browser) {
    System.out.println("onStartNewChat()");
    CodyAgent.executorService.execute(
        () -> {
          CodyAgent agent;
          try {
            agent = job.agent.get(20, TimeUnit.SECONDS);
          } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
            return;
          }
          System.out.println("Agent completed...");
          try {
            System.out.println("Callbacks done completed...");
            System.out.println("CHAT/NEW");
            pendingExtensionMessages.clear();
            chatId = agent.server.chat_new(null).get(5, TimeUnit.SECONDS);
            System.out.println("DONE - CHAT/NEW " + chatId);
          } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
            return; // TODO: display error message to the user
          }

          // We have the chat ID, let's update the browser URL.
          display.asyncExec(
              () -> {
                var url = String.format("http://localhost:%d", job.webserverPort);
                System.out.println("AGENT IS READY! " + url);
                browser.setUrl(url);

                try {
                  for (var message : pendingExtensionMessages) {
                    doPostMessage(browser, message);
                  }
                } catch (Exception e) {
                  e.printStackTrace();
                }

                createCallbacks(browser, agent);
              });
        });
  }

  private void createCallbacks(Browser browser, CodyAgent agent) {
    new BrowserFunction(browser, "eclipse_receiveMessage") {
      @Override
      public Object function(Object[] arguments) {
        if (chatId.isEmpty()) {
          System.out.println("CHAT ID IS EMPTY!!");
          return null;
        }
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

  private void addStartNewChatAction(Browser browser) {
    var action =
        new Action() {
          @Override
          public void run() {
            onStartNewChat(browser);
          }
        };

    action.setText("Start new chat");
    action.setToolTipText("Start new chat session");
    action.setImageDescriptor(
        PlatformUI.getWorkbench()
            .getSharedImages()
            .getImageDescriptor(ISharedImages.IMG_ETOOL_CLEAR));
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
