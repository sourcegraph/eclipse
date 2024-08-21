package com.sourcegraph.cody.logging;

import java.util.Arrays;
import java.util.function.Consumer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public class LogContentProvider extends LabelProvider
    implements IStructuredContentProvider, ILabelProvider {

  private Consumer<LogMessage> listener;

  private final LogView.Icons icons;

  public LogContentProvider(LogView.Icons icons) {
    this.icons = icons;
  }

  @Override
  public LogMessage[] getElements(Object inputElement) {
    var logger = (CodyLogger.Internal) inputElement;
    var envArr = logger.environment.toArray(new LogMessage[0]);
    var logArr = logger.backlog.toArray(new LogMessage[0]);
    var resultArr = Arrays.copyOf(envArr, envArr.length + logArr.length);
    System.arraycopy(logArr, 0, resultArr, envArr.length, logArr.length);
    return resultArr;
  }

  @Override
  public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    if (oldInput instanceof CodyLogger.Internal && listener != null) {
      ((CodyLogger.Internal) oldInput).removeListener(listener);
      listener = null;
    }

    if (newInput instanceof CodyLogger.Internal) {
      listener =
          (msg) ->
              Display.getDefault()
                  .asyncExec(
                      () -> {
                        viewer.refresh();
                        ((TableViewer) viewer).reveal(msg);
                      });
      ((CodyLogger.Internal) newInput).addListener(listener);
    }
  }

  @Override
  public String getText(Object element) {
    return ((LogMessage) element).message;
  }

  @Override
  public Image getImage(Object o) {
    switch (((LogMessage) o).kind) {
      case ERROR:
        return icons.error;
      case WARNING:
        return icons.warn;
      case INFO:
        return icons.info;
      case RECEIVED:
        return icons.received;
      case SENT:
        return icons.sent;
      default:
        return null;
    }
  }
}
