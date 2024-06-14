package com.sourcegraph.cody.chat;

import java.nio.file.Paths;

import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.util.Callback;

public class StaticFileServer {

  private final Server server;
  private final ServerConnector connector;

  public StaticFileServer(String resourceBase) {
    this.server = new Server();
    this.connector = new ServerConnector(server);
    connector.setPort(0);
    server.addConnector(connector);
    server.setHandler(
        new Handler.Abstract() {
          @Override
          public boolean handle(Request request, Response response, Callback callback)
              throws Exception {
            System.out.println("request: " + request.getHttpURI());
            return false;
          }
        });
  }

  public int port() {
    return connector.getLocalPort();
  }

  public void start() throws Exception {
    server.start();
    server.join();
  }

  public void stop() throws Exception {
    server.stop();
  }

  public static void main(String[] args) {
    if (args.length != 2) {
      System.err.println("Usage: java StaticFileServer <port> <resourceBase>");
      System.exit(1);
    }

    String resourceBase = args[1];

    StaticFileServer staticFileServer = new StaticFileServer(resourceBase);
    try {
      staticFileServer.start();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
