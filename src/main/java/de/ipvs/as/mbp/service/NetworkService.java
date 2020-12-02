package de.ipvs.as.mbp.service;

import de.ipvs.as.mbp.util.Validation;
import org.springframework.stereotype.Component;

import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Network service that provides basic network-related features.
 *
 * @author Jan
 */
@Component
public class NetworkService {

    private static final Pattern REGEX_IP_FROM_HOSTNAME = Pattern.compile("\\d{1,3}");

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
        if (isIPAddressUseful(ipAddress)) {
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

        //Check if the resolved address may be correct
        if (isIPAddressUseful(ipAddress)) {
            return ipAddress;
        }

        /*
        Resolved IP address is still useless
        Last option: resolve the IP address from host name
        */

        //Resolve host name
        String hostName = InetAddress.getLocalHost().getHostName();
        Matcher m = REGEX_IP_FROM_HOSTNAME.matcher(hostName);

        List<String> numberBlocks = new ArrayList<>();

        //Find all number blocks
        while (m.find()) {
            String numberBlock = m.group();
            numberBlocks.add(numberBlock);
        }

        //At least four blocks are required
        if (numberBlocks.size() < 4) {
            return null;
        }

        //String builder for putting together the ip address
        StringBuilder ipAddressBuilder = new StringBuilder();

        //Take the last four number blocks
        for (int i = (numberBlocks.size() - 4); i < numberBlocks.size(); i++) {
            //Add number block separator
            if (ipAddressBuilder.length() > 0) {
                ipAddressBuilder.append(".");
            }

            //Append number block
            ipAddressBuilder.append(numberBlocks.get(i));
        }

        return ipAddressBuilder.toString();
    }

    /**
     * Checks whether a given IP address is valid and makes sense so it can be used for deployment.
     * Considered as useless IP addresses are "127.0.0.1", "localhost" and IP addresses starting
     * with "10." (internal OpenStack IP addresses).
     *
     * @param ipAddress The IP address to check
     * @return True, if the IP address is useful; false otherwise
     */
    private boolean isIPAddressUseful(String ipAddress) {
        //Check format
        if (!Validation.isValidIPAddress(ipAddress)) {
            return false;
        }

        //Validation check
        return (!ipAddress.equals("127.0.0.1")) && (!ipAddress.equals("localhost")) && (!ipAddress.startsWith("10.0."));
    }
}