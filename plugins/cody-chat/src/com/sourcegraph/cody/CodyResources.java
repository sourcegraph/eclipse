package com.sourcegraph.cody;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class CodyResources {

  private static final ResourcePath WEBVIEW_PATH = ResourcePath.of("/resources/cody-webviews");
  private static final ResourcePath AGENT_PATH = ResourcePath.of("/resources/cody-agent");
  private static final ResourcePath NODE_BINARIES_PATH =
      ResourcePath.of("/resources/node-binaries");

  private static byte[] loadWebviewIndexBytes() {
    String html = loadResourceString(WEBVIEW_PATH.resolve("index.html").toString());
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

  private static String loadResourceString(String path) {
    return new String(loadResourceBytes(path), StandardCharsets.UTF_8);
  }

  private static byte[] loadResourceBytes(String path) {
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
    return loadResourceBytes(WEBVIEW_PATH.resolve(path).toString());
  }

  public static String loadAgentResourceString(String path) {
    return loadResourceString(AGENT_PATH.resolve(path).toString());
  }

  public static ResourcePath resolveAgentPath(String path) {
    return AGENT_PATH.resolve(path);
  }

  public static ResourcePath resolveNodeBinaryPath(String path) {
    return NODE_BINARIES_PATH.resolve(path);
  }
}
