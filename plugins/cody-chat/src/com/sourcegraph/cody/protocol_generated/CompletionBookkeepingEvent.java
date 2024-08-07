package com.sourcegraph.cody.protocol_generated;

public final class CompletionBookkeepingEvent {
  public CompletionLogID id;
  public Long startedAt;
  public Long networkRequestStartedAt;
  public Long startLoggedAt;
  public Long loadedAt;
  public Long suggestedAt;
  public Long suggestionLoggedAt;
  public Long suggestionAnalyticsLoggedAt;
  public Long acceptedAt;
  public java.util.List<CompletionItemInfo> items;
  public Long loggedPartialAcceptedLength;
  public Boolean read;
}
