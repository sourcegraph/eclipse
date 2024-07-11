package com.sourcegraph.cody.handlers;

import com.sourcegraph.cody.protocol_generated.WebviewMessage;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.di.annotations.Creatable;

import java.util.HashMap;
import java.util.Map;

import static java.lang.System.out;

@Creatable
@Singleton
public class MessageHandlers {
	static final String EXTENSION_ID = "com.sourcegraph.cody.messageHandler";

    private Map<Class<?>, MessageHandler<?>> map;

    public MessageHandlers() {
    	var registry = Platform.getExtensionRegistry();
        this.map = new HashMap<>();
        for (var config: registry.getConfigurationElementsFor(EXTENSION_ID)) {
            try {
                var msgType = Class.forName(config.getAttribute("message"));
                var impl = (MessageHandler<?>) config.createExecutableExtension("implementation");
                map.put(msgType, impl);
            } catch (CoreException | ClassNotFoundException e) {
                out.println("Cannot create message handler");
                e.printStackTrace();
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
