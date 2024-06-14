package com.sourcegraph.cody.chat.access;

import com.sourcegraph.cody.chat.access.TokenStorage.Profile;
import jakarta.inject.Inject;
import java.util.List;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

public class TokenSelectionView extends ViewPart {

  public static final String ID = "com.sourcegraph.cody.chat.access.TokenSelectionView";

  public static final String DEFAULT_URL =
      "https://sourcegraph.com/user/settings/tokens/new/callback";

  @Inject IWorkbench workbench;

  @Inject TokenStorage tokenStorage;

  @Inject Display display;

  @Inject IEclipseContext context;

  @Inject Shell shell;

  private TableViewer viewer;
  private Action removeAction;
  private Action addAction;
  private Action doubleClickAction;

  class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
    @Override
    public String getColumnText(Object obj, int index) {
      var profile = (Profile) obj;
      return profile.name + " (" + profile.url + ")";
    }

    @Override
    public Image getColumnImage(Object obj, int index) {
      return getImage(obj);
    }

    @Override
    public Image getImage(Object obj) {
      var profile = (Profile) obj;
      if (profile.name == tokenStorage.getActiveProfileName().orElse(null)) {
        return workbench.getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
      } else {
        return null;
      }
    }
  }

  @Override
  public void createPartControl(Composite parent) {
    viewer = new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL);

    viewer.setContentProvider(
        new IStructuredContentProvider() {
          @SuppressWarnings("unchecked")
          @Override
          public Object[] getElements(Object inputElement) {
            return ((List<Profile>) inputElement).toArray();
          }
        });
    viewer.setInput(tokenStorage.getAllProfiless());
    viewer.setLabelProvider(new ViewLabelProvider());
    getSite().setSelectionProvider(viewer);
    makeActions();
    hookDoubleClickAction();
    contributeToActionBars();
    tokenStorage.addCallback(
        () -> {
          display.asyncExec(
              () -> {
                viewer.setInput(tokenStorage.getAllProfiless());
              });
        });
  }

  private void contributeToActionBars() {
    IActionBars bars = getViewSite().getActionBars();
    fillLocalToolBar(bars.getToolBarManager());
  }

  private void fillLocalToolBar(IToolBarManager manager) {
    manager.add(addAction);
    manager.add(removeAction);
  }

  private void makeActions() {
    removeAction =
        new Action() {
          public void run() {
            var selection = viewer.getStructuredSelection().getFirstElement();
            if (selection instanceof Profile) {
              tokenStorage.remove(((Profile) selection).name);
            }
          }
        };
    removeAction.setText("Remove");
    removeAction.setToolTipText("Remove profile");
    removeAction.setImageDescriptor(
        PlatformUI.getWorkbench()
            .getSharedImages()
            .getImageDescriptor(ISharedImages.IMG_ETOOL_DELETE));

    addAction =
        new Action() {
          public void run() {
            var profile = NewTokenDialog.ask(shell);
            if (profile.isPresent()) {
              new LogInJob(context, profile.get().name, profile.get().url).schedule();
            }
          }
        };
    addAction.setText("Add");
    addAction.setToolTipText("Add");
    addAction.setImageDescriptor(
        workbench.getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_ADD));

    doubleClickAction =
        new Action() {
          public void run() {
            IStructuredSelection selection = viewer.getStructuredSelection();
            var profile = (Profile) selection.getFirstElement();
            tokenStorage.setActiveProfileName(profile.name);
          }
        };
  }

  private void hookDoubleClickAction() {
    viewer.addDoubleClickListener(
        new IDoubleClickListener() {
          public void doubleClick(DoubleClickEvent event) {
            doubleClickAction.run();
          }
        });
  }

  @Override
  public void setFocus() {
    viewer.getControl().setFocus();
  }
}
