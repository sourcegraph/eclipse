package com.sourcegraph.cody.chat.access;

import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;

import java.io.IOException;

import jakarta.inject.Singleton;

@Creatable
@Singleton
public class TokenStorage {
	
	private static String NODE_ID = "/com/sourcegraph/cody/chat";
	private static String KEY = "token";
	
	private ISecurePreferences preferences = SecurePreferencesFactory.getDefault().node(NODE_ID);
	
	public void put(String token) throws StorageException, IOException {
		preferences.put(KEY, token, true);
		preferences.flush();
	}
	
	public String get() throws StorageException {
		return preferences.get(KEY, "");
	}
	
	public void remove() {
		preferences.remove(KEY);
	}
}
