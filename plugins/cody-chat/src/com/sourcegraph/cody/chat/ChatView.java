package com.sourcegraph.cody.chat;

import static java.lang.System.out;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sourcegraph.cody.CodyAgent;
import com.sourcegraph.cody.chat.access.LogInJob;
import com.sourcegraph.cody.chat.access.TokenStorage;
import com.sourcegraph.cody.protocol_generated.ExtensionMessage;
import com.sourcegraph.cody.protocol_generated.ProtocolTypeAdapters;
import com.sourcegraph.cody.protocol_generated.WebviewMessage;
import jakarta.inject.Inject;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

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

  private Gson gson = ChatView.createGson();

  private static Gson createGson() {
    GsonBuilder builder = new GsonBuilder();
    ProtocolTypeAdapters.register(builder);
    return builder.create();
  }

  private void doSendExtensionMessage(Browser browser, ExtensionMessage message) {
    browser.execute("eclipse_receiveMessage(" + gson.toJson(gson.toJson(message)) + ");");
  }

  @Override
  public void createPartControl(Composite parent) {
    addLogInAction();
    addRestartCodyAction();
    AtomicReference<Browser> browser = new AtomicReference<>();
    ArrayList<ExtensionMessage> pendingExtensionMessages = new ArrayList<>();
    CodyAgent.CLIENT.extensionMessageConsumer =
        (message) -> {
          System.out.println("WEBVIEW/RECEIVE_MESSAGE");
          if (browser.get() != null && pendingExtensionMessages.isEmpty()) {
            doSendExtensionMessage(browser.get(), message);
          } else {
            // TODO: implement proper queue so we get FIFO ordering
            pendingExtensionMessages.add(message);
          }
        };

    CodyAgent agent = CodyAgent.start();
    var b = new Browser(parent, SWT.EDGE);
    browser.set(b);
    createCallbacks(browser.get(), agent);
    try {
      System.out.println("CHAT/NEW");
      agent.server.grosshacks_chat_new(null).get(5, TimeUnit.SECONDS);
      System.out.println("DONE - CHAT/NEW");
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    try {

      for (var message : pendingExtensionMessages) {
        doSendExtensionMessage(browser.get(), message);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    //    browser.setText(loadIndex());
    var text = loadCodyIndex();
	// out.println("HTMLTEXT " + text);
//    browser.get().setText(text);
    browser.get().setUrl("http://localhost:8000/cody-index.html");
  }

  private void createCallbacks(Browser browser, CodyAgent agent) {
    new BrowserFunction(browser, "eclipse_postMessage") {
      @Override
      public Object function(Object[] arguments) {
        System.out.println("SERVER - eclipse_postMessage: " + Arrays.asList(arguments));
        display.asyncExec(
            () -> {
              WebviewMessage parsedObject =
                  gson.fromJson((String) arguments[0], WebviewMessage.class);
              System.out.println("From webview: " + parsedObject);
              agent.server.grosshacks_webview_postMessageClientToServer(parsedObject);
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

  private void addRestartCodyAction() {
    var action =
        new Action() {
          @Override
          public void run() {
            try {
              CodyAgent.restart();
            } catch (IOException e) {
              // TODO Auto-generated catch block
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

  public String loadIndex() {
    return loadResource("/resources/index.html");
  }

  private String loadCodyIndex() {
    String content = loadResource("/resources/cody-webviews/index.html");

    return content
        .replace("{cspSource}", "'self' https://*.sourcegraphstatic.com")
        .replace(
            "<head>",
            String.format(
                "<head><script>%s</script><style>%s</style>", loadInjectedJS(), loadInjectedCSS()));
  }

  private String loadInjectedJS() {
    return loadResource("/resources/injected-script.js");
  }

  private String loadInjectedCSS() {
    return loadResource("/resources/injected-styles.css");
  }

  private String loadResource(String path) {
    try (var stream = getClass().getResourceAsStream(path)) {
      return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      e.printStackTrace();
      return "<h1> Cannot load index.html </h1>";
    }
  }
}
