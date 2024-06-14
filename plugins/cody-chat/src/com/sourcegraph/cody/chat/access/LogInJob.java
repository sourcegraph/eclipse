package com.sourcegraph.cody.chat.access;

import jakarta.inject.Inject;
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

import jakarta.inject.Inject;

public class LogInJob extends Job {

  private static final String ATTRIB = "?requestFrom=JETBRAINS-";

  private WaitingForLoginWindow window = null;

  @Inject private TokenStorage tokenStorage;

  @Inject private Display display;

  @Inject private Shell shell;

  private String name;
  private String url;

  public LogInJob(IEclipseContext context, String name, String url) {
    super("Loging in...");
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

      var logInUrl = url + ATTRIB + port;

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
      e.printStackTrace();
      return Status.CANCEL_STATUS;
    } finally {
      try {
        server.stop();
      } catch (Exception e) {
        e.printStackTrace();
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
