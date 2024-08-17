package com.sourcegraph.cody;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.eclipse.core.runtime.Platform;

public class CodyResources {

  private static final ResourcePath WEBVIEW_ASSETS = ResourcePath.of("/resources/dist/webviews");
  private static final ResourcePath AGENT_ASSETS = ResourcePath.of("/resources/dist/cody-agent");
  private static final ResourcePath NODE_BINARIES_PATH =
      ResourcePath.of("/resources/node-binaries");

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

  public static byte[] loadResourceBytes(String path) {
    try (var stream = CodyResources.class.getResourceAsStream(path)) {
      return stream.readAllBytes();
    } catch (IOException e) {
      throw new MessageOnlyException("failed to load resource " + path, e);
    }
  }

  public static byte[] loadWebviewBytes(String path) {
    System.out.println("Loading path: " + path);
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
    if (Platform.getOS().equals(Platform.OS_WIN32)) {
      String nodeExecutableName = "node-win-x64.exe";
      return NODE_BINARIES_PATH.resolve(nodeExecutableName).toPath();
    }

    throw new IllegalStateException(
        "Unable to infer the location of a Node.js installation. To fix this problem, set the VM"
            + " argument -Dcody.nodejs-executable and restart Eclipse.");
  }

  public static Path getAgentEntry() {
    return AGENT_ASSETS.resolve("index.js").toPath();
  }
}
