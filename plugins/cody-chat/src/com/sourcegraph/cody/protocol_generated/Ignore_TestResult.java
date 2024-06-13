package com.sourcegraph.cody.protocol_generated;

public final class Ignore_TestResult {
  public PolicyEnum policy; // Oneof: ignore, use

  public enum PolicyEnum {
    @com.google.gson.annotations.SerializedName("ignore") Ignore,
    @com.google.gson.annotations.SerializedName("use") Use,
  }
}

