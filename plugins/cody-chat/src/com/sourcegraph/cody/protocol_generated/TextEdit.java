package com.sourcegraph.cody.protocol_generated;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import java.lang.reflect.Type;

public abstract class TextEdit {
    public static JsonDeserializer<TextEdit> deserializer() {
      return (element, _type, context) -> {
        switch (element.getAsJsonObject().get("type").getAsString()) {
          case "replace": return context.deserialize(element, ReplaceTextEdit.class);
          case "insert": return context.deserialize(element, InsertTextEdit.class);
          case "delete": return context.deserialize(element, DeleteTextEdit.class);
          default: throw new RuntimeException("Unknown discriminator " + element);
        }
      };
  }

public final class ReplaceTextEdit extends TextEdit {
  public TypeEnum type; // Oneof: replace
  public Range range;
  public String value;
  public WorkspaceEditEntryMetadata metadata;

  public enum TypeEnum {
    @com.google.gson.annotations.SerializedName("replace") Replace,
  }
}

public final class InsertTextEdit extends TextEdit {
  public TypeEnum type; // Oneof: insert
  public Position position;
  public String value;
  public WorkspaceEditEntryMetadata metadata;

  public enum TypeEnum {
    @com.google.gson.annotations.SerializedName("insert") Insert,
  }
}

public final class DeleteTextEdit extends TextEdit {
  public TypeEnum type; // Oneof: delete
  public Range range;
  public WorkspaceEditEntryMetadata metadata;

  public enum TypeEnum {
    @com.google.gson.annotations.SerializedName("delete") Delete,
  }
}
}

