package com.sourcegraph.cody;

/** Same as RuntimException but without a stack trace. */
public final class WrappedRuntimeException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public WrappedRuntimeException(Exception cause) {
    super(cause);
  }

  @Override
  public synchronized Throwable fillInStackTrace() {
    return this;
  }
}
