package com.sourcegraph.cody.workspace;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

public final class EditorState {
  public final IFile file;
  public final String uri;
  public final ITextEditor editor;
  @Nullable private IDocument document = null;

  private EditorState(IFile file, String uri, ITextEditor editor) {
    this.file = file;
    this.uri = uri;
    this.editor = editor;
  }

  public String readContents() {
    return getDocument().get();
  }

  public IDocument getDocument() {
    if (document == null) {
      document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
    }
    return document;
  }

  @Nullable  public static EditorState from(IWorkbenchPartReference partReference) {
    var part = partReference.getPart(false);
    if (!(part instanceof ITextEditor)) {
      return null;
    }
    var editor1 = (ITextEditor) part;
    var input = editor1.getEditorInput();
    if (!(input instanceof FileEditorInput)) {
      System.out.println("Unknown input kind: " + input.getClass());
      return null;
    }
    var file1 = ((FileEditorInput) input).getFile();
    return new EditorState(file1, file1.getLocationURI().toString(), editor1);
  }

}
