- [Development guide](docs/development.md)

## Installing Cody for Eclipse

Currently the only version of Eclipse that Cody supports is Eclipse 2024-03 (4.31.0).

### Short version

Add the update site URL https://sourcegraph.github.io/eclipse and follow the installation instructions.

Alternatively, download the jar file and add it to the dropins folder of your Eclipse installation.

- Download link https://sourcegraph.github.io/eclipse/features/cody_feature_0.1.0.202407041638.jar

### Long version

First, open "Help > Install New Software"

Next, add the site URL https://sourcegraph.github.io/eclipse

If everything goes well, you should see the "Cody" category in the list of available plugins.

![Installing the plugin](docs/img/install-site-url.png)

Click "Next" and follow the installation instructions.

After you have completed the installation and restarted Eclipse, you should see the "Cody" view in the "Window > Show View > Other" menu.

![Cody view](docs/img/cody-view.png)

Once you open the "Cody" view, you should see a button to sign into your Sourcegraph account.

![Sign in](docs/img/sign-in.png)

After you sign in, you'll see the Cody chat view and can begin chatting with your codebase!

![Cody Chat](docs/img/cody-chat.png)

# Troubleshooting

## _The first time I open Cody Chat, I just see a white screen_

This can happen if Eclipse prompted you to setup a password for secure storage and Cody timed out waiting. Simply close and re-open the Cody Chat.

## _General Tips_

You can open the Cody Log view using the same steps as above, but instead selecting Cody Log.
![Cody Log](docs/img/cody-log.png)

This will include quite a bit of information about what Cody is doing, including any errors. There is a copy button at the top right of the log view that you can use to copy the log to your clipboard and sent to us. Be careful not to include any sensitive information as the log communication is verbose and may contain tokens.

Additionally, Eclipse's built-in Error Log can be used to view any uncaught exceptions and view their stack traces. You can open the Error Log using the "Window > Show View > Error Log" menu.
