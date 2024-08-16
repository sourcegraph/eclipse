package com.sourcegraph.cody.protocol_generated;

public final class WebviewNativeConfigParams {
  public ViewEnum view; // Oneof: multiple, single
  public String cspSource;
  public String webviewBundleServingPrefix;

  public enum ViewEnum {
    @com.google.gson.annotations.SerializedName("multiple")
    Multiple,
    @com.google.gson.annotations.SerializedName("single")
    Single,
  }
}
