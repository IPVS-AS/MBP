package org.citopt.connde.service;

import org.springframework.stereotype.Component;

import java.net.*;

/**
 * Network service that provides basic network-related features.
 *
 * @author Jan
 */
@Component
public class NetworkService {
    /**
     * Retrieves the own local IP address of the device this application is currently running on. Works even with
     * multiple available network interfaces.     *
     *
     * @return The IP address of the underlying device
     * @throws UnknownHostException In case of an unexpected network issue
     */
    public String getOwnIPAddress() throws UnknownHostException, SocketException {
        //Try to resolve IP address in an easy way
        String ipAddress = Inet4Address.getLocalHost().getHostAddress();

        //Check if the resolved address may be correct
        if ((!ipAddress.equals("127.0.0.1")) && (!ipAddress.equals("localhost"))) {
            return ipAddress;
        }

        /*
        Resolved IP address is useless, thus try another way
        */

        //Create UDP socket
        DatagramSocket socket = new DatagramSocket();

        //Pretend connection to an arbitrary address (does not need to be reachable)
        socket.connect(InetAddress.getByName("199.199.199.199"), 12321);

        //Resolve IP address
        ipAddress = socket.getLocalAddress().getHostAddress();
        socket.close();

        return ipAddress;
    }
}
