package com.sourcegraph.cody.protocol_generated;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import java.lang.reflect.Type;

public abstract class CustomCommandResult {
    public static JsonDeserializer<CustomCommandResult> deserializer() {
      return (element, _type, context) -> {
        switch (element.getAsJsonObject().get("type").getAsString()) {
          case "chat": return context.deserialize(element, CustomChatCommandResult.class);
          case "edit": return context.deserialize(element, CustomEditCommandResult.class);
          default: throw new RuntimeException("Unknown discriminator " + element);
        }
      };
  }

public static final class CustomChatCommandResult extends CustomCommandResult {
  public TypeEnum type; // Oneof: chat
  public String chatResult;

  public enum TypeEnum {
    @com.google.gson.annotations.SerializedName("chat") Chat,
  }
}

public static final class CustomEditCommandResult extends CustomCommandResult {
  public TypeEnum type; // Oneof: edit
  public EditTask editResult;

  public enum TypeEnum {
    @com.google.gson.annotations.SerializedName("edit") Edit,
  }
}
}

