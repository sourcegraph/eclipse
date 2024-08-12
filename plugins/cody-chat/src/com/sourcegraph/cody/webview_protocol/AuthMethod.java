package com.sourcegraph.cody.webview_protocol;

public enum AuthMethod {
  @com.google.gson.annotations.SerializedName("dotcom")
  Dotcom,
  @com.google.gson.annotations.SerializedName("github")
  Github,
  @com.google.gson.annotations.SerializedName("gitlab")
  Gitlab,
  @com.google.gson.annotations.SerializedName("google")
  Google,
}
