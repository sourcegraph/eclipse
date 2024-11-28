package com.sourcegraph.cody.edits;

public class FileEdit {
  public final int offset;
  public final int length;
  public final String text;

  public FileEdit(int offset, int length, String text) {
    this.offset = offset;
    this.length = length;
    this.text = text;
  }
}
