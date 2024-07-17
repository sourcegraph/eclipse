package com.sourcegraph.cody.handlers;

import com.sourcegraph.cody.protocol_generated.WebviewMessage;

public interface MessageHandler<T extends WebviewMessage> {

  void doHandle(T message);

  @SuppressWarnings("unchecked")
  default void handle(WebviewMessage message) {
    doHandle((T) message);
  }
}
