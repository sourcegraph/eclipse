package com.sourcegraph.cody.protocol_generated;

import com.google.gson.JsonDeserializer;

public abstract class ContextProvider {
  public static JsonDeserializer<ContextProvider> deserializer() {
    return (element, _type, context) -> {
      switch (element.getAsJsonObject().get("kind").getAsString()) {
        case "embeddings":
          return context.deserialize(element, LocalEmbeddingsProvider.class);
        case "search":
          return context.deserialize(element, LocalSearchProvider.class);
        default:
          throw new RuntimeException("Unknown discriminator " + element);
      }
    };
  }

  public final class LocalEmbeddingsProvider extends ContextProvider {
    public KindEnum kind; // Oneof: embeddings
    public StateEnum state; // Oneof: indeterminate, no-match, unconsented, indexing, ready
    public ErrorReasonEnum errorReason; // Oneof: not-a-git-repo, git-repo-has-no-remote
    public EmbeddingsProvider embeddingsAPIProvider; // Oneof: sourcegraph, openai

    public enum KindEnum {
      @com.google.gson.annotations.SerializedName("embeddings")
      Embeddings,
    }

    public enum StateEnum {
      @com.google.gson.annotations.SerializedName("indeterminate")
      Indeterminate,
      @com.google.gson.annotations.SerializedName("no-match")
      No_match,
      @com.google.gson.annotations.SerializedName("unconsented")
      Unconsented,
      @com.google.gson.annotations.SerializedName("indexing")
      Indexing,
      @com.google.gson.annotations.SerializedName("ready")
      Ready,
    }

    public enum ErrorReasonEnum {
      @com.google.gson.annotations.SerializedName("not-a-git-repo")
      Not_a_git_repo,
      @com.google.gson.annotations.SerializedName("git-repo-has-no-remote")
      Git_repo_has_no_remote,
    }
  }

  public final class LocalSearchProvider extends ContextProvider {
    public KindEnum kind; // Oneof: search
    public TypeEnum type; // Oneof: local
    public StateEnum state; // Oneof: unindexed, indexing, ready, failed

    public enum KindEnum {
      @com.google.gson.annotations.SerializedName("search")
      Search,
    }

    public enum TypeEnum {
      @com.google.gson.annotations.SerializedName("local")
      Local,
    }

    public enum StateEnum {
      @com.google.gson.annotations.SerializedName("unindexed")
      Unindexed,
      @com.google.gson.annotations.SerializedName("indexing")
      Indexing,
      @com.google.gson.annotations.SerializedName("ready")
      Ready,
      @com.google.gson.annotations.SerializedName("failed")
      Failed,
    }
  }

  public final class RemoteSearchProvider extends ContextProvider {
    public KindEnum kind; // Oneof: search
    public TypeEnum type; // Oneof: remote
    public StateEnum state; // Oneof: ready, no-match
    public String id;
    public InclusionEnum inclusion; // Oneof: auto, manual
    public Boolean isIgnored;

    public enum KindEnum {
      @com.google.gson.annotations.SerializedName("search")
      Search,
    }

    public enum TypeEnum {
      @com.google.gson.annotations.SerializedName("remote")
      Remote,
    }

    public enum StateEnum {
      @com.google.gson.annotations.SerializedName("ready")
      Ready,
      @com.google.gson.annotations.SerializedName("no-match")
      No_match,
    }

    public enum InclusionEnum {
      @com.google.gson.annotations.SerializedName("auto")
      Auto,
      @com.google.gson.annotations.SerializedName("manual")
      Manual,
    }
  }
}
