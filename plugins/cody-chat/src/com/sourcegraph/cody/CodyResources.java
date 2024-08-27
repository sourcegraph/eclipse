package com.sourcegraph.cody;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import org.eclipse.core.runtime.Platform;

public class CodyResources {

  private static final ResourcePath WEBVIEW_ASSETS = ResourcePath.of("/resources/dist/webviews");
  private static final ResourcePath AGENT_ASSETS = ResourcePath.of("/resources/dist/cody-agent");
  private static final ResourcePath NODE_BINARIES_PATH =
      ResourcePath.of("/resources/node-binaries");
  private final Destinations destinations;

  private static byte[] indexHTML;

  public CodyResources(Destinations destinations) {
    this.destinations = destinations;
    copyAssetsTo(destinations);
  }

  public static String loadInjectedJS() {
    return loadResourceString("/resources/injected-script.js");
  }

  public static String loadInjectedCSS() {
    return loadResourceString("/resources/injected-styles.css");
  }

  public static String loadResourceString(ResourcePath path) {
    return loadResourceString(path.toString());
  }

  public static String loadResourceString(String path) {
    return new String(loadResourceBytes(path), StandardCharsets.UTF_8);
  }

  public static byte[] loadResourceBytes(ResourcePath path) {
    return loadResourceBytes(path.toString());
  }

  public static byte[] loadResourceBytes(String path) {
    try (var stream = CodyResources.class.getResourceAsStream(path)) {
      return stream.readAllBytes();
    } catch (IOException e) {
      throw new MessageOnlyException("failed to load resource " + path, e);
    }
  }

  public static byte[] loadWebviewBytes(String path) throws IOException {
    if (path.equals("index.html") && indexHTML != null) {
      return indexHTML;
    }
    return loadResourceBytes(WEBVIEW_ASSETS.resolve(path));
  }

  // Returns the path to the node binary. This first checks for a user-provided
  // node binary, then checks for a platform-specific binary provided in the resources.
  // The first time this is run, it will copy the node binary from the resources to the
  // destination directory.
  public Path getNodeJSLocation() {
    Path userPath = getUserProvidedNodePath();
    if (userPath != null) return userPath;

    String nodeExecutableName = getNodeExecutableName();
    if (!nodeExecutableName.isEmpty()) {
      var finalPath = destinations.node.resolve(nodeExecutableName);
      if (Files.isExecutable(finalPath)) {
        return finalPath;
      }
      try {
        copyResourcePath(NODE_BINARIES_PATH.resolve(nodeExecutableName), finalPath);
        markFileAsExecutable(finalPath);
      } catch (IOException e) {
        throw new MessageOnlyException("failed to copy node binary", e);
      }
      return finalPath;
    }

    throw new IllegalStateException(
        "Unable to infer the location of a Node.js installation. To fix this problem, set the VM"
            + " argument -Dcody.nodejs-executable and restart Eclipse.");
  }

  public Path getAgentEntry() {
    return destinations.agent.resolve("index.js");
  }

  public static void setIndexHTML(byte[] indexHTML) {
    CodyResources.indexHTML = indexHTML;
  }

  public static void copyAssetsTo(Destinations destinations) {
    try {
      copyFromAssetFile(destinations.agent, AGENT_ASSETS);
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

  private static Path getUserProvidedNodePath() {
    String userProvidedNode = System.getProperty("cody.nodejs-executable");
    if (userProvidedNode != null) {
      Path path = Paths.get(userProvidedNode);
      if (!Files.isExecutable(path)) {
        throw new IllegalArgumentException(
            "not executable: -Dcody.nodejs-executable=" + userProvidedNode);
      }
      return path;
    }
    return null;
  }

  private static String getNodeExecutableName() {
    if (Platform.getOS().equals(Platform.OS_WIN32)) {
      return "node-win-x64.exe";
    } else if (Platform.OS.isMac() && Platform.getOSArch().equals(Platform.ARCH_AARCH64)) {
      return "node-macos-arm64";
    }
    return "";
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

  private static void markFileAsExecutable(Path path) throws IOException {
    if (Platform.getOS().equals(Platform.OS_WIN32)) {
      // Windows doesn't have an executable bit.
      return;
    }
    Files.setPosixFilePermissions(path, EnumSet.of(PosixFilePermission.OWNER_EXECUTE));
  }

  public static class Destinations {
    private Destinations() {}

    private Path agent;
    private Path node;
  }

  public static class DestinationsBuilder {
    private final Destinations paths = new Destinations();

    public DestinationsBuilder withAgent(Path agent) {
      this.paths.agent = agent;
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
      if (this.paths.node == null) {
        throw new IllegalStateException("node must be set");
      }
      return this.paths;
    }
  }
}
