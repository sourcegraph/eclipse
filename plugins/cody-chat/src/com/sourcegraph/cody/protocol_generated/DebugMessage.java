package com.sourcegraph.cody.protocol_generated;

public final class DebugMessage {
  public String channel;
  public String message;
  public DebugMessageLogLevel level; // Oneof: trace, debug, info, warn, error
}
