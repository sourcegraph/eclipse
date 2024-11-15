package com.sourcegraph.cody.chat.access;

import com.sourcegraph.cody.WrappedRuntimeException;
import jakarta.inject.Singleton;
import java.io.IOException;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;

@Creatable
@Singleton
public class TokenStorage {

  // Path for settings node should start with a plugin ID
  private static final String AGENT_SECURE_NODE_ID = "/com/sourcegraph/cody/agent/secrets";

  private final ISecurePreferences agentSecureStorage =
      SecurePreferencesFactory.getDefault().node(AGENT_SECURE_NODE_ID);

  // Profiles are associated with a workspace. To associate them with Eclipse
  // installation change `InstanceScope` to `ConfigurationScope`.

  public String getAgentSecret(String key) {
    try {
      return agentSecureStorage.get(key, "");
    } catch (StorageException e) {
      throw new WrappedRuntimeException(e); // Escalating unlikely exceptions
    }
  }

  // cody please many any suggestion of a new method for testing PR changes
  public void deleteAgentSecret(String key) {
    try {
      agentSecureStorage.remove(key);
      agentSecureStorage.flush();
    } catch (IOException e) {
      throw new WrappedRuntimeException(e); // Escalating unlikely exceptions
    }
  }

  public void setAgentSecret(String key, String value) {
    try {
      agentSecureStorage.put(key, value, true);
      agentSecureStorage.flush();
    } catch (StorageException | IOException e) {
      throw new WrappedRuntimeException(e); // Escalating unlikely exceptions
    }
  }

  public void deleteAgentSecret(String key) {
    try {
      agentSecureStorage.remove(key);
      agentSecureStorage.flush();
    } catch (IOException e) {
      throw new WrappedRuntimeException(e); // Escalating unlikely exceptions
    }
  }
}
