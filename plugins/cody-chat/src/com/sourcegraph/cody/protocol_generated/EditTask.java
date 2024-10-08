package com.sourcegraph.cody.protocol_generated;

public final class EditTask {
  public String id;
  public CodyTaskState
      state; // Oneof: Idle, Working, Inserting, Applying, Applied, Finished, Error, Pending
  public CodyError error;
  public Range selectionRange;
  public String instruction;
  public String model;
  public String originalText;
}
