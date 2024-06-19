package com.sourcegraph.cody;

public final class MessageOnlyException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public MessageOnlyException(String message, Exception cause) {
    super(message, cause);
  }

  @Override
  public synchronized Throwable fillInStackTrace() {
    return this;
  }
}
