package org.citopt.connde.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Properties;

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
    
    public String getMQTTBrokerIP () {
    	Properties prop = new Properties();
    	InputStream input = null;
    	String ip = "";
    	
    	try {	
    		input = getClass().getClassLoader().getResourceAsStream("config.properties");
    		prop.load(input);
    		ip = prop.getProperty("mqttbrokerip");

    	} catch (IOException ex) {
    		ex.printStackTrace();
    	} finally {
    		if (input != null) {
    			try {
    				input.close();
    			} catch (IOException e) {
    				e.printStackTrace();
    			}
    		}
    	}
    	return ip;
    }
}
