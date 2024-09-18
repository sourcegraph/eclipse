package com.sourcegraph.cody.edits;

import com.sourcegraph.cody.WrappedRuntimeException;
import com.sourcegraph.cody.workspace.CodyListener;
import com.sourcegraph.cody.workspace.EditorState;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.codemining.AbstractCodeMining;
import org.eclipse.jface.text.codemining.ICodeMining;
import org.eclipse.jface.text.codemining.ICodeMiningProvider;
import org.eclipse.jface.text.codemining.LineHeaderCodeMining;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.ISourceViewerExtension5;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.StyledTextLineSpacingProvider;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

public class FileEditManager
    implements ICodeMiningProvider, StyledTextLineSpacingProvider, CodyListener {

  private final EditorState state;
  private final EditManager manager;
  private final ISourceViewer viewer;
  private final ISourceViewerExtension5 viewerExtension;

  private final Set<FileEdit> edits;
  private final Map<FileEdit, CompletableFuture<FileEditUi>> cache;
  private final Map<Integer, Integer> lineSpacings;

  private final Color backgroundRed;
  private final Color backgroundGreen;

  private final AtomicBoolean disposing = new AtomicBoolean(false);

  public FileEditManager(EditorState state, EditManager manager) {
    this.state = state;
    this.manager = manager;

    viewer = (ISourceViewer) state.editor.getAdapter(ITextViewer.class);
    viewerExtension = (ISourceViewerExtension5) viewer;

    edits = Collections.newSetFromMap(new ConcurrentHashMap<>());
    cache = new ConcurrentHashMap<>();
    lineSpacings = new ConcurrentHashMap<>();

    backgroundRed = new Color(Display.getDefault(), 255, 220, 220);
    backgroundGreen = new Color(Display.getDefault(), 220, 255, 220);
  }

  @Override
  public CompletableFuture<List<? extends ICodeMining>> provideCodeMinings(
      ITextViewer viewer, IProgressMonitor monitor) {
    var futures =
        edits.stream()
            .map(f -> cache.computeIfAbsent(f, this::createUi))
            .collect(Collectors.toList());
    return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
        .thenApply(
            v ->
                futures.stream()
                    .map(CompletableFuture::join)
                    .flatMap(f -> f.minings.stream())
                    .collect(Collectors.toList()));
  }

  @Override
  public Integer getLineSpacing(int lineIndex) {
    return lineSpacings.getOrDefault(lineIndex, 0) * viewer.getTextWidget().getLineHeight();
  }

  @Override
  public void install() {
    viewerExtension.setCodeMiningProviders(new ICodeMiningProvider[] {this});
    viewer.getTextWidget().setLineSpacingProvider(this);
    manager.register(state.uri, this);

    System.out.println(state.uri);
  }

  @Override
  public void dispose() {
    // This can be called either when agent is disposed, or the editor it is connected to is
    // closed. In the first case this may trigger dispose more than once.
    if (disposing.getAndSet(true)) {
      return;
    }

    backgroundRed.dispose();
    backgroundGreen.dispose();

    edits.forEach(this::disposeEditUi);
    viewer.getTextWidget().setLineSpacingProvider(null);
    viewerExtension.setCodeMiningProviders(new ICodeMiningProvider[] {});
  }

  public void addEdit(FileEdit edit) {
    edits.add(edit);
    viewerExtension.updateCodeMinings();
  }

  private CompletableFuture<FileEditUi> createUi(FileEdit edit) {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            var document = state.getDocument();
            var startLine = document.getLineOfOffset(edit.offset);
            var endLine = document.getLineOfOffset(edit.offset + edit.length);

            // Skip empty lines at the beginning of the edit. Eclipse is misbehaving if we try to
            // attach minings to them.
            while (document.get(document.getLineOffset(startLine), 1).matches("[\\n\\r]")) {
              startLine++;
            }

            var startOffset = document.getLineOffset(startLine);
            var afterEndOffset = document.getLineOffset(endLine);

            var position = new Position(startOffset, 1);
            var endPosition = new Position(afterEndOffset, 1);

            var cleanedText =
                edit.text
                    .lines()
                    .dropWhile(String::isBlank)
                    .collect(Collectors.joining("\n"))
                    .stripTrailing();
            var addedLines = (int) cleanedText.lines().count();
            lineSpacings.put(endLine - 1, addedLines);

            var removedText = document.get(startOffset, afterEndOffset - startOffset);
            var removedLines = (int) removedText.lines().count();

            var minings =
                List.of(
                    createIconMining(position, manager.codyIcon),
                    createButtonMining(position, "Accept", e -> accept(edit)),
                    createButtonMining(position, "Reject", e -> reject(edit)),
                    createAdded(endPosition, cleanedText, addedLines, removedText, removedLines));

            // We are rejecting edits of the part marked for removal, because it is easier than
            // keeping track of ranges during editing. If we want to change that in the future, we
            // can use `viewer.getDocument().addPosition(...)`
            VerifyListener listener =
                e -> {
                  // check if manualy edited range overlaps with a range of auto-edit. +1 to catch
                  // the `\n`.
                  if (e.end > edit.offset && e.start < edit.offset + edit.length + 1) {
                    e.doit = false;
                  }
                };

            Display.getDefault()
                .asyncExec(() -> viewer.getTextWidget().addVerifyListener(listener));

            return new FileEditUi(minings, endLine - 1, listener);
          } catch (BadLocationException e) {
            throw new WrappedRuntimeException(e);
          }
        });
  }

  private ICodeMining createButtonMining(
      Position position, String text, Consumer<MouseEvent> action) {
    try {
      var mining = new LineHeaderCodeMining(position, this, action) {};
      mining.setLabel(text);
      return mining;
    } catch (BadLocationException e) {
      throw new WrappedRuntimeException(e);
    }
  }

  private ICodeMining createIconMining(Position position, Image image) {
    try {
      var mining =
          new LineHeaderCodeMining(position, this, null) {
            @Override
            public Point draw(GC gc, StyledText textWidget, Color color, int x, int y) {
              gc.drawImage(image, x, y);
              var bounds = image.getBounds();
              return new Point(bounds.x + bounds.width, bounds.y + bounds.height);
            }
          };

      // Shouldn't be visible anywhere in the UI. It is needed because minings without a label are
      // invalid.
      mining.setLabel("icon");
      return mining;
    } catch (BadLocationException e) {
      throw new WrappedRuntimeException(e);
    }
  }

  private ICodeMining createAdded(
      Position endPosition,
      String addedText,
      int addedLines,
      String removedText,
      int removedLines) {
    var mining =
        new AbstractCodeMining(endPosition, this, null) {
          @Override
          public Point draw(GC gc, StyledText textWidget, Color color, int x, int y) {
            var lineHeight = textWidget.getLineHeight();
            var width = textWidget.getBounds().width;
            var addedHeight = lineHeight * addedLines;
            var removedHeight = lineHeight * removedLines;

            gc.setBackground(backgroundRed);
            gc.fillRectangle(0, y - removedHeight - addedHeight, width, removedHeight);
            gc.drawText(
                removedText,
                textWidget.getLeftMargin(),
                y - (addedLines + removedLines) * lineHeight);

            gc.setBackground(backgroundGreen);
            gc.fillRectangle(0, y - addedHeight, width, addedHeight);
            gc.drawText(addedText, textWidget.getLeftMargin(), y - addedHeight);

            // This annotation is not taking any space from the editor (line spacing already handles
            // that)
            return new Point(0, 0);
          }
        };
    mining.setLabel("removed");
    return mining;
  }

  private void accept(FileEdit edit) {
    try {
      state.getDocument().replace(edit.offset, edit.length, edit.text);
      disposeEditUi(edit);
    } catch (BadLocationException e) {
      throw new WrappedRuntimeException(e);
    }
  }

  private void reject(FileEdit edit) {
    disposeEditUi(edit);
  }

  private void disposeEditUi(FileEdit edit) {
    var editUi = cache.remove(edit).join();
    lineSpacings.remove(editUi.shiftedLine);
    edits.remove(edit);
    viewer.getTextWidget().removeVerifyListener(editUi.verifyListener);
    viewerExtension.updateCodeMinings();
  }

  private static class FileEditUi {
    final List<ICodeMining> minings;
    final int shiftedLine;
    final VerifyListener verifyListener;

    FileEditUi(List<ICodeMining> minings, int shiftedLine, VerifyListener verifyListener) {
      this.minings = minings;
      this.shiftedLine = shiftedLine;
      this.verifyListener = verifyListener;
    }
  }
}
