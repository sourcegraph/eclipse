package com.sourcegraph.cody.chat.access;

import com.sourcegraph.cody.logging.CodyLogger;
import jakarta.inject.Inject;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.Callback;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class LogInJob extends Job {

  private WaitingForLoginWindow window = null;

  @Inject private TokenStorage tokenStorage;

  @Inject private Display display;

  @Inject private Shell shell;

  private CodyLogger log = new CodyLogger(getClass());

  private String name;
  private String url;

  public LogInJob(IEclipseContext context, String name, String url) {
    super("Logging in...");
    this.name = name;
    this.url = url;
    ContextInjectionFactory.inject(this, context);
  }

  @Override
  protected IStatus run(IProgressMonitor monitor) {
    var server = new Server();
    var connector = new ServerConnector(server);
    server.addConnector(connector);

    var tokenSignal = new CompletableFuture<String>();

    server.setHandler(
        new Handler.Abstract() {
          @Override
          public boolean handle(Request request, Response response, Callback callback)
              throws Exception {
            var token = extractToken(request.getHttpURI());
            if (token.isPresent()) {
              tokenSignal.complete(token.get());
              // HTML copy-pasted from the JetBrains plugin. TODO: load this from resources so we
              // get nice HTML syntax highlighthing when editing this HTML.
              var html =
                  "<!DOCTYPE html><html lang=\"en\"> <head> <meta charset=\"utf-8\"> <title>Cody:"
                      + " Authentication successful</title> </head> <body style=\"font-family:"
                      + " system-ui, -apple-system, BlinkMacSystemFont, \'Segoe UI\', Roboto,"
                      + " Oxygen, Ubuntu, Cantarell, \'Open Sans\', \'Helvetica Neue\', sans-serif;"
                      + " background: #f9fafb;\"> <div style=\"margin: 40px auto; text-align:"
                      + " center; max-width: 300px; border: 1px solid #e6ebf2; padding: 40px 20px;"
                      + " border-radius: 8px; background: white; box-shadow: 0px 5px 20px 1px"
                      + " rgba(0, 0, 0, 0.1); \"> <svg width=\"32\" height=\"32\" viewBox=\"0 0 195"
                      + " 176\" fill=\"none\" xmlns=\"http://www.w3.org/2000/svg\"> <path"
                      + " fill-rule=\"evenodd\" clip-rule=\"evenodd\" d=\"M141.819"
                      + " -8.93872e-07C152.834 -4.002e-07 161.763 9.02087 161.763 20.1487L161.763"
                      + " 55.9685C161.763 67.0964 152.834 76.1172 141.819 76.1172C130.805 76.1172"
                      + " 121.876 67.0963 121.876 55.9685L121.876 20.1487C121.876 9.02087 130.805"
                      + " -1.38754e-06 141.819 -8.93872e-07Z\" fill=\"#FF5543\"/> <path"
                      + " fill-rule=\"evenodd\" clip-rule=\"evenodd\" d=\"M15.5111 47.0133C15.5111"
                      + " 35.8855 24.44 26.8646 35.4543 26.8646H70.9088C81.9231 26.8646 90.8519"
                      + " 35.8855 90.8519 47.0133C90.8519 58.1411 81.9231 67.162 70.9088"
                      + " 67.162H35.4543C24.44 67.162 15.5111 58.1411 15.5111 47.0133Z\""
                      + " fill=\"#A112FF\"/> <path fill-rule=\"evenodd\" clip-rule=\"evenodd\""
                      + " d=\"M189.482 105.669C196.58 112.482 196.868 123.818 190.125"
                      + " 130.989L183.85 137.662C134.75 189.88 51.971 188.579 4.50166"
                      + " 134.844C-2.01751 127.464 -1.38097 116.142 5.92343 109.556C13.2278 102.97"
                      + " 24.434 103.613 30.9532 110.993C64.6181 149.101 123.324 150.024 158.146"
                      + " 112.991L164.42 106.318C171.164 99.1472 182.384 98.8565 189.482 105.669Z\""
                      + " fill=\"#00CBEC\"/> </svg> <h4>Authentication successful</h4> <p"
                      + " style=\"font-size: 12px;\">You may close this tab and return to your"
                      + " editor</p> </body></html>";
              response.setStatus(200);
              response.getHeaders().add("Content-Type", "text/html");
              response.write(
                  true, ByteBuffer.wrap(html.getBytes(StandardCharsets.UTF_8)), callback);
              callback.succeeded();
            } else {
              var reason = new RuntimeException("Request " + request + " does not contain a token");
              tokenSignal.completeExceptionally(
                  reason); // propagate to kill this job and shut down the server
              callback.failed(reason); // propagate to server
            }
            return true;
          }
        });

    try {
      showWindow(tokenSignal);
      server.start();
      var port = connector.getLocalPort();

      // open login page

      var uri = URI.create(url);
      var logInUrl =
          "https://"
              + uri.getHost()
              + "/user/settings/tokens/new/callback?requestFrom=JETBRAINS-"
              + port;

      display.asyncExec(
          () -> {
            Program.launch(logInUrl);
          });

      // wait for response
      var response = tokenSignal.get();
      tokenStorage.put(name, url, response);
      tokenStorage.setActiveProfileName(name);

      return Status.OK_STATUS;
    } catch (CancellationException e) {
      return Status.CANCEL_STATUS;
    } catch (Throwable e) {
      log.error("Problem while opening a login page", e);
      return Status.CANCEL_STATUS;
    } finally {
      try {
        server.stop();
      } catch (Exception e) {
        log.error("Problem stopping login server", e);
      }
      closeWindow();
    }
  }

  private void showWindow(CompletableFuture<String> tokenSignal) {
    display.asyncExec(
        () -> {
          window =
              new WaitingForLoginWindow(
                  shell,
                  () -> {
                    tokenSignal.cancel(true);
                  });
          window.open();
        });
  }

  private void closeWindow() {
    display.asyncExec(
        () -> {
          if (window != null) {
            window.close();
          }
        });
  }

  private Optional<String> extractToken(HttpURI httpURI) {
    var matcher = Pattern.compile("token=([^&]*)").matcher(httpURI.getQuery());
    if (matcher.find()) {
      return Optional.of(matcher.group(1));
    } else {
      return Optional.empty();
    }
  }
}
