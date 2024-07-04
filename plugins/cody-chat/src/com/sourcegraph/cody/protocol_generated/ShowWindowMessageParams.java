package com.sourcegraph.cody.protocol_generated;

public final class ShowWindowMessageParams {
  public SeverityEnum severity; // Oneof: error, warning, information
  public String message;
  public MessageOptions options;
  public java.util.List<String> items;

  public enum SeverityEnum {
    @com.google.gson.annotations.SerializedName("error")
    Error,
    @com.google.gson.annotations.SerializedName("warning")
    Warning,
    @com.google.gson.annotations.SerializedName("information")
    Information,
  }
}
