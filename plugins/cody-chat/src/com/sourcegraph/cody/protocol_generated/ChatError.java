package com.sourcegraph.cody.protocol_generated;

public final class ChatError {
  public String kind;
  public String name;
  public String message;
  public String retryAfter;
  public Integer limit;
  public String userMessage;
  public Date retryAfterDate;
  public String retryAfterDateString;
  public String retryMessage;
  public String feature;
  public Boolean upgradeIsAvailable;
  public IsChatErrorGuardEnum isChatErrorGuard; // Oneof: isChatErrorGuard

  public enum IsChatErrorGuardEnum {
    @com.google.gson.annotations.SerializedName("isChatErrorGuard") IsChatErrorGuard,
  }
}

