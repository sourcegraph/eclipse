package com.sourcegraph.cody;

import org.eclipse.ui.IStartup;

public class CodyStartup implements IStartup {
  @Override
  public void earlyStartup() {
    System.out.println("IStartup2()");
    //    Launcher<CodyAgentServer> launcher = new Launcher.Builder<CodyAgentServer>().create();
    //    launcher.startListening();
  }
}
