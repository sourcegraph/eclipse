package com.sourcegraph.cody.workspace;

import com.sourcegraph.cody.chat.agent.Disposable;

public interface CodyListener extends Disposable {

  /**
   * Installs the listener on appropriate model element and sends the initial state as a
   * notification.
   */
  void install();
}
