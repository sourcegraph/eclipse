package com.sourcegraph.cody.protocol_generated;

import com.google.gson.JsonDeserializer;

interface WorkspaceEditOperation {
  static JsonDeserializer<WorkspaceEditOperation> deserializer() {
    return (element, _type, context) -> {
      switch (element.getAsJsonObject().get("type").getAsString()) {
        case "create-file":
          return context.deserialize(element, CreateFileOperation.class);
        case "rename-file":
          return context.deserialize(element, RenameFileOperation.class);
        case "delete-file":
          return context.deserialize(element, DeleteFileOperation.class);
        case "edit-file":
          return context.deserialize(element, EditFileOperation.class);
        default:
          throw new RuntimeException("Unknown discriminator " + element);
      }
    };
  }

  public final class CreateFileOperation implements WorkspaceEditOperation {
    public TypeEnum type; // Oneof: create-file
    public String uri;
    public WriteFileOptions options;
    public String textContents;
    public WorkspaceEditEntryMetadata metadata;

    public enum TypeEnum {
      @com.google.gson.annotations.SerializedName("create-file")
      Create_file,
    }
  }

  public final class RenameFileOperation implements WorkspaceEditOperation {
    public TypeEnum type; // Oneof: rename-file
    public String oldUri;
    public String newUri;
    public WriteFileOptions options;
    public WorkspaceEditEntryMetadata metadata;

    public enum TypeEnum {
      @com.google.gson.annotations.SerializedName("rename-file")
      Rename_file,
    }
  }

  public final class DeleteFileOperation implements WorkspaceEditOperation {
    public TypeEnum type; // Oneof: delete-file
    public String uri;
    public DeleteOptionsParams deleteOptions;
    public WorkspaceEditEntryMetadata metadata;

    public enum TypeEnum {
      @com.google.gson.annotations.SerializedName("delete-file")
      Delete_file,
    }
  }

  public final class EditFileOperation implements WorkspaceEditOperation {
    public TypeEnum type; // Oneof: edit-file
    public String uri;
    public java.util.List<TextEdit> edits;

    public enum TypeEnum {
      @com.google.gson.annotations.SerializedName("edit-file")
      Edit_file,
    }
  }
}
