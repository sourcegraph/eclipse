package com.sourcegraph.cody.protocol_generated;

public final class WebviewNativeConfig {
  public ViewEnum view; // Oneof: multiple, single
  public String cspSource;
  public AssetLoaderEnum assetLoader; // Oneof: fs, webviewasset
  public String webviewBundleServingPrefix;
  public String rootDir;
  public String injectScript;
  public String injectStyle;

  public enum ViewEnum {
    @com.google.gson.annotations.SerializedName("multiple")
    Multiple,
    @com.google.gson.annotations.SerializedName("single")
    Single,
  }

  public enum AssetLoaderEnum {
    @com.google.gson.annotations.SerializedName("fs")
    Fs,
    @com.google.gson.annotations.SerializedName("webviewasset")
    Webviewasset,
  }
}
