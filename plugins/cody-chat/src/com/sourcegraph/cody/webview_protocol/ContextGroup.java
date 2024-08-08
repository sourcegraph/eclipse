package com.sourcegraph.cody.webview_protocol;

import com.sourcegraph.cody.protocol_generated.Uri;

public final class ContextGroup {
  public Uri dir;
  public String displayName;
  public java.util.List<ContextProvider> providers;
}
