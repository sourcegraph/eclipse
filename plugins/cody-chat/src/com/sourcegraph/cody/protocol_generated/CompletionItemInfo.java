package com.sourcegraph.cody.protocol_generated;

public final class CompletionItemInfo {
  public Long parseErrorCount;
  public Long lineTruncatedCount;
  public TruncatedWithEnum truncatedWith; // Oneof: tree-sitter, indentation
  public NodeTypesParams nodeTypes;
  public NodeTypesWithCompletionParams nodeTypesWithCompletion;
  public Long lineCount;
  public Long charCount;
  public String insertText;
  public String stopReason;

  public enum TruncatedWithEnum {
    @com.google.gson.annotations.SerializedName("tree-sitter")
    Tree_sitter,
    @com.google.gson.annotations.SerializedName("indentation")
    Indentation,
  }
}
