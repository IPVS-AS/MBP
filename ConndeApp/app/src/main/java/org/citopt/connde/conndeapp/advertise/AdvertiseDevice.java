package org.citopt.connde.conndeapp.advertise;

import java.util.Map;

/**
 * Created by rosso on 27.08.17.
 */

public class AdvertiseDevice {
  private String localId;
  private int globalId;
  private String type;
  private Map<String, Object> adapterConf;
  private long lastKeepalive;
  private boolean connected;
  private String conndeId;

  private AdvertiseDevice host;

  public AdvertiseDevice(String localId, Map<String, Object> adapterConf) {
    this(localId, 0, adapterConf, -1, false);
  }

  public AdvertiseDevice(String localId, int globalId, Map<String, Object> adapterConf, long lastKeepalive, boolean connected) {
    this.localId = localId;
    this.globalId = globalId;
    this.adapterConf = adapterConf;
    this.lastKeepalive = lastKeepalive;
    this.connected = connected;
  }

  public String getLocalId() {
    return localId;
  }

  public void setLocalId(String localId) {
    this.localId = localId;
  }

  public int getGlobalId() {
    return globalId;
  }

  public void setGlobalId(int globalId) {
    this.globalId = globalId;
  }

  public Map<String, Object> getAdapterConf() {
    return adapterConf;
  }

  public void setAdapterConf(Map<String, Object> adapterConf) {
    this.adapterConf = adapterConf;
  }

  public Object getAutodeployParam(String key){
    return adapterConf.get(key);
  }

  public String getAutodeployString(String key){
    return (String) adapterConf.get(key);
  }

  public double getAutodeployDouble(String key){
    Object val= adapterConf.get(key);
    if(val instanceof Integer){
      return (Integer) val;
    }
    return (double)adapterConf.get(key);
  }

  public long getLastKeepalive() {
    return lastKeepalive;
  }

  public void setLastKeepalive(long lastKeepalive) {
    this.lastKeepalive = lastKeepalive;
  }

  public boolean isConnected() {
    return connected;
  }

  public void setConnected(boolean connected) {
    this.connected = connected;
  }

  public AdvertiseDevice getHost() {
    return host;
  }

  public void setHost(AdvertiseDevice host) {
    this.host = host;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getConndeId() {
    return conndeId;
  }

  public void setConndeId(String conndeId) {
    this.conndeId = conndeId;
  }

  @Override
  public String toString() {
    return localId + "|" + type + "|" + globalId + "|" + (host != null? host.globalId : "") + "|" + (connected? "connected" : "unconnected");
  }
}
