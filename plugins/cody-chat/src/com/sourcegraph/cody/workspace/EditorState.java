package com.sourcegraph.cody.workspace;

import com.sourcegraph.cody.WrappedRuntimeException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

public final class EditorState {
  public final IFile file;
  public final String uri;

  private EditorState(IFile file, String uri) {
    this.file = file;
    this.uri = uri;
  }

  public String readContents() {
    try {
      return new String(file.getContents().readAllBytes(), StandardCharsets.UTF_8);
    } catch (CoreException | IOException e) {
      throw new WrappedRuntimeException(e);
    }
  }

  @Nullable  public static EditorState from(IWorkbenchPartReference partReference) {
    var part = partReference.getPart(false);
    if (!(part instanceof ITextEditor)) {
      return null;
    }
    var editor = (ITextEditor) part;
    var input = editor.getEditorInput();
    if (!(input instanceof FileEditorInput)) {
      System.out.println("Unknown input kind: " + input.getClass());
      return null;
    }
    var file = ((FileEditorInput) input).getFile();
    return new EditorState(file, file.getLocationURI().toString());
  }
}
