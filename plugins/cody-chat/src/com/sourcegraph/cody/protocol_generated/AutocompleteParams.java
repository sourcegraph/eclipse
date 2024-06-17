package com.sourcegraph.cody.protocol_generated;

public final class AutocompleteParams {
  public String uri;
  public String filePath;
  public Position position;
  public TriggerKindEnum triggerKind; // Oneof: Automatic, Invoke
  public SelectedCompletionInfo selectedCompletionInfo;

  public enum TriggerKindEnum {
    @com.google.gson.annotations.SerializedName("Automatic")
    Automatic,
    @com.google.gson.annotations.SerializedName("Invoke")
    Invoke,
  }
}
