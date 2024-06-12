import os
import sys


def append_to_build_scala(file_path, build_scala):
    if file_path.endswith("-sources.jar"):
        build_scala.write(f"//> using sourceJar {file_path}\n")
    else:
        build_scala.write(f"//> using jar {file_path}\n")


def main():
    build_scala_path = "build.scala"
    eclipse_plugin_config_path = os.path.join(
        os.path.dirname(__file__), "eclipse-plugins-directory.txt"
    )
    if not os.path.isfile(eclipse_plugin_config_path):
        print(
            f"Error: File {eclipse_plugin_config_path} does not exist. "
            + "To fix this problem, create the file and write the absolute file "
            + "path to your Eclipse installation. On macOS, you can try running this command:\n"
            + "  echo '/Applications/Eclipse.app/Contents/Eclipse/plugins' > '{eclipse_plugin_config_path}'",
            file=sys.stderr,
        )
        sys.exit(1)

    with open(eclipse_plugin_config_path, "r") as f:
        eclipse_plugins_dir = f.read().strip()

    if not os.path.isdir(eclipse_plugins_dir):
        print(f"Error: Directory {eclipse_plugins_dir} does not exist", file=sys.stderr)
        sys.exit(1)

    with open(build_scala_path, "w") as build_scala:
        build_scala.write("//> using files plugins/cody-chat/src\n")

        for file in os.listdir(eclipse_plugins_dir):
            file_path = os.path.join(eclipse_plugins_dir, file)
            if file_path.endswith(".source_"):
                without_source = file_path.replace(".source", "")
                new_path = without_source.replace(".jar", "-sources.jar")
                print(new_path)
                os.symlink(file_path, new_path)
                append_to_build_scala(new_path, build_scala)
            else:
                append_to_build_scala(file_path, build_scala)

    os.system("scala-cli setup-ide build.scala")
    print(
        "Done. Import the project in IntelliJ IDEA via BSP. "
        + "Make sure you have installed the Scala plugin. "
        + "Docs: https://www.jetbrains.com/help/idea/bsp-support.html"
    )


if __name__ == "__main__":
    main()
