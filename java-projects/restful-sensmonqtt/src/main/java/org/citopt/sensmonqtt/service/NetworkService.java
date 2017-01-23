package org.citopt.sensmonqtt.service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.springframework.stereotype.Component;

/**
 *
 * @author rafaelkperes
 */
@Component
public class NetworkService {

    public String getSelfIp() throws UnknownHostException {
        return InetAddress.getLocalHost().getHostAddress();
    }

}
