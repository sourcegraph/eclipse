package com.sourcegraph.cody.protocol_generated;

public final class ProtocolDiagnostic {
  public ProtocolLocation location;
  public String message;
  public DiagnosticSeverity severity; // Oneof: error, warning, info, suggestion
  public String code;
  public String source;
  public java.util.List<ProtocolRelatedInformationDiagnostic> relatedInformation;
}
