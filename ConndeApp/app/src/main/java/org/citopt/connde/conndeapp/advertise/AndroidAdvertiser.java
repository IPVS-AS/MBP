package org.citopt.connde.conndeapp.advertise;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

/**
 * Created by rosso on 18.09.17.
 */

public class AndroidAdvertiser extends AdvertiseClient {
  private static final Logger log = LoggerFactory.getLogger(AndroidAdvertiser.class);

  private DatagramSocket socket;

  public AndroidAdvertiser(String comm_type, AdvertiseService service) {
    super(comm_type, service);
    try {
      socket = new DatagramSocket();
      socket.setBroadcast(true);
      socket.setSoTimeout(Const.CLIENT_TIMEOUT * 1000);
    } catch (SocketException e) {
      e.printStackTrace();
    }
  }

  @Override
  protected void _send_msg(JSONObject msg) {
    if (getServer_address() == null) {
      throw new InternalError("No server address set");
    }
    String stringMsg = msg.toString();
    log.debug("Sending message |{}| to |{}|", stringMsg, InetHelper.getStringFor(getServer_address()));
    byte[] raw_msg = stringMsg.getBytes(Charset.forName("UTF-8"));
    try {
      DatagramPacket packet = new DatagramPacket(raw_msg, raw_msg.length, getServer_address(), Const.PORT);
      socket.send(packet);
    } catch (UnknownHostException e) {
      log.error("Uknown host", e);
    } catch (IOException e) {
      log.error("Comm exception", e);
    }
  }

  @Override
  protected JSONObject _receive_msg() {
    byte[] rawMsg = new byte[1024];
    DatagramPacket receivedPacket = new DatagramPacket(rawMsg, rawMsg.length);
    try {
      socket.receive(receivedPacket);
      String stringMsg = new String(rawMsg, Charset.forName("UTF-8"));
      log.debug("Recieved message |{}| from |{}|", stringMsg, InetHelper.getStringFor(receivedPacket.getAddress()));

      return new JSONObject(stringMsg);
    } catch (SocketTimeoutException e){
      log.error("No response");
    } catch (IOException e) {
      log.error("Comm error ", e);
    } catch (JSONException e) {
      log.error("Error loading json", e);
    }
    return null;
  }

  @Override
  protected DiscoveredServer discover_server() {
    try {
      InetAddress ownIp = null;
      InetAddress broadcastAddress = null;
      String ownMac = null;
      DiscoveredServer discoveredServer = null;
      List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
      for (NetworkInterface curInterface : interfaces) {
        byte[] macAddr = curInterface.getHardwareAddress();
        if (macAddr == null) {
          continue;
        }
        StringBuilder buf = new StringBuilder();
        for (byte aMacAddr : macAddr) {
          buf.append(String.format("%02X:", aMacAddr));
        }
        if (buf.length() > 0) {
          buf.deleteCharAt(buf.length() - 1);
        }
        ownMac = buf.toString();

        for (InterfaceAddress curIntAddr : curInterface.getInterfaceAddresses()) {
          ownIp = curIntAddr.getAddress();
          broadcastAddress = curIntAddr.getBroadcast();
          if(ownIp != null) {
            if (broadcastAddress != null) {
              log.info("Broadcasting to |{}|", InetHelper.getStringFor(broadcastAddress));
              discoveredServer = pingServer(ownIp, ownMac, broadcastAddress);
            } else {
              log.info("Connecting to hardcoded ip |127.0.0.1|");
              discoveredServer = pingServer(ownIp, ownMac, InetAddress.getByName("127.0.0.1"));
            }
          }

          if (discoveredServer != null) {
            return discoveredServer;
          }
        }

      }
    } catch (SocketException e) {
      log.error("Comm error", e);
    } catch (UnknownHostException e) {
      log.error("Unknown host", e);
    }
    return null;
  }

  private DiscoveredServer pingServer(InetAddress ownIp, String ownMac, InetAddress targetAddress) {
    JSONObject data = new JSONObject();
    try {
      data.put(Const.CONN_TYPE, Const.CONN_PING);
      data.put(Const.PING_MSG, "ping");

      String stringMsg = data.toString();
      log.debug("Ping server with |{}|", stringMsg);
      byte[] rawMsg = stringMsg.getBytes(Charset.forName("UTF-8"));
      DatagramPacket msg = new DatagramPacket(rawMsg, rawMsg.length, targetAddress, Const.PORT);
      socket.send(msg);

      try {
        DatagramPacket answer = new DatagramPacket(new byte[1024], 1024);
        socket.receive(answer);
        String stringAnswer = new String(answer.getData(), Charset.forName("UTF-8"));
        log.debug("Received pong |{}|", stringAnswer);
        JSONObject answerMsg = new JSONObject(stringAnswer);
        if (answerMsg.has(Const.PING_MSG) && answerMsg.getString(Const.PING_MSG).equals("pong")) {
          InetAddress serverAddress = answer.getAddress();
          log.info("Server found at |{}|", InetHelper.getStringFor(serverAddress));
          DiscoveredServer discoveredServer = new DiscoveredServer(serverAddress, ownIp, ownMac);
          return discoveredServer;
        }

      } catch (SocketTimeoutException e) {
        log.debug("No response");
      }

    } catch (JSONException e) {
      log.error("Error constructing json", e);
    } catch (IOException e) {
      log.error("Comm error", e);
    }

    return null;
  }


}
