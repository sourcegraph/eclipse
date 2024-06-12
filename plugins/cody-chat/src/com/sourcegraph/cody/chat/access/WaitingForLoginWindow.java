package com.sourcegraph.cody.chat.access;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

public class WaitingForLoginWindow extends Window {
	
	private Runnable cancelCallback;

	public WaitingForLoginWindow(Shell parentShell, Runnable cancellCallback) {
		super(parentShell);
		this.cancelCallback = cancellCallback;
		setShellStyle(SWT.APPLICATION_MODAL | SWT.BORDER);
	}

	@Override
	protected Control createContents(Composite parent) {
		var composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		
		var label = new Label(composite, SWT.NONE);
		label.setText("Loging in...");
		new ProgressBar(composite, SWT.INDETERMINATE);
		
		addCancelButton(composite);
		
		return composite;
	}

	private void addCancelButton(Composite parent) {
		var cancelButton = new Button(parent, SWT.PUSH);
		cancelButton.setText("Cancel");
		cancelButton.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(
				ISharedImages.IMG_ELCL_STOP));
		cancelButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				cancelCallback.run();
			}
		});
		cancelButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
	}

}
