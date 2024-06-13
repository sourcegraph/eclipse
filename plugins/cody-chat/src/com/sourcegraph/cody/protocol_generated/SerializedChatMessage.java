package com.sourcegraph.cody.protocol_generated;

public final class SerializedChatMessage {
  public java.util.List<ContextItem> contextFiles;
  public ChatError error;
  public Object editorState;
  public SpeakerEnum speaker; // Oneof: human, assistant, system
  public String text;
  public String model;

  public enum SpeakerEnum {
    @com.google.gson.annotations.SerializedName("human")
    Human,
    @com.google.gson.annotations.SerializedName("assistant")
    Assistant,
    @com.google.gson.annotations.SerializedName("system")
    System,
  }
}
