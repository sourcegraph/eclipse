package com.sourcegraph.cody.handlers;

import com.sourcegraph.cody.logging.CodyLogger;
import com.sourcegraph.cody.webview_protocol.WebviewMessage;
import jakarta.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.di.annotations.Creatable;

@Creatable
@Singleton
public class MessageHandlers {
  static final String EXTENSION_ID = "com.sourcegraph.cody.messageHandler";

  private final Map<Class<?>, MessageHandler<?>> map;

  private CodyLogger log = new CodyLogger(getClass());

  public MessageHandlers() {
    var registry = Platform.getExtensionRegistry();
    this.map = new HashMap<>();
    for (var config : registry.getConfigurationElementsFor(EXTENSION_ID)) {
      try {
        var msgType = Class.forName(config.getAttribute("message"));
        var impl = (MessageHandler<?>) config.createExecutableExtension("implementation");
        map.put(msgType, impl);
      } catch (CoreException | ClassNotFoundException e) {
        log.error("Cannot create message handler", e);
      }
    }
  }

  public boolean handle(WebviewMessage msg) {
    var handler = map.get(msg.getClass());
    if (handler == null) {
      return false;
    }

    handler.handle(msg);
    return true;
  }
}
