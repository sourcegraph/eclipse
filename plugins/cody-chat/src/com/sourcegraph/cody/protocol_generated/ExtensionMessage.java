package com.sourcegraph.cody.protocol_generated;

import com.google.gson.JsonDeserializer;

public abstract class ExtensionMessage {
  public static JsonDeserializer<ExtensionMessage> deserializer() {
    return (element, _type, context) -> {
      switch (element.getAsJsonObject().get("type").getAsString()) {
        case "config":
          return context.deserialize(element, ConfigExtensionMessage.class);
        case "search:config":
          return context.deserialize(element, Search_configExtensionMessage.class);
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
        case "update-search-results":
          return context.deserialize(element, Update_search_resultsExtensionMessage.class);
        case "index-updated":
          return context.deserialize(element, Index_updatedExtensionMessage.class);
        case "enhanced-context":
          return context.deserialize(element, Enhanced_contextExtensionMessage.class);
        case "attribution":
          return context.deserialize(element, AttributionExtensionMessage.class);
        case "setChatEnabledConfigFeature":
          return context.deserialize(element, SetChatEnabledConfigFeatureExtensionMessage.class);
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

  public final class ConfigExtensionMessage extends ExtensionMessage {
    public TypeEnum type; // Oneof: config
    public ConfigParams config;
    public AuthStatus authStatus;
    public java.util.List<String> workspaceFolderUris;

    public enum TypeEnum {
      @com.google.gson.annotations.SerializedName("config")
      Config,
    }
  }

  public final class Search_configExtensionMessage extends ExtensionMessage {
    public TypeEnum type; // Oneof: search:config
    public java.util.List<String> workspaceFolderUris;

    public enum TypeEnum {
      @com.google.gson.annotations.SerializedName("search:config")
      Search_config,
    }
  }

  public final class HistoryExtensionMessage extends ExtensionMessage {
    public TypeEnum type; // Oneof: history
    public UserLocalHistory localHistory;

    public enum TypeEnum {
      @com.google.gson.annotations.SerializedName("history")
      History,
    }
  }

  public final class TranscriptExtensionMessage extends ExtensionMessage {
    public TypeEnum type; // Oneof: transcript
    public java.util.List<SerializedChatMessage> messages;
    public Boolean isMessageInProgress;
    public String chatID;

    public enum TypeEnum {
      @com.google.gson.annotations.SerializedName("transcript")
      Transcript,
    }
  }

  public final class ViewExtensionMessage extends ExtensionMessage {
    public TypeEnum type; // Oneof: view
    public View view; // Oneof: chat, login

    public enum TypeEnum {
      @com.google.gson.annotations.SerializedName("view")
      View,
    }
  }

  public final class ErrorsExtensionMessage extends ExtensionMessage {
    public TypeEnum type; // Oneof: errors
    public String errors;

    public enum TypeEnum {
      @com.google.gson.annotations.SerializedName("errors")
      Errors,
    }
  }

  public final class Transcript_errorsExtensionMessage extends ExtensionMessage {
    public TypeEnum type; // Oneof: transcript-errors
    public Boolean isTranscriptError;

    public enum TypeEnum {
      @com.google.gson.annotations.SerializedName("transcript-errors")
      Transcript_errors,
    }
  }

  public final class UserContextFilesExtensionMessage extends ExtensionMessage {
    public TypeEnum type; // Oneof: userContextFiles
    public java.util.List<ContextItem> userContextFiles;

    public enum TypeEnum {
      @com.google.gson.annotations.SerializedName("userContextFiles")
      UserContextFiles,
    }
  }

  public final class ClientStateExtensionMessage extends ExtensionMessage {
    public TypeEnum type; // Oneof: clientState
    public ClientStateForWebview value;

    public enum TypeEnum {
      @com.google.gson.annotations.SerializedName("clientState")
      ClientState,
    }
  }

  public final class ClientActionExtensionMessage extends ExtensionMessage {
    public TypeEnum type; // Oneof: clientAction
    public java.util.List<ContextItem> addContextItemsToLastHumanInput;

    public enum TypeEnum {
      @com.google.gson.annotations.SerializedName("clientAction")
      ClientAction,
    }
  }

  public final class ChatModelsExtensionMessage extends ExtensionMessage {
    public TypeEnum type; // Oneof: chatModels
    public java.util.List<Model> models;

    public enum TypeEnum {
      @com.google.gson.annotations.SerializedName("chatModels")
      ChatModels,
    }
  }

  public final class Update_search_resultsExtensionMessage extends ExtensionMessage {
    public TypeEnum type; // Oneof: update-search-results
    public java.util.List<SearchPanelFile> results;
    public String query;

    public enum TypeEnum {
      @com.google.gson.annotations.SerializedName("update-search-results")
      Update_search_results,
    }
  }

  public final class Index_updatedExtensionMessage extends ExtensionMessage {
    public TypeEnum type; // Oneof: index-updated
    public String scopeDir;

    public enum TypeEnum {
      @com.google.gson.annotations.SerializedName("index-updated")
      Index_updated,
    }
  }

  public final class Enhanced_contextExtensionMessage extends ExtensionMessage {
    public TypeEnum type; // Oneof: enhanced-context
    public EnhancedContextContextT enhancedContextStatus;

    public enum TypeEnum {
      @com.google.gson.annotations.SerializedName("enhanced-context")
      Enhanced_context,
    }
  }

  public final class AttributionExtensionMessage extends ExtensionMessage {
    public TypeEnum type; // Oneof: attribution
    public String snippet;
    public AttributionParams attribution;
    public String error;

    public enum TypeEnum {
      @com.google.gson.annotations.SerializedName("attribution")
      Attribution,
    }
  }

  public final class SetChatEnabledConfigFeatureExtensionMessage extends ExtensionMessage {
    public TypeEnum type; // Oneof: setChatEnabledConfigFeature
    public Boolean data;

    public enum TypeEnum {
      @com.google.gson.annotations.SerializedName("setChatEnabledConfigFeature")
      SetChatEnabledConfigFeature,
    }
  }

  public final class Context_remote_reposExtensionMessage extends ExtensionMessage {
    public TypeEnum type; // Oneof: context/remote-repos
    public java.util.List<Repo> repos;

    public enum TypeEnum {
      @com.google.gson.annotations.SerializedName("context/remote-repos")
      Context_remote_repos,
    }
  }

  public final class SetConfigFeaturesExtensionMessage extends ExtensionMessage {
    public TypeEnum type; // Oneof: setConfigFeatures
    public ConfigFeaturesParams configFeatures;

    public enum TypeEnum {
      @com.google.gson.annotations.SerializedName("setConfigFeatures")
      SetConfigFeatures,
    }
  }

  public final class AllMentionProvidersMetadataExtensionMessage extends ExtensionMessage {
    public TypeEnum type; // Oneof: allMentionProvidersMetadata
    public java.util.List<ContextMentionProviderMetadata> providers;

    public enum TypeEnum {
      @com.google.gson.annotations.SerializedName("allMentionProvidersMetadata")
      AllMentionProvidersMetadata,
    }
  }
}
