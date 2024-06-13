package com.sourcegraph.cody.protocol_generated;

public final class CompletionBookkeepingEvent {
  public CompletionLogID id;
  public Integer startedAt;
  public Integer networkRequestStartedAt;
  public Integer startLoggedAt;
  public Integer loadedAt;
  public Integer suggestedAt;
  public Integer suggestionLoggedAt;
  public Integer suggestionAnalyticsLoggedAt;
  public Integer acceptedAt;
  public java.util.List<CompletionItemInfo> items;
  public Integer loggedPartialAcceptedLength;
}
