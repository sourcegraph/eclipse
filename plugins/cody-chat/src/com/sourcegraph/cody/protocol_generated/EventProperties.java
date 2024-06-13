package com.sourcegraph.cody.protocol_generated;

public final class EventProperties {
  public String anonymousUserID;
  public String prefix;
  public String client;
  public SourceEnum source; // Oneof: IDEEXTENSION

  public enum SourceEnum {
    @com.google.gson.annotations.SerializedName("IDEEXTENSION")
    IDEEXTENSION,
  }
}
