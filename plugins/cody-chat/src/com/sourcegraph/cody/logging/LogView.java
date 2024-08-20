package com.sourcegraph.cody.logging;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

public class LogView extends ViewPart {

  private TableViewer viewer;

  private LogContentProvider contentProvider;

  @Override
  public void createPartControl(Composite parent) {
    parent.setLayout(new GridLayout(2, false));

    var icons = new Icons();
    contentProvider = new LogContentProvider(icons);

    viewer = new TableViewer(parent);
    viewer.setContentProvider(contentProvider);
    viewer.setLabelProvider(contentProvider);
    viewer.setInput(CodyLogger.INSTANCE);
    viewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 0, 1));

    var filterPanel = new FilterPanel(parent, icons);
    filterPanel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1));

    viewer.setFilters(filterPanel.filter);

    addClearAction();
    addCopyAction();
  }

  private void addCopyAction() {
    var action =
        new Action("Copy") {
          @Override
          public void run() {
            var builder = new StringBuilder();
            for (var message : contentProvider.getElements(CodyLogger.INSTANCE)) {
              builder.append(message.toString()).append("\n\n");
            }
            new Clipboard(Display.getDefault())
                .setContents(
                    new Object[] {builder.toString()}, new Transfer[] {TextTransfer.getInstance()});
          }
        };
    action.setText("Copy logs");
    action.setToolTipText("Copy logs");
    action.setImageDescriptor(
        PlatformUI.getWorkbench()
            .getSharedImages()
            .getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
    getViewSite().getActionBars().getToolBarManager().add(action);
  }

  private void addClearAction() {
    var action =
        new Action("Clear") {
          @Override
          public void run() {
            CodyLogger.INSTANCE.clear();
            viewer.refresh();
          }
        };
    action.setText("Clear logs");
    action.setToolTipText("Clear logs");
    action.setImageDescriptor(
        PlatformUI.getWorkbench()
            .getSharedImages()
            .getImageDescriptor(ISharedImages.IMG_ELCL_REMOVEALL));
    getViewSite().getActionBars().getToolBarManager().add(action);
  }

  @Override
  public void setFocus() {}

  private class FilterPanel extends Composite {

    public final ViewerFilter filter;

    public FilterPanel(Composite composite, Icons icons) {
      super(composite, SWT.NONE);
      setLayout(new GridLayout(1, false));

      Text searchBar = new Text(this, SWT.BORDER);
      searchBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
      searchBar.setMessage("filter...");
      searchBar.addModifyListener(e -> viewer.refresh());

      Button errorCheckBox = createCheckbox("Errors", icons.error);
      Button warnCheckBox = createCheckbox("Warnings", icons.warn);
      Button infoCheckBox = createCheckbox("Info", icons.info);
      Button receivedCheckBox = createCheckbox("Received messages", icons.received);
      Button sentCheckBox = createCheckbox("Sent messages", icons.sent);

      filter =
          new ViewerFilter() {
            @Override
            public boolean select(Viewer viewer, Object ignored, Object data) {
              var searchText = searchBar.getText().trim();
              var message = (LogMessage) data;

              if (!searchText.isEmpty() && !message.message.contains(searchText)) {
                return false;
              }
              ;
              switch (message.kind) {
                case ERROR:
                  return errorCheckBox.getSelection();
                case WARNING:
                  return warnCheckBox.getSelection();
                case INFO:
                  return infoCheckBox.getSelection();
                case RECEIVED:
                  return receivedCheckBox.getSelection();
                case SENT:
                  return sentCheckBox.getSelection();
              }
              return true;
            }
          };
    }

    private Button createCheckbox(String text, Image icon) {
      var checkbox = new Button(this, SWT.CHECK);
      checkbox.setImage(icon);
      checkbox.setText(text);
      checkbox.setSelection(true);
      checkbox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
      checkbox.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> viewer.refresh()));
      return checkbox;
    }
  }

  public static class Icons {
    public final Image error = load(ISharedImages.IMG_OBJS_ERROR_TSK);
    public final Image warn = load(ISharedImages.IMG_OBJS_WARN_TSK);
    public final Image info = load(ISharedImages.IMG_OBJS_INFO_TSK);
    public final Image received = load(ISharedImages.IMG_TOOL_FORWARD);
    public final Image sent = load(ISharedImages.IMG_TOOL_BACK);

    private static Image load(String id) {
      return PlatformUI.getWorkbench().getSharedImages().getImage(id);
    }
  }
}
