package com.sourcegraph.cody.testing;

import static org.junit.Assert.assertTrue;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SWTBotJunit4ClassRunner.class)
public class ChatViewTests {
  private static SWTWorkbenchBot bot;

  @BeforeClass
  public static void prepareBot() {
    bot = new SWTWorkbenchBot();
    bot.viewByTitle("Welcome").close();
  }

  @Test
  public void opened_view_should_load_a_browser() {
    bot.menu("Window").menu("Show View").menu("Other...").click();
    var shell = bot.shell("Show View");
    shell.activate();
    bot.tree().expandNode("Cody").select("Cody Chat");
    bot.button("Open").click();

    bot.sleep(3_000); // wait 3 seconds
    var browser =
        bot.browser(); // there shouldn't be any other browser widget, so we can choose the first
    // encountered
    assertTrue(browser.isPageLoaded());
    assertTrue(browser.getText().contains("<title>Cody</title>"));
  }
}
