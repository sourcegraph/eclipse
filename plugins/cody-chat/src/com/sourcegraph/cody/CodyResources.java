package com.sourcegraph.cody;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class CodyResources {

  public static String loadIndex() {
    return loadResourceString("/resources/index.html");
  }

  public static String loadCodyIndex() {
    String content = loadResourceString("/resources/cody-webviews/index.html");
    return content
        .replace("'self'", "'self' https://*.sourcegraphstatic.com")
        .replace(
            "<head>",
            String.format(
                "<head><script>%s</script><style>%s</style>", loadInjectedJS(), loadInjectedCSS()));
  }

  public static String loadInjectedJS() {
    return loadResourceString("/resources/injected-script.js");
  }

  public static String loadInjectedCSS() {
    return loadResourceString("/resources/injected-styles.css");
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
}
