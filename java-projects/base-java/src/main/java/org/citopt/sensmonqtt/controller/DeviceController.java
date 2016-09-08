/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.citopt.sensmonqtt.controller;

import com.mongodb.MongoClient;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import org.bson.types.ObjectId;
import org.citopt.sensmonqtt.arp.ARP;
import org.citopt.sensmonqtt.database.DataService;
import org.citopt.sensmonqtt.database.MongoUtils;
import org.citopt.sensmonqtt.device.Device;
import org.citopt.sensmonqtt.device.Location;
import org.citopt.sensmonqtt.device.Pin;
import org.citopt.sensmonqtt.device.Script;
import org.citopt.sensmonqtt.device.Sensor;
import org.eclipse.paho.client.mqttv3.MqttException;

/**
 *
 * @author rafaelkperes
 */
public class DeviceController {

    private final DataService ds;
    private ARP arp;

    public DeviceController() throws UnknownHostException, MqttException {
        MongoClient mongoClient = MongoUtils.getMongoClient();
        this.arp = new ARP(MongoUtils.getMongoDB(mongoClient));
        this.ds = new DataService(MongoUtils.getMorphiaDatastore(mongoClient),
            MongoUtils.getMongoDB(mongoClient));
    }

    public List<Pin> getPins() {
        return ds.queryPin().asList();
    }

    public Pin registerPin(String name, String arg) {
        Pin p = new Pin(name, arg);
        ds.storePin(p);
        return p;
    }

    public List<Location> getLocation() {
        return ds.queryLocation().asList();
    }

    public Location registerLocation(String name, String description) {
        Location l = new Location(name, description);
        ds.storeLocation(l);
        return l;
    }

    public List<Script> getScripts() {
        return ds.queryScript().asList();
    }

    public Script registerScript(String name, String description) {
        Script s = new Script(name, description);
        ds.storeScript(s);
        return s;
    }

    public List<Device> getDevices() {
        return ds.queryDevice().asList();
    }

    public Device registerDevice(String macAddress) {
        Device d = new Device(macAddress);
        ds.storeDevice(d);
        return d;
    }

    public Device registerDevice(String macAddress, ObjectId locationid) {
        Location location = ds.getLocation(locationid);
        Device d = new Device(macAddress, location);
        ds.storeDevice(d);
        return d;
    }
    
    public Device setDeviceLocation(ObjectId device, ObjectId location) {
        Location l = ds.getLocation(location);
        Device d = ds.getDevice(device);
        d.setLocation(l);
        ds.storeDevice(d);
        return d;
    }
    
    public Device addDevicePin(ObjectId device, ObjectId pin) {
        Pin p = ds.getPin(pin);
        Device d = ds.getDevice(device);
        d.addPin(p);
        ds.storeDevice(d);
        return d;
    }
    
    public Device setDevicePins(ObjectId device, List<ObjectId> pins) {
        Device d = ds.getDevice(device);
        for (ObjectId pin : pins) {
            Pin p = ds.getPin(pin);
            d.addPin(p);
        }
        ds.storeDevice(d);
        return d;
    }
    
    public List<Sensor> getSensors() {
        return ds.querySensor().asList();
    }
    
    public Sensor registerSensor(ObjectId deviceid) {
        Device device = ds.getDevice(deviceid);
        Sensor s = new Sensor(device);
        ds.storeSensor(s);
        return s;
    }
    
    public Sensor registerSensor(ObjectId deviceid, List<ObjectId> pinsid, ObjectId scriptid) {
        Device device = ds.getDevice(deviceid);
        List<Pin> pins = new LinkedList<>();
        for(ObjectId pinid : pinsid) {
            pins.add(ds.getPin(pinid));
        }
        Script script = ds.getScript(scriptid);
        Sensor s = new Sensor(device, pins, script);
        ds.storeSensor(s);
        return s;
    }
    
    public String getDeviceIp(ObjectId deviceid) {
        Device d = ds.getDevice(deviceid);
        return arp.getIp(d.getMacAddress());
    }
    
    public Sensor.NetworkStatus getDeviceStatus(ObjectId deviceid) {
        Device d = ds.getDevice(deviceid);
        String ip = arp.getIp(d.getMacAddress());
        if(ip == null) {
            return Sensor.NetworkStatus.UNREACHABLE;
        }
        try {
            return arp.getStatus(ip);
        } catch (IOException ex) {
            return Sensor.NetworkStatus.UNREACHABLE;
        }
    }
    
    public void deploySensor(ObjectId sensorid) {
        Sensor sensor = ds.getSensor(sensorid);
        Script script = sensor.getScript();
        if (script == null) {
            // exception no script found
        }
        
        String sensorip = arp.getIp(sensor.getDevice().getMacAddress());
        try {
            if (sensorip != null && Sensor.NetworkStatus.REACHABLE.equals(arp.getStatus(sensorip))) {
                
            } else {
                // exception not reachable
            }
            
            // do ssh deply here
        } catch (IOException ex) {
            // exception not reachable
        }
    }

}
