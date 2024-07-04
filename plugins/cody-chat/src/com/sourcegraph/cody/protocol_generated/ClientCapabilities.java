package com.sourcegraph.cody.protocol_generated;

public final class ClientCapabilities {
  public CompletionsEnum completions; // Oneof: none
  public ChatEnum chat; // Oneof: none, streaming
  public GitEnum git; // Oneof: none, enabled
  public ProgressBarsEnum progressBars; // Oneof: none, enabled
  public EditEnum edit; // Oneof: none, enabled
  public EditWorkspaceEnum editWorkspace; // Oneof: none, enabled
  public UntitledDocumentsEnum untitledDocuments; // Oneof: none, enabled
  public ShowDocumentEnum showDocument; // Oneof: none, enabled
  public CodeLensesEnum codeLenses; // Oneof: none, enabled
  public ShowWindowMessageEnum showWindowMessage; // Oneof: notification, request
  public IgnoreEnum ignore; // Oneof: none, enabled
  public CodeActionsEnum codeActions; // Oneof: none, enabled
  public WebviewMessagesEnum webviewMessages; // Oneof: object-encoded, string-encoded

  public enum CompletionsEnum {
    @com.google.gson.annotations.SerializedName("none")
    None,
  }

  public enum ChatEnum {
    @com.google.gson.annotations.SerializedName("none")
    None,
    @com.google.gson.annotations.SerializedName("streaming")
    Streaming,
  }

  public enum GitEnum {
    @com.google.gson.annotations.SerializedName("none")
    None,
    @com.google.gson.annotations.SerializedName("enabled")
    Enabled,
  }

  public enum ProgressBarsEnum {
    @com.google.gson.annotations.SerializedName("none")
    None,
    @com.google.gson.annotations.SerializedName("enabled")
    Enabled,
  }

  public enum EditEnum {
    @com.google.gson.annotations.SerializedName("none")
    None,
    @com.google.gson.annotations.SerializedName("enabled")
    Enabled,
  }

  public enum EditWorkspaceEnum {
    @com.google.gson.annotations.SerializedName("none")
    None,
    @com.google.gson.annotations.SerializedName("enabled")
    Enabled,
  }

  public enum UntitledDocumentsEnum {
    @com.google.gson.annotations.SerializedName("none")
    None,
    @com.google.gson.annotations.SerializedName("enabled")
    Enabled,
  }

  public enum ShowDocumentEnum {
    @com.google.gson.annotations.SerializedName("none")
    None,
    @com.google.gson.annotations.SerializedName("enabled")
    Enabled,
  }

  public enum CodeLensesEnum {
    @com.google.gson.annotations.SerializedName("none")
    None,
    @com.google.gson.annotations.SerializedName("enabled")
    Enabled,
  }

  public enum ShowWindowMessageEnum {
    @com.google.gson.annotations.SerializedName("notification")
    Notification,
    @com.google.gson.annotations.SerializedName("request")
    Request,
  }

  public enum IgnoreEnum {
    @com.google.gson.annotations.SerializedName("none")
    None,
    @com.google.gson.annotations.SerializedName("enabled")
    Enabled,
  }

  public enum CodeActionsEnum {
    @com.google.gson.annotations.SerializedName("none")
    None,
    @com.google.gson.annotations.SerializedName("enabled")
    Enabled,
  }

  public enum WebviewMessagesEnum {
    @com.google.gson.annotations.SerializedName("object-encoded")
    Object_encoded,
    @com.google.gson.annotations.SerializedName("string-encoded")
    String_encoded,
  }
}
