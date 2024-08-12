package com.sourcegraph.cody.webview_protocol;

public final class MentionQuery {
  public ContextMentionProviderID provider;
  public String text;
  public RangeData range;
  public Boolean maybeHasRangeSuffix;
  public Boolean includeRemoteRepositories;
}
