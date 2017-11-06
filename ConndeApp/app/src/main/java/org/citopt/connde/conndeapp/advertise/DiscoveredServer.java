package org.citopt.connde.conndeapp.advertise;

import java.net.InetAddress;

/**
 * Created by rosso on 25.09.17.
 */

public class DiscoveredServer {
  private InetAddress serverAddress;
  private InetAddress ownAddress;
  private String macAddress;

  public DiscoveredServer(InetAddress serverAddress, InetAddress ownAddress, String macAddress) {
    this.serverAddress = serverAddress;
    this.ownAddress = ownAddress;
    this.macAddress = macAddress;
  }

  public InetAddress getServerAddress() {
    return serverAddress;
  }

  public InetAddress getOwnAddress() {
    return ownAddress;
  }

  public String getMacAddress() {
    return macAddress;
  }
}
