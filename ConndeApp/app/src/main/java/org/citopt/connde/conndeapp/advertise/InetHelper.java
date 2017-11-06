package org.citopt.connde.conndeapp.advertise;

import java.net.InetAddress;

/**
 * Created by rosso on 19.09.17.
 */

public class InetHelper {

  public static String getStringFor(InetAddress address){
    String addressString= address.getHostAddress();
    return addressString;
  }
}
