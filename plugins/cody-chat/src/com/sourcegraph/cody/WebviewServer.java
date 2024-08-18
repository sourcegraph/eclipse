package com.sourcegraph.cody;

import com.sourcegraph.cody.chat.agent.CodyManager;
import com.sourcegraph.cody.chat.agent.Disposable;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.Callback;

public class WebviewServer implements Disposable {

  private final Server server;
  private final ServerConnector connector;
  private final CodyManager codyManager;
  ;
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

  public WebviewServer(CodyManager codyManager) {
    this.codyManager = codyManager;
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
              path = "index.html";
            }
            var resource = codyManager.getResources().loadWebviewBytes(path);
            String extension = getFileExtension(path);
            String mimeType = MIME_TYPES.getOrDefault(extension, "application/octet-stream");
            response.getHeaders().add("Content-Type", mimeType);
            response.write(true, ByteBuffer.wrap(resource), callback);
            response.setStatus(200);
            return true;
          }
        });
  }

  private String getFileExtension(String path) {
    int lastIndexOfDot = path.lastIndexOf('.');
    if (lastIndexOfDot == -1) {
      return ""; // empty extension
    }
    return path.substring(lastIndexOfDot + 1);
  }

  public int start() throws Exception {
    this.server.start();
    return this.connector.getLocalPort();
  }

  @Override
  public void dispose() {
    try {
      this.server.stop();
    } catch (Exception e) {
      throw new WrappedRuntimeException(e);
    } finally {
      codyManager.webserverDisposed();
    }
  }
}
