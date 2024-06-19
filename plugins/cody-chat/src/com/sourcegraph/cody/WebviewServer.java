package com.sourcegraph.cody;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.Callback;

public class WebviewServer {

  private final Server server;
  private final ServerConnector connector;

  private static final Map<String, String> MIME_TYPES = new HashMap<>();

  static {
    MIME_TYPES.put("js", "application/javascript");
    MIME_TYPES.put("css", "text/css");
    MIME_TYPES.put("html", "text/html");
    MIME_TYPES.put("png", "image/png");
    MIME_TYPES.put("svg", "image/svg+xml");
    MIME_TYPES.put("jpg", "image/jpeg");
    MIME_TYPES.put("jpeg", "image/jpeg");
    MIME_TYPES.put("gif", "image/gif");
    MIME_TYPES.put("ico", "image/x-icon");

    MIME_TYPES.put("ttf", "application/unknown");
  }

  public WebviewServer() {
    this.server = new Server();
    this.connector = new ServerConnector(server);
    connector.setPort(0);
    server.addConnector(connector);
    server.setHandler(
        new Handler.Abstract() {
          @Override
          public boolean handle(Request request, Response response, Callback callback)
              throws Exception {
            var path = request.getHttpURI().getPath();
            if (path.isEmpty() || path.equals("/")) {
              path = "/index.html";
            }
            var resource = this.getClass().getResourceAsStream("/resources/cody-webviews" + path);
            if (resource != null) {
              String extension = getFileExtension(path);
              String mimeType = MIME_TYPES.getOrDefault(extension, "application/octet-stream");
              response.getHeaders().add("Content-Type", mimeType);
              var bytes = resource.readAllBytes();
              if (path.endsWith("/index.html")) {
                bytes = WebviewServer.postProcessIndexHtml(bytes);
              }
              response.write(true, ByteBuffer.wrap(bytes), callback);
              response.setStatus(200);
              return true;
            }
            return false;
          }
        });
  }

  private static int indexOrOrCrash(String string, String substring) {
    var index = string.indexOf(substring);
    if (index < 0) {
      throw new IllegalArgumentException(
          String.format("substring '%s' does not exist in string '%s'", substring, string));
    }
    return index;
  }

  private static byte[] postProcessIndexHtml(byte[] bytes) {
    var html = new String(bytes, StandardCharsets.UTF_8);
    var start =
        WebviewServer.indexOrOrCrash(
            html,
            "<!-- This content security policy also implicitly disables inline scripts and styles."
                + " -->");
    var endMarker = "<!-- DO NOT REMOVE: THIS FILE IS THE ENTRY FILE FOR CODY WEBVIEW -->";
    var end = WebviewServer.indexOrOrCrash(html, endMarker);
    html = html.substring(0, start) + html.substring(end + endMarker.length() + 1);
    html = html.replace("{cspSource}", "'self' https://*.sourcegraphstatic.com");
    html =
        html.replace(
            "<head>",
            String.format(
                "<head><script>%s</script><style>%s</style>",
                CodyResources.loadInjectedJS(), CodyResources.loadInjectedCSS()));
    return html.getBytes(StandardCharsets.UTF_8);
  }

  private String getFileExtension(String path) {
    int lastIndexOfDot = path.lastIndexOf('.');
    if (lastIndexOfDot == -1) {
      return ""; // empty extension
    }
    return path.substring(lastIndexOfDot + 1);
  }

  public int start() {
    try {
      var port = new CompletableFuture<Integer>();
      CodyAgent.executorService.execute(
          () -> {
            try {
              this.server.start();
            } catch (Exception e) {
              e.printStackTrace();
              port.completeExceptionally(e);
            }
            port.complete(this.connector.getLocalPort());
          });
      var result = port.get(5, TimeUnit.SECONDS);
      return result;
    } catch (Exception e) {
      throw new WrappedRuntimeException(e);
    }
  }

  public void stop() {
    try {
      this.server.stop();
    } catch (Exception e) {
      throw new WrappedRuntimeException(e);
    }
  }
}
