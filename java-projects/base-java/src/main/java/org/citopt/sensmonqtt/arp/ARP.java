/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.citopt.sensmonqtt.arp;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.citopt.sensmonqtt.device.Sensor;

/**
 *
 * @author rafaelkperes
 */
public class ARP {

    private DBCollection coll;
    
    private static final int TIMEOUT = 50;

    public ARP(DB db) {
        this.coll = db.getCollection("arping");
    }

    public String getIp(String macAddress) {
        DBObject result = this.coll.findOne(new BasicDBObject("mac", macAddress));
        if (result != null) {
            return (String) result.get("ip");
        } else {
            return null;
        }
    }   
    
    public Sensor.NetworkStatus getStatus(String ip) throws UnknownHostException, IOException {
        InetAddress addr = InetAddress.getByName(ip);
        if (addr.isReachable(TIMEOUT)) {
            return Sensor.NetworkStatus.REACHABLE;
        }
        return Sensor.NetworkStatus.UNREACHABLE;
    }

}
