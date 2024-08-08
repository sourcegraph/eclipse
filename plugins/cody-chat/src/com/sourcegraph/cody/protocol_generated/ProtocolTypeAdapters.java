package com.sourcegraph.cody.protocol_generated;

public final class ProtocolTypeAdapters {
  public static void register(com.google.gson.GsonBuilder gson) {
    gson.registerTypeAdapter(ContextItem.class, ContextItem.deserializer());
    gson.registerTypeAdapter(CustomCommandResult.class, CustomCommandResult.deserializer());
    gson.registerTypeAdapter(TextEdit.class, TextEdit.deserializer());
    gson.registerTypeAdapter(WorkspaceEditOperation.class, WorkspaceEditOperation.deserializer());
  }
}
