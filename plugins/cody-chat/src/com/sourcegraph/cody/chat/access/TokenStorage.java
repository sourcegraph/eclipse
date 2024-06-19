package com.sourcegraph.cody.chat.access;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.INodeChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.NodeChangeEvent;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.equinox.security.storage.EncodingUtils;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.osgi.service.prefs.BackingStoreException;

import com.sourcegraph.cody.CodyAgent;
import com.sourcegraph.cody.WrappedRuntimeException;
import com.sourcegraph.cody.protocol_generated.ExtensionConfiguration;

import jakarta.inject.Singleton;

@Creatable
@Singleton
public class TokenStorage {

  public static class Profile {
    public final String name;
    public final String url;

    public Profile(String name, String url) {
      this.name = name;
      this.url = url;
    }
  }

  private static String SECURE_NODE_ID = "/com/sourcegraph/cody/chat/tokens";
  private static String SETTING_NODE_ID =
      "cody-chat/tokens"; // Path for settings node should start with a plugin ID
  private static String PROFILES_NODE_ID = SETTING_NODE_ID + "/profiles";

  private ISecurePreferences secureStorage =
      SecurePreferencesFactory.getDefault().node(SECURE_NODE_ID);
  private IEclipsePreferences settingsStorage = InstanceScope.INSTANCE.getNode(SETTING_NODE_ID);
  private IEclipsePreferences profileStorage = InstanceScope.INSTANCE.getNode(PROFILES_NODE_ID);
  // Profiles are associated with a workspace. To associate them with Eclipse
  // installation change `InstanceScope` to `ConfigurationScope`.

  private String activeProfile = null;

  public List<Profile> getAllProfiles() {
    List<Profile> result = new ArrayList<>();
    try {
      profileStorage.accept(
          (node) -> {
            if (node.absolutePath().endsWith(PROFILES_NODE_ID)) {
              return true;
            } else {
              result.add(new Profile(node.get("name", null), node.get("url", null)));
              return false;
            }
          });
    } catch (BackingStoreException e) {
      throw new RuntimeException(e); // Escalating unlikely exception
    }
    return result;
  }

  public void addCallback(Runnable callback) {
    profileStorage.addNodeChangeListener(
        new INodeChangeListener() {
          @Override
          public void added(NodeChangeEvent event) {
            callback.run();
          }

          @Override
          public void removed(NodeChangeEvent event) {
            callback.run();
          }
        });
    settingsStorage.addPreferenceChangeListener(
        event -> {
          callback.run();
        });
  }

  public void put(String name, String url, String token) {
    var id = safeID(name);
    var node = profileStorage.node(id);
    node.put("name", name);
    node.put("url", url);

    try {
      secureStorage.put(id, token, true);
      profileStorage.flush();
      secureStorage.flush();
    } catch (StorageException | BackingStoreException | IOException e) {
      throw new WrappedRuntimeException(e); // Escalating unlikely exceptions
    }
  }

  public String getToken(String name) {
    try {
      String id = safeID(name);
      return secureStorage.get(id, "");
    } catch (StorageException e) {
      throw new WrappedRuntimeException(e); // Escalating unlikely exception
    }
  }

  public void setActiveProfileName(String name) {
    activeProfile = name;
    settingsStorage.put("last", name);
    try {
      settingsStorage.flush();
    } catch (BackingStoreException e) {
      throw new RuntimeException(e); // Escalating unlikely exception
    }
    updateCodyAgentConfiguration();
  }

  public void updateCodyAgentConfiguration() {
    var profileName = this.getActiveProfileName().orElse(null);
    System.out.println("PROFILE NAME " + profileName);
    if (profileName != null) {
      var configuration = new ExtensionConfiguration();
      configuration.serverEndpoint = this.getServerEndpoint(profileName);
      configuration.accessToken = this.getToken(profileName);
      configuration.customConfiguration = new HashMap<>();
      CodyAgent.onConfigChange(configuration);
    }
  }

  public Optional<String> getActiveProfileName() {
    if (activeProfile == null) {
      try {
        var fromPreviousSession = settingsStorage.get("last", null);
        if (fromPreviousSession != null && profileStorage.nodeExists(safeID(fromPreviousSession))) {
          activeProfile = fromPreviousSession;
        }
      } catch (BackingStoreException e) {
        throw new RuntimeException(e); // Escalating unlikely exception
      }
    }
    return Optional.ofNullable(activeProfile);
  }

  public void remove(String name) {
    var id = safeID(name);
    try {
      profileStorage.node(id).removeNode();
      secureStorage.remove(id);
      profileStorage.flush();
      secureStorage.flush();
    } catch (BackingStoreException | IOException e) {
      throw new RuntimeException(e); // Escalating unlikely exception
    }
  }

  private String safeID(String name) {
    // Node ids in secure storage can only contain ASCII chars
    // from open ranges (32, 47) and (47, 126).
    return EncodingUtils.encodeSlashes(EncodingUtils.encodeBase64(name.getBytes()));
  }

  public String getServerEndpoint(String profileName) {
    for (var profile : getAllProfiles()) {
      if (profile.name.equals(profileName)) {
        return String.format("https://%s/", URI.create(profile.url).getHost());
      }
    }
    return null;
  }
}
