package org.citopt.connde.service;

import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Network service that provides basic network-related features.
 *
 * @author rafaelkperes, Jan
 */
@Component
public class NetworkService {
    /**
     * Retrieves the own IP address of the device this application is currently running on.
     *
     * @return The IP address of the underlying device
     * @throws UnknownHostException In case of an unexpected network issue
     */
    public String getOwnIPAddress() throws UnknownHostException {
        return InetAddress.getLocalHost().getHostAddress();
    }
}
