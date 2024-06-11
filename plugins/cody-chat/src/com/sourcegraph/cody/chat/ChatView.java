package com.sourcegraph.cody.chat;

import static java.lang.System.out;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.sourcegraph.cody.chat.access.LogInJob;

import jakarta.inject.Inject;

public class ChatView extends ViewPart {
	public static final String ID = "com.sourcegraph.cody.chat.ChatView";
	
	@Inject
	private Display display;

	@Override
	public void createPartControl(Composite parent) {
		addLogInAction();
		
		var browser = new Browser(parent, SWT.EDGE);
		browser.setText(loadIndex());
		
		new BrowserFunction(browser, "postMessage") {
			@Override
			public Object function(Object[] arguments) {
				out.println("From eclipse: " + arguments[0]);
				display.asyncExec(() -> 
					browser.execute("receiveMessage(\"received: " + arguments[0] + "\");")
				);
				return null;
			};
		};
	}
//	
//	class Xwindow extends Window {
//
//		protected Xwindow(Shell parentShell) {
//			super(parentShell);
//			setShellStyle(SWT.APPLICATION_MODAL);
//			
//		}
//		@Override
//		protected Control createContents(Composite parent) {
//			var content = super.createContents(parent);
//			// TODO Auto-generated method stu
//			var label = new Label(parent, 0);
//			label.setText("X");
//			var button = new Button(parent, SWT.PUSH);
//			button.setText("Cancel");
//			button.addListener(SWT.MouseDown, (e) -> out.println(e));
//			var progress = new ProgressBar(parent, SWT.INDETERMINATE);
//			return content;
//		}
//		
//	}

	private void addLogInAction() {
		var action = new Action() {
			@Override public void run() {
//				display.asyncExec(() -> {
//					var window = new Xwindow(display.getActiveShell());
//					window.create();
//					window.open();
//				});
				new LogInJob().schedule();
			}
		};
		
		action.setText("Log in");
		action.setToolTipText("Log in using GitHub");
		action.setImageDescriptor(
				PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(
						ISharedImages.IMG_TOOL_NEW_WIZARD));
		
		var toolBar = getViewSite().getActionBars().getToolBarManager();
		toolBar.add(action);
	}
	

	@Override
	public void setFocus() { }
		
	private String loadIndex() {
		try (var stream = getClass().getResourceAsStream("/resources/index.html")) {
			return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
			return "<h1> Cannot load index.html </h1>";
		}
	}
}
