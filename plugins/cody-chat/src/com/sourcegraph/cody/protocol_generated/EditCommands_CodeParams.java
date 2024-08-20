package com.sourcegraph.cody.protocol_generated;

public final class EditCommands_CodeParams {
  public String instruction;
  public String model;
  public ModeEnum mode; // Oneof: edit, insert
  public Range range;

  public enum ModeEnum {
    @com.google.gson.annotations.SerializedName("edit")
    Edit,
    @com.google.gson.annotations.SerializedName("insert")
    Insert,
  }
}
