package com.sourcegraph.cody.protocol_generated;

public final class ProtocolTextDocument {
  public String uri;
  public String filePath;
  public String content;
  public Range selection;
  public java.util.List<ProtocolTextDocumentContentChangeEvent> contentChanges;
  public Range visibleRange;
  public TestingParams testing;
}

