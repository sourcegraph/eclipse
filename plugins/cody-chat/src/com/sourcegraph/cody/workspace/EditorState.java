package com.sourcegraph.cody.workspace;

import com.sourcegraph.cody.WrappedRuntimeException;
import com.sourcegraph.cody.protocol_generated.Position;
import com.sourcegraph.cody.protocol_generated.Range;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

public final class EditorState {
  public final IFile file;
  public final String uri;
  public final ITextEditor editor;
  @Nullable private IDocument document = null;

  private static ILog log = Platform.getLog(EditorState.class);

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

  public Position positionFor(int offset) {
    try {
      var line = getDocument().getLineOfOffset(offset);
      var lineOffset = getDocument().getLineOffset(line);
      var character = offset - lineOffset;
      var position = new Position();
      position.line = (long) line;
      position.character = (long) character;
      return position;
    } catch (BadLocationException e) {
      throw new WrappedRuntimeException(e);
    }
  }

  public Range rangeFor(int start, int length) {
    var range = new Range();
    range.start = positionFor(start);
    range.end = positionFor(start + length);
    return range;
  }

  @Nullable
  public static EditorState from(IWorkbenchPartReference partReference) {
    var part = partReference.getPart(false);
    if (!(part instanceof ITextEditor)) {
      return null;
    }
    var editor1 = (ITextEditor) part;
    var input = editor1.getEditorInput();
    if (!(input instanceof FileEditorInput)) {
      log.warn("Unknown input kind: " + input.getClass());
      return null;
    }
    var file1 = ((FileEditorInput) input).getFile();
    return new EditorState(file1, file1.getLocationURI().toString(), editor1);
  }
}
