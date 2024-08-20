package com.sourcegraph.cody.webview_protocol;

public final class ConfigParams {
  public Boolean experimentalNoodle;
  public CodyIDE agentIDE; // Oneof: VSCode, JetBrains, Neovim, Emacs, Web
  public String agentExtensionVersion;
  public String serverEndpoint;
  public Boolean uiKindIsWeb;
}
