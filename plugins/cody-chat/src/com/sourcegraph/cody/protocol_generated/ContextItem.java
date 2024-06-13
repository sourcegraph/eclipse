package com.sourcegraph.cody.protocol_generated;

import com.google.gson.JsonDeserializer;

interface ContextItem {
  static JsonDeserializer<ContextItem> deserializer() {
    return (element, _type, context) -> {
      switch (element.getAsJsonObject().get("type").getAsString()) {
        case "file":
          return context.deserialize(element, ContextItemFile.class);
        case "repository":
          return context.deserialize(element, ContextItemRepository.class);
        case "tree":
          return context.deserialize(element, ContextItemTree.class);
        case "symbol":
          return context.deserialize(element, ContextItemSymbol.class);
        case "openctx":
          return context.deserialize(element, ContextItemOpenCtx.class);
        default:
          throw new RuntimeException("Unknown discriminator " + element);
      }
    };
  }

  public final class ContextItemFile implements ContextItem {
    public String uri;
    public RangeData range;
    public String content;
    public String repoName;
    public String revision;
    public String title;
    public String description;
    public ContextItemSource
        source; // Oneof: embeddings, user, editor, search, initial, unified, selection, terminal,
    // uri, history
    public Integer size;
    public Boolean isIgnored;
    public Boolean isTooLarge;
    public String provider;
    public String icon;
    public TypeEnum type; // Oneof: file

    public enum TypeEnum {
      @com.google.gson.annotations.SerializedName("file")
      File,
    }
  }

  public final class ContextItemRepository implements ContextItem {
    public String uri;
    public RangeData range;
    public String content;
    public String repoName;
    public String revision;
    public String title;
    public String description;
    public ContextItemSource
        source; // Oneof: embeddings, user, editor, search, initial, unified, selection, terminal,
    // uri, history
    public Integer size;
    public Boolean isIgnored;
    public Boolean isTooLarge;
    public String provider;
    public String icon;
    public TypeEnum type; // Oneof: repository
    public String repoID;

    public enum TypeEnum {
      @com.google.gson.annotations.SerializedName("repository")
      Repository,
    }
  }

  public final class ContextItemTree implements ContextItem {
    public String uri;
    public RangeData range;
    public String content;
    public String repoName;
    public String revision;
    public String title;
    public String description;
    public ContextItemSource
        source; // Oneof: embeddings, user, editor, search, initial, unified, selection, terminal,
    // uri, history
    public Integer size;
    public Boolean isIgnored;
    public Boolean isTooLarge;
    public String provider;
    public String icon;
    public TypeEnum type; // Oneof: tree
    public Boolean isWorkspaceRoot;
    public String name;

    public enum TypeEnum {
      @com.google.gson.annotations.SerializedName("tree")
      Tree,
    }
  }

  public final class ContextItemSymbol implements ContextItem {
    public String uri;
    public RangeData range;
    public String content;
    public String repoName;
    public String revision;
    public String title;
    public String description;
    public ContextItemSource
        source; // Oneof: embeddings, user, editor, search, initial, unified, selection, terminal,
    // uri, history
    public Integer size;
    public Boolean isIgnored;
    public Boolean isTooLarge;
    public String provider;
    public String icon;
    public TypeEnum type; // Oneof: symbol
    public String symbolName;
    public SymbolKind kind; // Oneof: class, function, method

    public enum TypeEnum {
      @com.google.gson.annotations.SerializedName("symbol")
      Symbol,
    }
  }

  public final class ContextItemOpenCtx implements ContextItem {
    public String uri;
    public RangeData range;
    public String content;
    public String repoName;
    public String revision;
    public String title;
    public String description;
    public ContextItemSource
        source; // Oneof: embeddings, user, editor, search, initial, unified, selection, terminal,
    // uri, history
    public Integer size;
    public Boolean isIgnored;
    public Boolean isTooLarge;
    public String provider;
    public String icon;
    public TypeEnum type; // Oneof: openctx
    public String providerUri;
    public MentionParams mention;

    public enum TypeEnum {
      @com.google.gson.annotations.SerializedName("openctx")
      Openctx,
    }
  }
}
