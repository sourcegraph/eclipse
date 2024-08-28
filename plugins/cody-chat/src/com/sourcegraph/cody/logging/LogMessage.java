package com.sourcegraph.cody.logging;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogMessage {
  public static enum Kind {
    ERROR,
    WARNING,
    INFO,
  }

  public final Kind kind;
  public final String message;
  public final LocalDateTime timestamp;

  public LogMessage(Kind kind, String message, LocalDateTime timestamp) {
    this.kind = kind;
    this.message = message.replace("\n", "").replace("\\n", "");
    this.timestamp = timestamp;
  }

  @Override
  public String toString() {
    return String.format(
        "[%s] %s: %s", DateTimeFormatter.ISO_LOCAL_TIME.format(timestamp), kind.name(), message);
  }
}
