package com.sourcegraph.cody.webview_protocol;

import com.sourcegraph.cody.protocol_generated.RangeData;

public final class MentionQuery {
  public ContextMentionProviderID provider;
  public String text;
  public RangeData range;
  public Boolean maybeHasRangeSuffix;
  public Boolean includeRemoteRepositories;
}
