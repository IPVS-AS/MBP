package org.citopt.connde.conndeapp.advertise;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by rosso on 19.08.17.
 */

public class AdvertiseService {
  private static final Logger log = LoggerFactory.getLogger(AdvertiseService.class);

  private Map<String, AdvertiseDevice> devices; // TODO include host in devices?
  private DiscoveredServer discoveredServer;
  private List<ServerDiscoveredListener> discoveredListeners = new LinkedList<>();

  private JSONObject autodeploy_data;
  private AdvertiseDevice host;
  private double min_timeout; // timeout in seconds
  private AtomicBoolean keepalive = new AtomicBoolean(true);

  private File filesDir;

  public AdvertiseService(File filesDir) throws JSONException {
    log.info("Setting up advertising service");

    this.filesDir = filesDir;
    devices = new HashMap<>();

    read_autodeploy();
    read_global_ids();

    log.info("Autodeploy data: " + this.autodeploy_data.toString());

    // calculate smallest timeout
    JSONArray devices = autodeploy_data.getJSONArray(Const.DEPLOY_DEVICES);
    min_timeout = Integer.MAX_VALUE;
    for (int i = 0; i < devices.length(); i++) {
      JSONObject device = devices.getJSONObject(i);
      int timeout = device.getJSONObject(Const.ADAPTER_CONF).getInt(Const.TIMEOUT);
      min_timeout = Math.min(min_timeout, timeout);
    }
    min_timeout = (min_timeout / 2) * 1000;
  }

  private void read_global_ids() throws JSONException {
    File globalIdFile = new File(filesDir, Const.GLOBAL_ID_FILE);
    JSONObject readObject = null;
    if (globalIdFile.exists()) {
      log.info("Found GLOBAL_ID file. Reading for reconnection...");
      try (InputStream is = new FileInputStream(globalIdFile)) {
        int size = is.available();
        byte[] buffer = new byte[size];
        is.read(buffer);
        String json = new String(buffer, "UTF-8");
        readObject = new JSONObject(json);
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else {
      log.info("Could not find GLOBAL_ID file.");
      readObject = new JSONObject();
    }

    Iterator<String> keys = readObject.keys();
    while (keys.hasNext()) {
      String localId = keys.next();
      int globalId = readObject.getInt(localId);

      setGlobalId(localId, globalId);
    }
  }

  private void read_autodeploy() throws JSONException {
    File autodeployFile = new File(filesDir, Const.AUTODEPLOY_FILE);
    JSONObject readObject = null;
    if (autodeployFile.exists()) {
      try (InputStream is = new FileInputStream(autodeployFile)) {
        int size = is.available();
        byte[] buffer = new byte[size];
        is.read(buffer);
        is.close();
        String json = new String(buffer, "UTF-8");
        readObject = new JSONObject(json);
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    } else {
      readObject = new JSONObject();
    }

    autodeploy_data = readObject;


    if (autodeploy_data.has(Const.DEPLOY_SELF)) {
      this.host = DeviceHelper.deviceFromJSON(this.autodeploy_data.getJSONObject(Const.DEPLOY_SELF));
    } else {
      this.host = null;
    }

    if (autodeploy_data.has(Const.DEPLOY_DEVICES)) {
      JSONArray deviceArray = autodeploy_data.getJSONArray(Const.DEPLOY_DEVICES);
      for (int i = 0; i < deviceArray.length(); i++) {
        JSONObject jsonDevice = deviceArray.getJSONObject(i);
        AdvertiseDevice device = DeviceHelper.deviceFromJSON(jsonDevice);
        this.devices.put(device.getLocalId(), device);
      }
    }
  }

  private boolean check_connected() {
    boolean connected = true;

    connected = discoveredServer != null;

    if (host != null && !host.isConnected()) {
      connected = false;
    }

    for (String key : devices.keySet()) {
      AdvertiseDevice device = devices.get(key);
      if (!device.isConnected()) {
        connected = false;
      }
    }

    return connected;
  }

  public void start(String AdvertiserClass) throws JSONException {
    log.info("Starting advertising service with |{}|", AdvertiserClass);

    AdvertiseClient advertiser = new AndroidAdvertiser(Const.LAN, this);

    int tries = 0;
    boolean connected = false;
    while (!connected && tries < 5) {
      tries += 1;
      log.debug("advertising try |{}|", tries);
      DiscoveredServer newDiscoveredServer = advertiser.advertise();
      if (newDiscoveredServer != null) {
        setDiscoveredServer(newDiscoveredServer);
      }
      connected = this.check_connected();
    }

    int connected_devices = 0;
    for (String curKey : this.devices.keySet()) {
      if (this.devices.get(curKey).isConnected()) {
        connected_devices++;
      }
    }

    if (connected_devices == 0) {
      log.info("No devices connected exiting");
      return;
    } else {
      log.info("Successfully connected |{}| devices", connected_devices);
    }

    while (keepalive.get()) {
      long cur_time = System.currentTimeMillis();
      Map<String, AdvertiseDevice> allDevices = this.getAllDevices();
      for (String device_name : allDevices.keySet()) {
        AdvertiseDevice device = allDevices.get(device_name);
        if (!device.isConnected()) {
          log.debug("Device |{}| not connected", device_name);
          continue;
        }
        long last_contact = device.getLastKeepalive();
        double timeout = device.getAutodeployDouble(Const.TIMEOUT);
        long passed_time = cur_time - last_contact;
        double max_passed_time = timeout - this.min_timeout;
        log.debug("Checking device |{}|. " +
                        "\nTimeout is |{}|, min timeout is |{}|, max passed time is |{}|," +
                        "\nlast contact at |{}|, now is |{}|, passed time is |{}|," +
                        "\nneed keep alive |{}|",
                device_name, timeout, this.min_timeout, max_passed_time, last_contact,
                cur_time, passed_time, String.valueOf(passed_time >= max_passed_time));
        if (passed_time >= max_passed_time) {
          advertiser.send_keepalive(device_name);
          device.setLastKeepalive(cur_time);
        }
      }
      try {
        Thread.sleep((long) this.min_timeout);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  public void stop() throws JSONException {
    keepalive.set(false);
    File globalIdFile = new File(filesDir, Const.GLOBAL_ID_FILE);
    try (OutputStream os = new FileOutputStream(globalIdFile)) {
      JSONObject writeObject = new JSONObject();
      Map<String, AdvertiseDevice> allDevices = getAllDevices();
      for (String localId : allDevices.keySet()) {
        int globalId = allDevices.get(localId).getGlobalId();
        if (globalId > 0) {
          writeObject.put(localId, globalId);
        }
      }
      os.write(writeObject.toString(2).getBytes(Charset.forName("UTF-8")));
      os.flush();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  void setGlobalId(String deviceName, int globalId) {
    AdvertiseDevice device = devices.get(deviceName);
    if (device != null) {
      device.setGlobalId(globalId);
    } else if (host != null && host.getLocalId().equals(deviceName)) {
      host.setGlobalId(globalId);
    }
  }

  int getGlobalId(String deviceName) {
    AdvertiseDevice device = devices.get(deviceName);
    if (device != null) {
      return device.getGlobalId();
    } else if (host != null && host.getLocalId().equals(deviceName)) {
      return host.getGlobalId();
    }
    return -1;
  }

  void setConnected(String deviceName, boolean connected) {
    AdvertiseDevice device = devices.get(deviceName);
    if (device != null) {
      device.setConnected(connected);
    } else if (host != null && host.getLocalId().equals(deviceName)) {
      host.setConnected(connected);
    }
  }

  public AdvertiseDevice getHost() {
    return host;
  }

  public Map<String, AdvertiseDevice> getDevices() {
    return devices;
  }

  Map<String, AdvertiseDevice> getAllDevices() {
    if (host != null) {
      Map<String, AdvertiseDevice> allDevices = new HashMap<>(devices);
      allDevices.put(host.getLocalId(), host);
      return allDevices;
    } else {
      return devices;
    }

  }

  private void setDiscoveredServer(DiscoveredServer newDiscoveredServer) {
    discoveredServer = newDiscoveredServer;
    for (ServerDiscoveredListener curListener : discoveredListeners) {
      curListener.onServerDiscovered(new ServerDiscoveredListener.ServerDiscoveredEvent(discoveredServer));
    }
  }

  public DiscoveredServer getDiscoveredServer() {
    return discoveredServer;
  }

  public void addDiscoveredListener(ServerDiscoveredListener listener) {
    discoveredListeners.add(listener);
  }

  public void removeDiscoveredListener(ServerDiscoveredListener listener) {
    discoveredListeners.remove(listener);
  }
}
