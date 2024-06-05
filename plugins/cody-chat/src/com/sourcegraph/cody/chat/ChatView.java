package com.sourcegraph.cody.chat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;

public class ChatView extends ViewPart {
	public static final String ID = "com.sourcegraph.cody.chat.ChatView";

	@Override
	public void createPartControl(Composite parent) {
		Label label = new Label(parent, SWT.CENTER);
		label.setText("Chat View placeholder");
	}

	@Override
	public void setFocus() {
		
	}
}
