package com.sourcegraph.cody.chat.agent;

import com.sourcegraph.cody.logging.CodyLogger;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** A consumer that broadcasts events to multiple listeners. */
public class MultiConsumer<T> implements Consumer<T> {
  private final CodyLogger log = new CodyLogger(getClass());
  private final List<Consumer<T>> listeners = new ArrayList<>();

  public void addListener(Consumer<T> listener) {
    listeners.add(listener);
  }

  public void removeListener(Consumer<T> listener) {
    listeners.remove(listener);
  }

  @Override
  public void accept(T event) {
    for (Consumer<T> listener : listeners) {
      try {
        listener.accept(event);
      } catch (Exception e) {
        log.error(String.format("Error in listener %s accepting event %s", listener, event), e);
      }
    }
  }
}
