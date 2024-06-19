package com.sourcegraph.cody;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class CodyActivator extends AbstractUIPlugin {

	public CodyActivator() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		System.out.println("START ACTIVATOR!");
	}
	
	@Override
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
//		context.
		System.out.println("STOP ACTIVATOR!");
	}

}
