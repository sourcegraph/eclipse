package com.sourcegraph.cody.chat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sourcegraph.cody.chat.agent.CodyAgent;
import com.sourcegraph.cody.chat.agent.CodyManager;
import com.sourcegraph.cody.handlers.MessageHandlers;
import com.sourcegraph.cody.logging.CodyLogger;
import com.sourcegraph.cody.protocol_generated.ProtocolTypeAdapters;
import com.sourcegraph.cody.protocol_generated.Webview_ReceiveMessageStringEncodedParams;
import com.sourcegraph.cody.protocol_generated.Webview_ResolveWebviewViewParams;
import com.sourcegraph.cody.webview_protocol.WebviewMessage;
import jakarta.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

@SuppressWarnings("unused")
public class ChatView extends ViewPart {
  public static final String ID = "com.sourcegraph.cody.chat.ChatView";

  @Inject private Display display;

  @Inject private MessageHandlers handlers;

  @Inject private CodyManager manager;

  private final CodyLogger log = new CodyLogger(getClass());

  private Browser browserField;
  private volatile String chatId = "";
  private CompletableFuture<Boolean> webviewInitialized = new CompletableFuture<Boolean>();
  private final ConcurrentLinkedQueue<String> pendingExtensionMessages =
      new ConcurrentLinkedQueue<>();

  public static Gson gson = createGson();
  private Consumer<String> webviewMessagesConsumer;

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
      addRestartCodyAction();
      addWebview(parent);
    } catch (Exception e) {
      log.error("Cannot create chat view", e);
    }
  }

  public void addWebview(Composite parent) {
    final Browser browser = new Browser(parent, SWT.EDGE);
    this.browserField = browser;
    manager.withAgent(agent -> newWebview(browser, agent));
  }

  private void newWebview(Browser browser, CodyAgent agent) {
    connectWebviewToBrowser(browser);
    onStartNewChat(browser, agent);
  }

  private void doPostMessage(Browser browser, String message) {
    display.asyncExec(
        () -> {
          String stringifiedMessage = gson.toJson(message);
          // Only log non-transcript messages
          if (!stringifiedMessage.contains("\\\"type\\\":\\\"transcript\\\"")) {
            log.sent(stringifiedMessage);
          }
          browser.execute("eclipse_postMessage(" + stringifiedMessage + ");");
        });
  }

  private void connectWebviewToBrowser(Browser browser) {
    pendingExtensionMessages.clear();
    webviewInitialized = new CompletableFuture<>();
    webviewInitialized.thenRun(() -> flushPendingMessages(browser));
    Consumer<String> oldConsumer = this.webviewMessagesConsumer;
    this.webviewMessagesConsumer =
        message -> {
          if (webviewInitialized.isDone() && pendingExtensionMessages.isEmpty()) {
            doPostMessage(browser, message);
          } else {
            pendingExtensionMessages.add(message);
          }
        };
    if (oldConsumer != null) {
      manager.webviewConsumer.removeListener(oldConsumer);
    }
    manager.webviewConsumer.addListener(webviewMessagesConsumer);
  }

  private void onStartNewChat(Browser browser, CodyAgent agent) {
    try {
      // Resolve the native webview with the pre-defined view IDs.
      chatId = "eclipse-sidebar";
      Webview_ResolveWebviewViewParams viewParams = new Webview_ResolveWebviewViewParams();
      viewParams.viewId = "cody.chat";
      viewParams.webviewHandle = chatId;
      agent.server.webview_resolveWebviewView(viewParams).get(5, TimeUnit.SECONDS);

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
  }

  private void addActionToToolbar(Action action) {
    getViewSite().getActionBars().getToolBarManager().add(action);
  }

  private void addRestartCodyAction() {
    // This is disabled by default because it doesn't work correctly. When you restart, the
    // webview gets stuck in a loading state.
    if (!"true".equals(System.getProperty("cody-agent.restart-button", "false"))) {
      return;
    }
    var action =
        new Action() {
          @Override
          public void run() {
            manager.withAgent(CodyAgent::dispose);
            manager.withAgent(
                agent -> {
                  log.info("Agent restarted successfully");
                  browserField.refresh();
                  newWebview(browserField, agent);
                });
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
