package com.sourcegraph.cody.protocol_generated;

public final class AuthStatus {
  public String endpoint;
  public Boolean authenticated;
  public Boolean showNetworkError;
  public Boolean showInvalidAccessTokenError;
  public String username;
  public Boolean isFireworksTracingEnabled;
  public Boolean hasVerifiedEmail;
  public Boolean requiresVerifiedEmail;
  public String siteVersion;
  public Long codyApiVersion;
  public CodyLLMSiteConfiguration configOverwrites;
  public String primaryEmail;
  public String displayName;
  public String avatarURL;
  public Boolean userCanUpgrade;
  public Boolean isOfflineMode;
}
