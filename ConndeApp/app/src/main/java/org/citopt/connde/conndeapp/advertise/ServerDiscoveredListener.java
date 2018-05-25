package org.citopt.connde.conndeapp.advertise;

/**
 * Created by rosso on 25.09.17.
 */

public interface ServerDiscoveredListener {
  class ServerDiscoveredEvent {
    private DiscoveredServer discoveredServer;

    public ServerDiscoveredEvent(DiscoveredServer discoveredServer) {
      this.discoveredServer = discoveredServer;
    }

    public DiscoveredServer getDiscoveredServer() {
      return discoveredServer;
    }
  }

  void onServerDiscovered(ServerDiscoveredEvent event);
}
