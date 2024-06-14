package com.sourcegraph.cody.chat.access;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.DefaultHandler;

public class StaticFileServer {

    private final Server server;

    public StaticFileServer(int port, String resourceBase) {
        server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(port);
        server.addConnector(connector);

        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setDirectoriesListed(true);
        resourceHandler.setWelcomeFiles(new String[]{"index.html"});
        resourceHandler.setResourceBase(resourceBase);

        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new org.eclipse.jetty.server.Handler[]{resourceHandler, new DefaultHandler()});
        server.setHandler(handlers);
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

        int port = Integer.parseInt(args[0]);
        String resourceBase = args[1];

        StaticFileServer staticFileServer = new StaticFileServer(port, resourceBase);
        try {
            staticFileServer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
