package com.sourcegraph.cody.protocol_generated;

public final class RemoteRepoFetchState {
  public StateEnum state; // Oneof: paused, fetching, errored, complete
  public CodyError error;

  public enum StateEnum {
    @com.google.gson.annotations.SerializedName("paused") Paused,
    @com.google.gson.annotations.SerializedName("fetching") Fetching,
    @com.google.gson.annotations.SerializedName("errored") Errored,
    @com.google.gson.annotations.SerializedName("complete") Complete,
  }
}

