package com.sourcegraph.cody.webview_protocol;

public enum CodyIDE {
  @com.google.gson.annotations.SerializedName("VSCode")
  VSCode,
  @com.google.gson.annotations.SerializedName("JetBrains")
  JetBrains,
  @com.google.gson.annotations.SerializedName("Neovim")
  Neovim,
  @com.google.gson.annotations.SerializedName("Emacs")
  Emacs,
  @com.google.gson.annotations.SerializedName("Web")
  Web,
}
