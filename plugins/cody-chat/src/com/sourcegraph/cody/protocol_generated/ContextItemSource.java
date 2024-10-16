package com.sourcegraph.cody.protocol_generated;

public enum ContextItemSource {
  @com.google.gson.annotations.SerializedName("user")
  User,
  @com.google.gson.annotations.SerializedName("editor")
  Editor,
  @com.google.gson.annotations.SerializedName("search")
  Search,
  @com.google.gson.annotations.SerializedName("initial")
  Initial,
  @com.google.gson.annotations.SerializedName("priority")
  Priority,
  @com.google.gson.annotations.SerializedName("unified")
  Unified,
  @com.google.gson.annotations.SerializedName("selection")
  Selection,
  @com.google.gson.annotations.SerializedName("terminal")
  Terminal,
  @com.google.gson.annotations.SerializedName("history")
  History,
  @com.google.gson.annotations.SerializedName("agentic")
  Agentic,
}
