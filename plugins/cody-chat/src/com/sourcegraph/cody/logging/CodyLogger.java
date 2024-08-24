package com.sourcegraph.cody.logging;

import com.sourcegraph.cody.protocol_generated.ExtensionConfiguration;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.FrameworkUtil;

public class CodyLogger {

  private static final int MAX_SIZE = 1024 * 1024 * 20; // 20MB of text

  static final Internal INSTANCE = new Internal();

  private final ILog delegate;

  public CodyLogger(Class<?> clazz) {
    this.delegate = Platform.getLog(clazz);
  }

  public static void onConfigChange(ExtensionConfiguration config) {
    INSTANCE.setConnectedInstance(config.serverEndpoint);
  }

  public void error(String message) {
    delegate.error(message);
    INSTANCE.log(new LogMessage(LogMessage.Kind.ERROR, message, LocalDateTime.now()));
  }

  public void error(String message, Throwable t) {
    var stackTrace = new PrintWriter(new StringWriter());
    t.printStackTrace(stackTrace);
    delegate.error(message, t);
    INSTANCE.log(
        new LogMessage(LogMessage.Kind.ERROR, message + "\n" + stackTrace, LocalDateTime.now()));
  }

  public void warn(String message) {
    delegate.warn(message);
    INSTANCE.log(new LogMessage(LogMessage.Kind.WARNING, message, LocalDateTime.now()));
  }

  public void warn(String message, Throwable t) {
    var stackTrace = new PrintWriter(new StringWriter());
    t.printStackTrace(stackTrace);
    delegate.warn(message, t);
    INSTANCE.log(
        new LogMessage(LogMessage.Kind.WARNING, message + "\n" + stackTrace, LocalDateTime.now()));
  }

  public void info(String message) {
    delegate.info(message);
    INSTANCE.log(new LogMessage(LogMessage.Kind.INFO, message, LocalDateTime.now()));
  }

  public void received(String message) {
    INSTANCE.log(new LogMessage(LogMessage.Kind.RECEIVED, message, LocalDateTime.now()));
  }

  public void sent(String message) {
    INSTANCE.log(new LogMessage(LogMessage.Kind.SENT, message, LocalDateTime.now()));
  }

  static class Internal {

    // plugin version, os, architecture and similar info
    final List<LogMessage> environment = new ArrayList<>();

    // circular buffer of actual log messages, capped at MAX_SIZE of text
    final Deque<LogMessage> backlog = new java.util.ArrayDeque<>();

    private final Set<Consumer<LogMessage>> listeners = new HashSet<>();

    private int currentSize = 0;

    private String connectedInstance;
    private static final String CONNECTED_INSTANCE_PREFIX = "Connected to: ";

    Internal() {
      var bundle = FrameworkUtil.getBundle(getClass());
      var version = bundle.getVersion().toString();
      environment.add(
          new LogMessage(LogMessage.Kind.INFO, "Plugin version: " + version, LocalDateTime.now()));

      var java =
          String.format(
              "%s %s", System.getProperty("java.vendor"), System.getProperty("java.version"));
      environment.add(new LogMessage(LogMessage.Kind.INFO, java, LocalDateTime.now()));

      var system =
          String.format(
              "%s %s (%s)",
              System.getProperty("os.name"),
              System.getProperty("os.version"),
              System.getProperty("os.arch"));
      environment.add(new LogMessage(LogMessage.Kind.INFO, system, LocalDateTime.now()));

      addConnectedInstanceMsg();
    }

    private void setConnectedInstance(String instance) {
      this.connectedInstance = instance;
      for (var msg : environment) {
        if (msg.message.startsWith(CONNECTED_INSTANCE_PREFIX)) {
          environment.remove(msg);
          break;
        }
      }
      addConnectedInstanceMsg();
    }

    private void addConnectedInstanceMsg() {
      if (connectedInstance == null) {
        return;
      }
      var msg = CONNECTED_INSTANCE_PREFIX + connectedInstance;
      environment.add(new LogMessage(LogMessage.Kind.INFO, msg, LocalDateTime.now()));
    }

    public void log(LogMessage message) {
      backlog.addLast(message);
      currentSize += message.message.length();
      while (currentSize > MAX_SIZE) {
        currentSize -= backlog.removeFirst().message.length();
      }
      for (var listener : listeners) {
        listener.accept(message);
      }
    }

    public void clear() {
      backlog.clear();
      currentSize = 0;
    }

    void addListener(Consumer<LogMessage> listener) {
      listeners.add(listener);
    }

    void removeListener(Consumer<LogMessage> listener) {
      listeners.remove(listener);
    }
  }
}
