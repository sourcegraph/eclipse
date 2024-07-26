package com.sourcegraph.cody.chat.agent;

public enum OnFailure {
  IGNORE,
  LOG,
  THROW,
  RETRY,
}
