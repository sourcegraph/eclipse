package com.sourcegraph.cody.protocol_generated;

public enum CodyTaskState {
  @com.google.gson.annotations.SerializedName("Idle")
  Idle,
  @com.google.gson.annotations.SerializedName("Working")
  Working,
  @com.google.gson.annotations.SerializedName("Inserting")
  Inserting,
  @com.google.gson.annotations.SerializedName("Applying")
  Applying,
  @com.google.gson.annotations.SerializedName("Applied")
  Applied,
  @com.google.gson.annotations.SerializedName("Finished")
  Finished,
  @com.google.gson.annotations.SerializedName("Error")
  Error,
  @com.google.gson.annotations.SerializedName("Pending")
  Pending,
}
