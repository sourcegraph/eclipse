package com.sourcegraph.cody.webview_protocol;

public enum ChatSubmitType {
  @com.google.gson.annotations.SerializedName("user")
  User,
  @com.google.gson.annotations.SerializedName("user-newchat")
  User_newchat,
}
