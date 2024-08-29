package com.sourcegraph.cody.chat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

public class DisableableBrowser extends Composite {
    private final Browser browser;
    private final Composite overlayComposite;
    private final StackLayout stackLayout;

    public DisableableBrowser(Composite parent, int style) {
        super(parent, style);
        stackLayout = new StackLayout();
        setLayout(stackLayout);

        browser = new Browser(this, SWT.NONE);

        overlayComposite = new Composite(this, SWT.NONE);
        overlayComposite.setLayout(new FillLayout());
        Composite overlay = new Composite(overlayComposite, SWT.NO_BACKGROUND);
        overlay.setBackground(new Color(Display.getCurrent(), 0, 0, 0, 128)); // Semi-transparent black

        stackLayout.topControl = overlay;
    }

    public Browser getBrowser() {
        return browser;
    }

    public void setEnabled(boolean enabled) {
        stackLayout.topControl = enabled ? browser : overlayComposite;
        layout();
    }

    @Override
    public void dispose() {
        if (overlayComposite != null && !overlayComposite.isDisposed()) {
            overlayComposite.dispose();
        }
        super.dispose();
    }
}