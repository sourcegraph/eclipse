package com.sourcegraph.cody.chat;

import static java.lang.System.out;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.ViewPart;

import jakarta.inject.Inject;

public class ChatView extends ViewPart {
	public static final String ID = "com.sourcegraph.cody.chat.ChatView";
	
	@Inject
	private Display display;

	@Override
	public void createPartControl(Composite parent) {
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
