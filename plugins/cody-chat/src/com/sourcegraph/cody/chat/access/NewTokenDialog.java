package com.sourcegraph.cody.chat.access;

import com.sourcegraph.cody.chat.access.TokenStorage.Profile;
import java.util.Optional;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class NewTokenDialog extends Dialog {

  private Text nameText;
  private Text urlText;

  private String name;
  private String url;

  private NewTokenDialog(Shell parentShell) {
    super(parentShell);
  }

  @Override
  protected Control createDialogArea(Composite parent) {
    var composite = new Composite(parent, SWT.NONE);
    composite.setLayout(new GridLayout(2, false));

    Label nameLabel = new Label(composite, SWT.NONE);
    nameLabel.setText("Name");
    nameText = new Text(composite, SWT.BORDER);
    var nameLayout = new GridData();
    nameLayout.grabExcessHorizontalSpace = true;
    nameLayout.horizontalAlignment = GridData.FILL;
    nameText.setLayoutData(nameLayout);

    Label urlLabel = new Label(composite, SWT.NONE);
    urlLabel.setText("URL");
    urlText = new Text(composite, SWT.BORDER);
    var urlLayout = new GridData();
    urlLayout.grabExcessHorizontalSpace = true;
    urlLayout.horizontalAlignment = GridData.FILL;
    urlText.setLayoutData(urlLayout);
    urlText.setText(TokenSelectionView.DEFAULT_URL);

    return composite;
  }

  @Override
  protected void okPressed() {
    name = nameText.getText();
    url = urlText.getText();
    super.okPressed();
  }

  static Optional<Profile> ask(Shell shell) {
    var dialog = new NewTokenDialog(shell);
    if (dialog.open() == Window.OK) {
      return Optional.of(new Profile(dialog.name, dialog.url));
    } else {
      return Optional.empty();
    }
  }
}
