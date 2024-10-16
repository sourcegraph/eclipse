package com.sourcegraph.cody.protocol_generated;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import java.lang.reflect.Type;

public abstract class ProtocolAuthStatus {
  public static JsonDeserializer<ProtocolAuthStatus> deserializer() {
    return (element, _type, context) -> {
    	System.out.println(element.getAsJsonObject());
      switch (element.getAsJsonObject().get("status").getAsString()) {
        case "authenticated": return context.deserialize(element, ProtocolAuthenticatedAuthStatus.class);
        case "unauthenticated": return context.deserialize(element, ProtocolUnauthenticatedAuthStatus.class);
        default: throw new RuntimeException("Unknown discriminator " + element);
      }
    };
  }
public static final class ProtocolAuthenticatedAuthStatus extends ProtocolAuthStatus {
  public StatusEnum status; // Oneof: authenticated
  public Boolean authenticated;
  public String endpoint;
  public String username;
  public Boolean isFireworksTracingEnabled;
  public Boolean hasVerifiedEmail;
  public Boolean requiresVerifiedEmail;
  public String primaryEmail;
  public String displayName;
  public String avatarURL;
  public Boolean pendingValidation;
  public java.util.List<OrganizationsParams> organizations;

  public enum StatusEnum {
    @com.google.gson.annotations.SerializedName("authenticated") Authenticated,
  }
}
public static final class ProtocolUnauthenticatedAuthStatus extends ProtocolAuthStatus {
  public StatusEnum status; // Oneof: unauthenticated
  public Boolean authenticated;
  public String endpoint;
  public Boolean showNetworkError;
  public Boolean showInvalidAccessTokenError;
  public Boolean pendingValidation;

  public enum StatusEnum {
    @com.google.gson.annotations.SerializedName("unauthenticated") Unauthenticated,
  }
}
}

