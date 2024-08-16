package com.sourcegraph.cody;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import org.eclipse.core.runtime.Platform;

public class CodyResources {

  private static final ResourcePath WEBVIEW_ASSETS = ResourcePath.of("/resources/webviews");
  private static final ResourcePath AGENT_ASSETS = ResourcePath.of("/resources/cody-agent");
  private static final ResourcePath NODE_BINARIES_PATH =
      ResourcePath.of("/resources/node-binaries");

  private static byte[] loadWebviewIndexBytes() {
    String html = loadResourceString(WEBVIEW_ASSETS.resolve("index.html").toString());
    var start =
        indexOrOrCrash(
            html,
            "<!-- This content security policy also implicitly disables inline scripts and styles."
                + " -->");
    var endMarker = "<!-- DO NOT REMOVE: THIS FILE IS THE ENTRY FILE FOR CODY WEBVIEW -->";
    var end = indexOrOrCrash(html, endMarker);
    html = html.substring(0, start) + html.substring(end + endMarker.length() + 1);
    return html.replace("'self'", "'self' https://*.sourcegraphstatic.com")
        .replace(
            "<head>",
            String.format(
                "<head><script>%s</script><style>%s</style>", loadInjectedJS(), loadInjectedCSS()))
        .getBytes(StandardCharsets.UTF_8);
  }

  private static int indexOrOrCrash(String string, String substring) {
    var index = string.indexOf(substring);
    if (index < 0) {
      throw new IllegalArgumentException(
          String.format("substring '%s' does not exist in string '%s'", substring, string));
    }
    return index;
  }

  private static String loadInjectedJS() {
    return loadResourceString("/resources/injected-script.js");
  }

  private static String loadInjectedCSS() {
    return loadResourceString("/resources/injected-styles.css");
  }

  public static String loadResourceString(ResourcePath path) {
    return loadResourceString(path.toString());
  }

  public static String loadResourceString(String path) {
    return new String(loadResourceBytes(path), StandardCharsets.UTF_8);
  }

  public static byte[] loadResourceBytes(String path) {
    try (var stream = CodyResources.class.getResourceAsStream(path)) {
      return stream.readAllBytes();
    } catch (IOException e) {
      throw new MessageOnlyException("failed to load resource " + path, e);
    }
  }

  public static byte[] loadWebviewBytes(String path) {
    if (path.isEmpty() || path.equals("/") || path.endsWith("index.html")) {
      return loadWebviewIndexBytes();
    }
    return loadResourceBytes(WEBVIEW_ASSETS.resolve(path).toString());
  }

  public static ResourcePath resolveNodeBinaryPath(String path) {
    return NODE_BINARIES_PATH.resolve(path);
  }

  public static Path getNodeJSLocation() {
    String userProvidedNode = System.getProperty("cody.nodejs-executable");
    if (userProvidedNode != null) {
      var path = Paths.get(userProvidedNode);
      if (!Files.isExecutable(path)) {
        throw new IllegalArgumentException(
            "not executable: -Dcody.nodejs-executable=" + userProvidedNode);
      }
      return path;
    }
    // We only support Windows at this time. The binary for Node.js is included in the plugin JAR.
    if (Platform.getOS().equals(Platform.OS_WIN32)) {
      String nodeExecutableName = "node-win-x64.exe";
      return NODE_BINARIES_PATH.resolve(nodeExecutableName).toPath();
    }

    throw new IllegalStateException(
        "Unable to infer the location of a Node.js installation. To fix this problem, set the VM"
            + " argument -Dcody.nodejs-executable and restart Eclipse.");
  }

  public static void copyAssetsTo(Destinations destinations) {
    try {
      copyFromAssetFile(destinations.webviews, WEBVIEW_ASSETS);
      copyFromAssetFile(destinations.agent, AGENT_ASSETS);
      copyNodeBinaries(destinations.node);
    } catch (IOException e) {
      throw new MessageOnlyException("failed to copy assets", e);
    }
  }

  private static void copyFromAssetFile(Path dir, ResourcePath assetsDir) throws IOException {
    String assets = CodyResources.loadResourceString(assetsDir.resolve("assets.txt"));
    Files.createDirectories(dir);
    for (String asset : assets.split("\n")) {
      copyResourcePath(assetsDir.resolve(asset), dir.resolve(asset));
    }
  }

  /**
   * Returns the path to a Node.js executable to run the agent.
   *
   * <p>We are bundling the correct version of Node.js with the agent on Windows. It is used by
   * default. Users can provide the location through the system property code.nodejs-executable.
   * Down the road, we may consider publishing separate plugin jars for each OS (cody-win.jar,
   * cody-macos.jar, etc).
   *
   * <p>Importantly, we don't try to just shell out to "node". While this may work in some cases, we
   * have no guarantee what Node version this gives us (and the agent requires Node >=v17).
   */
  private static void copyNodeBinaries(Path dir) throws IOException {
    String userProvidedNode = System.getProperty("cody.nodejs-executable");
    if (userProvidedNode != null) {
      Path path = Paths.get(userProvidedNode);
      if (!Files.isExecutable(path)) {
        throw new IllegalArgumentException(
            "not executable: -Dcody.nodejs-executable=" + userProvidedNode);
      }
      return;
    }
    // We only support Windows at this time. The binary for Node.js is included in the plugin JAR.
    if (Platform.getOS().equals(Platform.OS_WIN32)) {
      String nodeExecutableName = "node-win-x64.exe";
      Path path = dir.resolve(nodeExecutableName);
      if (!Files.isRegularFile(path)) {
        copyResourcePath(NODE_BINARIES_PATH.resolve(nodeExecutableName), path);
      }
      return;
    }

    throw new IllegalStateException(
        "Unable to infer the location of a Node.js installation. To fix this problem, set the VM"
            + " argument -Dcody.nodejs-executable and restart Eclipse.");
  }

  private static void copyResourcePath(ResourcePath path, Path target) throws IOException {
    try (InputStream in = CodyResources.class.getResourceAsStream(path.toString())) {
      if (in == null) {
        throw new IllegalStateException(
            String.format(
                "not found: %s. To fix this problem, "
                    + "run `make all` from the root directory and try again.",
                path));
      }
      Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
    }
  }

  public static class Destinations {
    private Destinations() {}

    private Path agent;
    private Path webviews;
    private Path node;
  }

  public static class DestinationsBuilder {
    private final Destinations paths = new Destinations();

    public DestinationsBuilder withAgent(Path agent) {
      this.paths.agent = agent;
      return this;
    }

    public DestinationsBuilder withWebviews(Path webviews) {
      this.paths.webviews = webviews;
      return this;
    }

    public DestinationsBuilder withNode(Path node) {
      this.paths.node = node;
      return this;
    }

    public Destinations build() {
      if (this.paths.agent == null) {
        throw new IllegalStateException("agent must be set");
      }
      if (this.paths.webviews == null) {
        throw new IllegalStateException("webviews must be set");
      }
      if (this.paths.node == null) {
        throw new IllegalStateException("node must be set");
      }
      return this.paths;
    }
  }
}
