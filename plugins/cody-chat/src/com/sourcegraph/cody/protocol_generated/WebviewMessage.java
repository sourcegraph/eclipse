package com.sourcegraph.cody.protocol_generated;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import java.lang.reflect.Type;

public abstract class WebviewMessage {
    public static JsonDeserializer<WebviewMessage> deserializer() {
      return (element, _type, context) -> {
        switch (element.getAsJsonObject().get("command").getAsString()) {
          case "ready": return context.deserialize(element, ReadyWebviewMessage.class);
          case "initialized": return context.deserialize(element, InitializedWebviewMessage.class);
          case "event": return context.deserialize(element, EventWebviewMessage.class);
          case "recordEvent": return context.deserialize(element, RecordEventWebviewMessage.class);
          case "submit": return context.deserialize(element, SubmitWebviewMessage.class);
          case "history": return context.deserialize(element, HistoryWebviewMessage.class);
          case "restoreHistory": return context.deserialize(element, RestoreHistoryWebviewMessage.class);
          case "deleteHistory": return context.deserialize(element, DeleteHistoryWebviewMessage.class);
          case "links": return context.deserialize(element, LinksWebviewMessage.class);
          case "show-page": return context.deserialize(element, Show_pageWebviewMessage.class);
          case "chatModel": return context.deserialize(element, ChatModelWebviewMessage.class);
          case "get-chat-models": return context.deserialize(element, Get_chat_modelsWebviewMessage.class);
          case "openFile": return context.deserialize(element, OpenFileWebviewMessage.class);
          case "openLocalFileWithRange": return context.deserialize(element, OpenLocalFileWithRangeWebviewMessage.class);
          case "edit": return context.deserialize(element, EditWebviewMessage.class);
          case "context/get-remote-search-repos": return context.deserialize(element, Context_get_remote_search_reposWebviewMessage.class);
          case "context/choose-remote-search-repo": return context.deserialize(element, Context_choose_remote_search_repoWebviewMessage.class);
          case "context/remove-remote-search-repo": return context.deserialize(element, Context_remove_remote_search_repoWebviewMessage.class);
          case "embeddings/index": return context.deserialize(element, Embeddings_indexWebviewMessage.class);
          case "symf/index": return context.deserialize(element, Symf_indexWebviewMessage.class);
          case "insert": return context.deserialize(element, InsertWebviewMessage.class);
          case "newFile": return context.deserialize(element, NewFileWebviewMessage.class);
          case "copy": return context.deserialize(element, CopyWebviewMessage.class);
          case "auth": return context.deserialize(element, AuthWebviewMessage.class);
          case "abort": return context.deserialize(element, AbortWebviewMessage.class);
          case "simplified-onboarding": return context.deserialize(element, Simplified_onboardingWebviewMessage.class);
          case "getUserContext": return context.deserialize(element, GetUserContextWebviewMessage.class);
          case "queryContextItems": return context.deserialize(element, QueryContextItemsWebviewMessage.class);
          case "search": return context.deserialize(element, SearchWebviewMessage.class);
          case "show-search-result": return context.deserialize(element, Show_search_resultWebviewMessage.class);
          case "reset": return context.deserialize(element, ResetWebviewMessage.class);
          case "attribution-search": return context.deserialize(element, Attribution_searchWebviewMessage.class);
          case "troubleshoot/reloadAuth": return context.deserialize(element, Troubleshoot_reloadAuthWebviewMessage.class);
          case "getAllMentionProvidersMetadata": return context.deserialize(element, GetAllMentionProvidersMetadataWebviewMessage.class);
          default: throw new RuntimeException("Unknown discriminator " + element);
        }
      };
  }

public final class ReadyWebviewMessage extends WebviewMessage {
  public CommandEnum command; // Oneof: ready

  public enum CommandEnum {
    @com.google.gson.annotations.SerializedName("ready") Ready,
  }
}

public final class InitializedWebviewMessage extends WebviewMessage {
  public CommandEnum command; // Oneof: initialized

  public enum CommandEnum {
    @com.google.gson.annotations.SerializedName("initialized") Initialized,
  }
}

public final class EventWebviewMessage extends WebviewMessage {
  public CommandEnum command; // Oneof: event
  public String eventName;
  public TelemetryEventProperties properties;

  public enum CommandEnum {
    @com.google.gson.annotations.SerializedName("event") Event,
  }
}

public final class RecordEventWebviewMessage extends WebviewMessage {
  public CommandEnum command; // Oneof: recordEvent
  public String feature;
  public String action;
  public WebviewRecordEventParameters parameters;

  public enum CommandEnum {
    @com.google.gson.annotations.SerializedName("recordEvent") RecordEvent,
  }
}

public final class SubmitWebviewMessage extends WebviewMessage {
  public CommandEnum command; // Oneof: submit
  public Boolean addEnhancedContext;
  public java.util.List<ContextItem> contextFiles;
  public String text;
  public ChatSubmitType submitType; // Oneof: user, user-newchat
  public Object editorState;

  public enum CommandEnum {
    @com.google.gson.annotations.SerializedName("submit") Submit,
  }
}

public final class HistoryWebviewMessage extends WebviewMessage {
  public CommandEnum command; // Oneof: history
  public ActionEnum action; // Oneof: clear, export

  public enum CommandEnum {
    @com.google.gson.annotations.SerializedName("history") History,
  }

  public enum ActionEnum {
    @com.google.gson.annotations.SerializedName("clear") Clear,
    @com.google.gson.annotations.SerializedName("export") Export,
  }
}

public final class RestoreHistoryWebviewMessage extends WebviewMessage {
  public CommandEnum command; // Oneof: restoreHistory
  public String chatID;

  public enum CommandEnum {
    @com.google.gson.annotations.SerializedName("restoreHistory") RestoreHistory,
  }
}

public final class DeleteHistoryWebviewMessage extends WebviewMessage {
  public CommandEnum command; // Oneof: deleteHistory
  public String chatID;

  public enum CommandEnum {
    @com.google.gson.annotations.SerializedName("deleteHistory") DeleteHistory,
  }
}

public final class LinksWebviewMessage extends WebviewMessage {
  public CommandEnum command; // Oneof: links
  public String value;

  public enum CommandEnum {
    @com.google.gson.annotations.SerializedName("links") Links,
  }
}

public final class Show_pageWebviewMessage extends WebviewMessage {
  public CommandEnum command; // Oneof: show-page
  public String page;

  public enum CommandEnum {
    @com.google.gson.annotations.SerializedName("show-page") Show_page,
  }
}

public final class ChatModelWebviewMessage extends WebviewMessage {
  public CommandEnum command; // Oneof: chatModel
  public String model;

  public enum CommandEnum {
    @com.google.gson.annotations.SerializedName("chatModel") ChatModel,
  }
}

public final class Get_chat_modelsWebviewMessage extends WebviewMessage {
  public CommandEnum command; // Oneof: get-chat-models

  public enum CommandEnum {
    @com.google.gson.annotations.SerializedName("get-chat-models") Get_chat_models,
  }
}

public final class OpenFileWebviewMessage extends WebviewMessage {
  public CommandEnum command; // Oneof: openFile
  public Uri uri;
  public RangeData range;

  public enum CommandEnum {
    @com.google.gson.annotations.SerializedName("openFile") OpenFile,
  }
}

public final class OpenLocalFileWithRangeWebviewMessage extends WebviewMessage {
  public CommandEnum command; // Oneof: openLocalFileWithRange
  public String filePath;
  public RangeData range;

  public enum CommandEnum {
    @com.google.gson.annotations.SerializedName("openLocalFileWithRange") OpenLocalFileWithRange,
  }
}

public final class EditWebviewMessage extends WebviewMessage {
  public CommandEnum command; // Oneof: edit
  public Boolean addEnhancedContext;
  public java.util.List<ContextItem> contextFiles;
  public String text;
  public Integer index;
  public Object editorState;

  public enum CommandEnum {
    @com.google.gson.annotations.SerializedName("edit") Edit,
  }
}

public final class Context_get_remote_search_reposWebviewMessage extends WebviewMessage {
  public CommandEnum command; // Oneof: context/get-remote-search-repos

  public enum CommandEnum {
    @com.google.gson.annotations.SerializedName("context/get-remote-search-repos") Context_get_remote_search_repos,
  }
}

public final class Context_choose_remote_search_repoWebviewMessage extends WebviewMessage {
  public CommandEnum command; // Oneof: context/choose-remote-search-repo
  public java.util.List<Repo> explicitRepos;

  public enum CommandEnum {
    @com.google.gson.annotations.SerializedName("context/choose-remote-search-repo") Context_choose_remote_search_repo,
  }
}

public final class Context_remove_remote_search_repoWebviewMessage extends WebviewMessage {
  public CommandEnum command; // Oneof: context/remove-remote-search-repo
  public String repoId;

  public enum CommandEnum {
    @com.google.gson.annotations.SerializedName("context/remove-remote-search-repo") Context_remove_remote_search_repo,
  }
}

public final class Embeddings_indexWebviewMessage extends WebviewMessage {
  public CommandEnum command; // Oneof: embeddings/index

  public enum CommandEnum {
    @com.google.gson.annotations.SerializedName("embeddings/index") Embeddings_index,
  }
}

public final class Symf_indexWebviewMessage extends WebviewMessage {
  public CommandEnum command; // Oneof: symf/index

  public enum CommandEnum {
    @com.google.gson.annotations.SerializedName("symf/index") Symf_index,
  }
}

public final class InsertWebviewMessage extends WebviewMessage {
  public CommandEnum command; // Oneof: insert
  public String text;

  public enum CommandEnum {
    @com.google.gson.annotations.SerializedName("insert") Insert,
  }
}

public final class NewFileWebviewMessage extends WebviewMessage {
  public CommandEnum command; // Oneof: newFile
  public String text;

  public enum CommandEnum {
    @com.google.gson.annotations.SerializedName("newFile") NewFile,
  }
}

public final class CopyWebviewMessage extends WebviewMessage {
  public CommandEnum command; // Oneof: copy
  public EventTypeEnum eventType; // Oneof: Button, Keydown
  public String text;

  public enum CommandEnum {
    @com.google.gson.annotations.SerializedName("copy") Copy,
  }

  public enum EventTypeEnum {
    @com.google.gson.annotations.SerializedName("Button") Button,
    @com.google.gson.annotations.SerializedName("Keydown") Keydown,
  }
}

public final class AuthWebviewMessage extends WebviewMessage {
  public CommandEnum command; // Oneof: auth
  public AuthKindEnum authKind; // Oneof: signin, signout, support, callback, simplified-onboarding
  public String endpoint;
  public String value;
  public AuthMethod authMethod; // Oneof: dotcom, github, gitlab, google

  public enum CommandEnum {
    @com.google.gson.annotations.SerializedName("auth") Auth,
  }

  public enum AuthKindEnum {
    @com.google.gson.annotations.SerializedName("signin") Signin,
    @com.google.gson.annotations.SerializedName("signout") Signout,
    @com.google.gson.annotations.SerializedName("support") Support,
    @com.google.gson.annotations.SerializedName("callback") Callback,
    @com.google.gson.annotations.SerializedName("simplified-onboarding") Simplified_onboarding,
  }
}

public final class AbortWebviewMessage extends WebviewMessage {
  public CommandEnum command; // Oneof: abort

  public enum CommandEnum {
    @com.google.gson.annotations.SerializedName("abort") Abort,
  }
}

public final class Simplified_onboardingWebviewMessage extends WebviewMessage {
  public CommandEnum command; // Oneof: simplified-onboarding
  public OnboardingKindEnum onboardingKind; // Oneof: web-sign-in-token

  public enum CommandEnum {
    @com.google.gson.annotations.SerializedName("simplified-onboarding") Simplified_onboarding,
  }

  public enum OnboardingKindEnum {
    @com.google.gson.annotations.SerializedName("web-sign-in-token") Web_sign_in_token,
  }
}

public final class GetUserContextWebviewMessage extends WebviewMessage {
  public CommandEnum command; // Oneof: getUserContext
  public String query;

  public enum CommandEnum {
    @com.google.gson.annotations.SerializedName("getUserContext") GetUserContext,
  }
}

public final class QueryContextItemsWebviewMessage extends WebviewMessage {
  public CommandEnum command; // Oneof: queryContextItems
  public MentionQuery query;

  public enum CommandEnum {
    @com.google.gson.annotations.SerializedName("queryContextItems") QueryContextItems,
  }
}

public final class SearchWebviewMessage extends WebviewMessage {
  public CommandEnum command; // Oneof: search
  public String query;

  public enum CommandEnum {
    @com.google.gson.annotations.SerializedName("search") Search,
  }
}

public final class Show_search_resultWebviewMessage extends WebviewMessage {
  public CommandEnum command; // Oneof: show-search-result
  public Uri uri;
  public RangeData range;

  public enum CommandEnum {
    @com.google.gson.annotations.SerializedName("show-search-result") Show_search_result,
  }
}

public final class ResetWebviewMessage extends WebviewMessage {
  public CommandEnum command; // Oneof: reset

  public enum CommandEnum {
    @com.google.gson.annotations.SerializedName("reset") Reset,
  }
}

public final class Attribution_searchWebviewMessage extends WebviewMessage {
  public CommandEnum command; // Oneof: attribution-search
  public String snippet;

  public enum CommandEnum {
    @com.google.gson.annotations.SerializedName("attribution-search") Attribution_search,
  }
}

public final class Troubleshoot_reloadAuthWebviewMessage extends WebviewMessage {
  public CommandEnum command; // Oneof: troubleshoot/reloadAuth

  public enum CommandEnum {
    @com.google.gson.annotations.SerializedName("troubleshoot/reloadAuth") Troubleshoot_reloadAuth,
  }
}

public final class GetAllMentionProvidersMetadataWebviewMessage extends WebviewMessage {
  public CommandEnum command; // Oneof: getAllMentionProvidersMetadata

  public enum CommandEnum {
    @com.google.gson.annotations.SerializedName("getAllMentionProvidersMetadata") GetAllMentionProvidersMetadata,
  }
}
}

