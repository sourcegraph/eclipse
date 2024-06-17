package com.sourcegraph.cody.protocol_generated;

import com.google.gson.JsonDeserializer;

interface CustomCommandResult {
  static JsonDeserializer<CustomCommandResult> deserializer() {
    return (element, _type, context) -> {
      switch (element.getAsJsonObject().get("type").getAsString()) {
        case "chat":
          return context.deserialize(element, CustomChatCommandResult.class);
        case "edit":
          return context.deserialize(element, CustomEditCommandResult.class);
        default:
          throw new RuntimeException("Unknown discriminator " + element);
      }
    };
  }

  public final class CustomChatCommandResult implements CustomCommandResult {
    public TypeEnum type; // Oneof: chat
    public String chatResult;

    public enum TypeEnum {
      @com.google.gson.annotations.SerializedName("chat")
      Chat,
    }
  }

  public final class CustomEditCommandResult implements CustomCommandResult {
    public TypeEnum type; // Oneof: edit
    public EditTask editResult;

    public enum TypeEnum {
      @com.google.gson.annotations.SerializedName("edit")
      Edit,
    }
  }
}
