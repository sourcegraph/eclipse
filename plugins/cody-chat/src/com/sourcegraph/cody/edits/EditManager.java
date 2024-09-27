package com.sourcegraph.cody.edits;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public class EditManager {
  public final Image codyIcon =
      new Image(Display.getDefault(), getClass().getResourceAsStream("/icons/sample.png"));

  private final Map<String, FileEditManager> managers;

  public EditManager() {
    this.managers = new ConcurrentHashMap<>();
  }

  public FileEditManager get(String filePath) {
    return managers.get(filePath);
  }

  public void register(String uri, FileEditManager manager) {
    managers.put(uri, manager);
  }

  public void unregister(String uri) {
    managers.remove(uri);
  }

  public void addEdit(String uri, FileEdit edit) {
    FileEditManager manager = managers.get(uri);
    if (manager != null) {
      manager.addEdit(edit);
    }
  }
}
