- [Development guide](docs/development.md)

## Installing Cody for Eclipse

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

After you sign in, the "Cody" view will be blank because of an issue that we are still working on fixing.
