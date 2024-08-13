package com.sourcegraph.cody.chat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sourcegraph.cody.chat.access.AddProfileAction;
import com.sourcegraph.cody.chat.access.TokenSelectionView;
import com.sourcegraph.cody.chat.access.TokenStorage;
import com.sourcegraph.cody.chat.agent.CodyAgent;
import com.sourcegraph.cody.chat.agent.CodyManager;
import com.sourcegraph.cody.handlers.MessageHandlers;
import com.sourcegraph.cody.logging.CodyLogger;
import com.sourcegraph.cody.protocol_generated.ProtocolTypeAdapters;
import com.sourcegraph.cody.protocol_generated.WebviewMessage;
import com.sourcegraph.cody.protocol_generated.Webview_ReceiveMessageStringEncodedParams;
import jakarta.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
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

public class ChatView extends ViewPart {
  public static final String ID = "com.sourcegraph.cody.chat.ChatView";

  @Inject private Display display;

  @Inject private TokenStorage tokenStorage;

  @Inject private IWorkbenchPage page;

  @Inject private IEclipseContext context;

  @Inject private MessageHandlers handlers;

  @Inject private CodyManager manager;

  private final CodyLogger log = new CodyLogger(getClass());

  private Browser browserField;
  private volatile String chatId = "";
  private CompletableFuture<Boolean> webviewInitialized = new CompletableFuture<Boolean>();
  private final ConcurrentLinkedQueue<String> pendingExtensionMessages =
      new ConcurrentLinkedQueue<>();

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
        tokenStorage.updateCodyAgentConfiguration();
        addWebview(parent);
      } else {
        addLoginButton(parent);
      }
    } catch (Exception e) {
      log.error("Cannot create chat view", e);
    }
  }

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
                        if (!tokenStorage.getActiveProfileName().orElse("").isEmpty()
                            && isDone.compareAndSet(false, true)) {

                          tokenStorage.updateCodyAgentConfiguration();
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
    final Browser browser = new Browser(parent, SWT.EDGE);
    this.browserField = browser;
    restartWebviewPrototocol(browser);
    addStartNewChatAction();
    onStartNewChat(browser);
  }

  private void doPostMessage(Browser browser, String message) {
    display.asyncExec(
        () -> {
          String stringifiedMessage = gson.toJson(message);
          log.sent(stringifiedMessage);
          browser.execute("eclipse_postMessage(" + stringifiedMessage + ");");
        });
  }

  private void restartWebviewPrototocol(Browser browser) {
    pendingExtensionMessages.clear();
    webviewInitialized = new CompletableFuture<>();
    webviewInitialized.thenRun(() -> flushPendingMessages(browser));
    manager.withAgent(
        agent -> {
          agent.client.extensionMessageConsumer =
              (message) -> {
                if (webviewInitialized.isDone() && pendingExtensionMessages.isEmpty()) {
                  doPostMessage(browser, message);
                } else {
                  pendingExtensionMessages.add(message);
                }
              };
        });
  }

  private void onStartNewChat(Browser browser) {
    manager.withAgent(
        agent -> {
          try {

            chatId = agent.server.chat_new(null).get(5, TimeUnit.SECONDS);

          } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.error("Cannot create new chat", e);
            return; // TODO: display error message to the user
          }

          // We have the chat ID, let's update the browser URL.
          display.asyncExec(
              () -> {
                createCallbacks(browser, agent);
                var url = String.format("http://localhost:%d", agent.webviewPort);
                browser.setUrl(url);
                browser.getParent().layout();
              });
        });
  }

  private void flushPendingMessages(Browser browser) {
    try {
      String message;
      while ((message = pendingExtensionMessages.poll()) != null) {
        doPostMessage(browser, message);
      }
    } catch (Exception e) {
      log.error("Cannot post message", e);
    }
  }

  private void createCallbacks(Browser browser, CodyAgent agent) {
    if (browser == null) {
      return;
    }

    new BrowserFunction(browser, "eclipse_initialized") {
      @Override
      public Object function(Object[] arguments) {
        webviewInitialized.complete(true);
        return null;
      }
    };

    // Most webview messages are proxied directly to the Cody Agent.
    // We only intercept a small number of messages for features are easier
    // to implement directly in Eclipse instead of wiring through the agent
    // to implement low-level editor primitives.
    new BrowserFunction(browser, "eclipse_interceptMessage") {
      @Override
      public Object function(Object[] arguments) {
        log.received(arguments[0].toString() + " (Intercepted)");
        if (chatId.isEmpty()) {
          log.warn("No chat ID");
          return null;
        }
        display.asyncExec(
            () -> {
              String messageJson = (String) arguments[0];
              var message = gson.fromJson(messageJson, WebviewMessage.class);
              boolean handled = handlers.handle(message);
              if (!handled) {
                log.warn("Unhandled intercept message: " + messageJson);
              }
            });
        return null;
      }
    };

    new BrowserFunction(browser, "eclipse_receiveMessage") {
      @Override
      public Object function(Object[] arguments) {
        log.received(arguments[0].toString());
        if (chatId.isEmpty()) {
          log.warn("No chat ID");
          return null;
        }
        display.asyncExec(
            () -> {
              String messageJson = (String) arguments[0];
              Webview_ReceiveMessageStringEncodedParams params =
                  new Webview_ReceiveMessageStringEncodedParams();
              params.id = chatId;
              params.messageStringEncoded = messageJson;
              agent.server.webview_receiveMessageStringEncoded(params);
            });
        return null;
      }
    };

    new BrowserFunction(browser, "eclipse_logError") {
      @Override
      public Object function(Object[] arguments) {
        log.error(arguments[0].toString());
        return null;
      }
    };

    new BrowserFunction(browser, "eclipse_getToken") {
      @Override
      public Object function(Object[] arguments) {
        return tokenStorage.getActiveProfileName().map(tokenStorage::getToken).orElse("");
      }
    };
  }

  private IToolBarManager addActionToToolbar(Action action) {
    var toolBar = getViewSite().getActionBars().getToolBarManager();
    toolBar.add(action);
    return toolBar;
  }

  private void addLogInAction() {
    var action =
        new Action() {
          @Override
          public void run() {
            try {
              page.showView(TokenSelectionView.ID);
            } catch (PartInitException e) {
              log.error("Cannot open token selection view", e);
            }
          }
        };

    action.setText("Open Cody Settings");
    action.setToolTipText("Open Cody Settings");
    action.setImageDescriptor(
        PlatformUI.getWorkbench()
            .getSharedImages()
            .getImageDescriptor(ISharedImages.IMG_TOOL_NEW_WIZARD));
    addActionToToolbar(action);
  }

  private void addStartNewChatAction() {
    var action =
        new Action() {
          @Override
          public void run() {
            if (browserField != null) {
              onStartNewChat(browserField);
            }
          }
        };

    action.setText("Start new chat");
    action.setToolTipText("Start new chat session");
    action.setImageDescriptor(
        PlatformUI.getWorkbench()
            .getSharedImages()
            .getImageDescriptor(ISharedImages.IMG_ETOOL_CLEAR));
    addActionToToolbar(action).update(true);
  }

  private void addRestartCodyAction() {
    if (!"true".equals(System.getProperty("cody-agent.restart-button", "false"))) {
      return;
    }
    var action =
        new Action() {
          @Override
          public void run() {
            manager.withAgent(CodyAgent::dispose);
            manager.withAgent(a -> log.info("Agent restarted successfully"));
          }
        };

    action.setText("Restart Cody");
    action.setToolTipText("Restart Cody Agent");
    action.setImageDescriptor(
        PlatformUI.getWorkbench()
            .getSharedImages()
            .getImageDescriptor(ISharedImages.IMG_ELCL_SYNCED));

    addActionToToolbar(action);
  }

  @Override
  public void setFocus() {}
}
