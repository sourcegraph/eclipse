# Development guide

## Prerequisites

- Windows or Linux computer. The plugin itself can be installed on any platform,
  but the debug build inside Eclipse does not work on Apple Silicon macOS.
- Eclipse IDE for Eclipse Committers The project was created with version
  2024-03. It can be downloaded from
  [here](https://www.eclipse.org/downloads/packages/release/2024-03/r/eclipse-ide-eclipse-committers).

## Importing cloned repository into a lokal workspace

- Open Eclipse and select a path to a place you want to create your local
  workspace.
- From `File` select `Import`.
- In the pop-up select the `General` category and from it
  `Existing Projects into Workspace`. ![Import categories](img/import.png)
- On the next page choose `Select root directory` then using `Browse` open the
  root directory of this repository.
- Three projects should appear. Make sure that all are selected and click
  `Finish`

## Running and debugging the plugin

To run the project for the first time, right click on `minimal_run.launch` file
in the `Cody Feature` project. From the context menu select `Run as` then
`1. minimal_run`. You will be asked whether to clear the workspace data. If you
want to preserve settings and files from the previous run select `Don't clear`.

After running the project once, the launch configuration will be added to the
run and debug menus in the toolbar.

![Run or debug](img/run.png)

If you can open the Cody Chat view from `Window > Show view > Other`, the plugin
is loaded properly.

![View selection](img/cody-view.png)

## Manual build

Open `site.xml` file from the `Cody Update Site` project. In the `Site Map`
editor tab click the `Build All` button.

![Build all](img/build_all.png)

This will re-generate all the files in the `Cody Update Site` project. It is now
a complete update site. It can be used to install the plugin on any eclipse
instance.

![Plugin installation](img/install.png)

## Bazel build and import into IntelliJ IDEA

This repo has an optional Bazel build that enables the following:

- Build the plugin for different versions of Eclipse (2019-12, 2022-12, 2024-03)
- Import the plugin codebase into IntelliJ IDEA

The steps to import the 2024-03 version of the plugin into IntelliJ IDEA are as
follows:

1. Create a symlink to the `plugins/` directory of local your Eclipse
   installation. For example,
   `ln -svf /Applications/Eclipse.app/Contents/Eclipse/plugins eclipse-platforms/eclipse-2024-03-jars`.
2. Validate you can build the plugin locally by running
   `bazel build //:cody_eclipse_2024_03`. This step needs to succeed to move on
   to the next step.
3. Run the script `python scripts/bazel-sources.py` to generate a single jar
   file containing the source code from Eclipse. This step makes "Go to
   definition" show the Eclipse source code in IntelliJ.
4. Install the
   ["Bazel for IntelliJ"](https://plugins.jetbrains.com/plugin/8609-bazel-for-intellij)
   plugin from the JetBrains Marketplace.
5. Run the "Import Bazel Project" action from IntelliJ.
6. Select the workspace directory.
7. Select the "Import project view" option and select the file
   "2024-03.bazelproject". ![Import project view](img/import_project_view.png)
