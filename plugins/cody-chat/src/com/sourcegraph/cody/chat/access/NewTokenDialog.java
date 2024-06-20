package com.sourcegraph.cody.chat.access;

import com.sourcegraph.cody.chat.access.TokenStorage.Profile;
import java.net.URI;
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

    Label urlLabel = new Label(composite, SWT.NONE);
    urlLabel.setText("Sourcegraph Instance URL");
    urlText = new Text(composite, SWT.BORDER);
    var urlLayout = new GridData();
    urlLayout.grabExcessHorizontalSpace = true;
    urlLayout.horizontalAlignment = GridData.FILL;
    urlText.setLayoutData(urlLayout);
    urlText.setText(TokenSelectionView.DEFAULT_URL);
    // TODO: change the text of the "OK" button to something like "Open browser"

    return composite;
  }

  @Override
  protected void okPressed() {
    url = urlText.getText();
    name = URI.create(url).getHost();
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
