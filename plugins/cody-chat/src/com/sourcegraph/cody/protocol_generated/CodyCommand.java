package com.sourcegraph.cody.protocol_generated;

public final class CodyCommand {
  public String slashCommand;
  public String key;
  public String prompt;
  public String description;
  public CodyCommandContext context;
  public CodyCommandType type; // Oneof: workspace, user, default, experimental, recently used
  public CodyCommandMode mode; // Oneof: ask, edit, insert
  public String requestID;
}
