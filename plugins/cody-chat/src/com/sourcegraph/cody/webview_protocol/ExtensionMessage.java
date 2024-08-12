package com.sourcegraph.cody.webview_protocol;

import com.google.gson.JsonDeserializer;

public abstract class ExtensionMessage {
  public static JsonDeserializer<ExtensionMessage> deserializer() {
    return (element, _type, context) -> {
      switch (element.getAsJsonObject().get("type").getAsString()) {
        case "config":
          return context.deserialize(element, ConfigExtensionMessage.class);
        case "ui/theme":
          return context.deserialize(element, Ui_themeExtensionMessage.class);
        case "history":
          return context.deserialize(element, HistoryExtensionMessage.class);
        case "transcript":
          return context.deserialize(element, TranscriptExtensionMessage.class);
        case "view":
          return context.deserialize(element, ViewExtensionMessage.class);
        case "errors":
          return context.deserialize(element, ErrorsExtensionMessage.class);
        case "transcript-errors":
          return context.deserialize(element, Transcript_errorsExtensionMessage.class);
        case "userContextFiles":
          return context.deserialize(element, UserContextFilesExtensionMessage.class);
        case "clientState":
          return context.deserialize(element, ClientStateExtensionMessage.class);
        case "clientAction":
          return context.deserialize(element, ClientActionExtensionMessage.class);
        case "chatModels":
          return context.deserialize(element, ChatModelsExtensionMessage.class);
        case "enhanced-context":
          return context.deserialize(element, Enhanced_contextExtensionMessage.class);
        case "attribution":
          return context.deserialize(element, AttributionExtensionMessage.class);
        case "context/remote-repos":
          return context.deserialize(element, Context_remote_reposExtensionMessage.class);
        case "setConfigFeatures":
          return context.deserialize(element, SetConfigFeaturesExtensionMessage.class);
        case "allMentionProvidersMetadata":
          return context.deserialize(element, AllMentionProvidersMetadataExtensionMessage.class);
        default:
          throw new RuntimeException("Unknown discriminator " + element);
      }
    };
  }

  public static final class ConfigExtensionMessage extends ExtensionMessage {
    public TypeEnum type; // Oneof: config
    public ConfigParams config;
    public AuthStatus authStatus;
    public java.util.List<String> workspaceFolderUris;

    public enum TypeEnum {
      @com.google.gson.annotations.SerializedName("config")
      Config,
    }
  }

  public static final class Ui_themeExtensionMessage extends ExtensionMessage {
    public TypeEnum type; // Oneof: ui/theme
    public CodyIDE agentIDE; // Oneof: VSCode, JetBrains, Neovim, Emacs, Web
    public CodyIDECssVariables cssVariables;

    public enum TypeEnum {
      @com.google.gson.annotations.SerializedName("ui/theme")
      Ui_theme,
    }
  }

  public static final class HistoryExtensionMessage extends ExtensionMessage {
    public TypeEnum type; // Oneof: history
    public UserLocalHistory localHistory;

    public enum TypeEnum {
      @com.google.gson.annotations.SerializedName("history")
      History,
    }
  }

  public static final class TranscriptExtensionMessage extends ExtensionMessage {
    public TypeEnum type; // Oneof: transcript
    public java.util.List<SerializedChatMessage> messages;
    public Boolean isMessageInProgress;
    public String chatID;

    public enum TypeEnum {
      @com.google.gson.annotations.SerializedName("transcript")
      Transcript,
    }
  }

  public static final class ViewExtensionMessage extends ExtensionMessage {
    public TypeEnum type; // Oneof: view
    public View view; // Oneof: chat, login

    public enum TypeEnum {
      @com.google.gson.annotations.SerializedName("view")
      View,
    }
  }

  public static final class ErrorsExtensionMessage extends ExtensionMessage {
    public TypeEnum type; // Oneof: errors
    public String errors;

    public enum TypeEnum {
      @com.google.gson.annotations.SerializedName("errors")
      Errors,
    }
  }

  public static final class Transcript_errorsExtensionMessage extends ExtensionMessage {
    public TypeEnum type; // Oneof: transcript-errors
    public Boolean isTranscriptError;

    public enum TypeEnum {
      @com.google.gson.annotations.SerializedName("transcript-errors")
      Transcript_errors,
    }
  }

  public static final class UserContextFilesExtensionMessage extends ExtensionMessage {
    public TypeEnum type; // Oneof: userContextFiles
    public java.util.List<ContextItem> userContextFiles;

    public enum TypeEnum {
      @com.google.gson.annotations.SerializedName("userContextFiles")
      UserContextFiles,
    }
  }

  public static final class ClientStateExtensionMessage extends ExtensionMessage {
    public TypeEnum type; // Oneof: clientState
    public ClientStateForWebview value;

    public enum TypeEnum {
      @com.google.gson.annotations.SerializedName("clientState")
      ClientState,
    }
  }

  public static final class ClientActionExtensionMessage extends ExtensionMessage {
    public TypeEnum type; // Oneof: clientAction
    public java.util.List<ContextItem> addContextItemsToLastHumanInput;

    public enum TypeEnum {
      @com.google.gson.annotations.SerializedName("clientAction")
      ClientAction,
    }
  }

  public static final class ChatModelsExtensionMessage extends ExtensionMessage {
    public TypeEnum type; // Oneof: chatModels
    public java.util.List<Model> models;

    public enum TypeEnum {
      @com.google.gson.annotations.SerializedName("chatModels")
      ChatModels,
    }
  }

  public static final class Enhanced_contextExtensionMessage extends ExtensionMessage {
    public TypeEnum type; // Oneof: enhanced-context
    public EnhancedContextContextT enhancedContextStatus;

    public enum TypeEnum {
      @com.google.gson.annotations.SerializedName("enhanced-context")
      Enhanced_context,
    }
  }

  public static final class AttributionExtensionMessage extends ExtensionMessage {
    public TypeEnum type; // Oneof: attribution
    public String snippet;
    public AttributionParams attribution;
    public String error;

    public enum TypeEnum {
      @com.google.gson.annotations.SerializedName("attribution")
      Attribution,
    }
  }

  public static final class Context_remote_reposExtensionMessage extends ExtensionMessage {
    public TypeEnum type; // Oneof: context/remote-repos
    public java.util.List<Repo> repos;

    public enum TypeEnum {
      @com.google.gson.annotations.SerializedName("context/remote-repos")
      Context_remote_repos,
    }
  }

  public static final class SetConfigFeaturesExtensionMessage extends ExtensionMessage {
    public TypeEnum type; // Oneof: setConfigFeatures
    public ConfigFeaturesParams configFeatures;

    public enum TypeEnum {
      @com.google.gson.annotations.SerializedName("setConfigFeatures")
      SetConfigFeatures,
    }
  }

  public static final class AllMentionProvidersMetadataExtensionMessage extends ExtensionMessage {
    public TypeEnum type; // Oneof: allMentionProvidersMetadata
    public java.util.List<ContextMentionProviderMetadata> providers;

    public enum TypeEnum {
      @com.google.gson.annotations.SerializedName("allMentionProvidersMetadata")
      AllMentionProvidersMetadata,
    }
  }
}
