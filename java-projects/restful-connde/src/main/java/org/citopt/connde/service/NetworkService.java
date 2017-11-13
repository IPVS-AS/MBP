package org.citopt.connde.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;

import org.springframework.stereotype.Component;

/**
 *
 * @author rafaelkperes
 */
@Component
public class NetworkService {

    public String getSelfIp () throws UnknownHostException {
        return InetAddress.getLocalHost().getHostAddress();
    }

    public String getPublicIP () throws UnknownHostException {
    	URL publicIP;
		try {
			publicIP = new URL("http://checkip.amazonaws.com");
	    	BufferedReader in = new BufferedReader(new InputStreamReader(publicIP.openStream()));
	    	return in.readLine(); 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return getSelfIp();
    }
}
