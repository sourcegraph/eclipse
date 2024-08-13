package com.sourcegraph.cody;

// java.nio.file.Path always uses the system separator, but java resources
// always use / as the separator. This wrapper class likewise will always use /
public class ResourcePath {

  private final String path;

  public ResourcePath(String path) {
    this.path = path;
  }

  public static ResourcePath of(String path) {
    return new ResourcePath(path);
  }

  public ResourcePath resolve(String path) {
    return new ResourcePath(this.join(path));
  }

  private String join(String child) {
    if (this.path.endsWith("/")) {
      return this.path + child;
    } else {
      return this.path + "/" + child;
    }
  }

  public String toString() {
    return path;
  }
}
