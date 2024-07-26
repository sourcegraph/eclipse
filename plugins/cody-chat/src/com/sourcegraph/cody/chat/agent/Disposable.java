package com.sourcegraph.cody.chat.agent;

/**
 * Interface for marking things as disposable.
 *
 * <p>Eclipse don't have any general purpose tools for disposable management. In the future we may
 * try to recreate the IntelliJ's Disposers.
 */
public interface Disposable {
  void dispose();
}
