/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.citopt.sensmonqtt.controller;

import com.mongodb.MongoClient;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
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

    private DataService ds;
    private ARP arp;

    public DeviceController() throws UnknownHostException, MqttException {
        //arp = ARP.getInstance();
        //arp.connectMqtt();
        
        MongoClient mongoClient = new MongoClient();
        this.ds = new DataService(MongoUtils.getMorphiaDatastore(mongoClient));
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
    
    public void deploySensor(ObjectId sensorid) {
        Sensor sensor = ds.getSensor(sensorid);
        
        if (sensor.getScript() == null) {
            // some exception here, since no script was set yet
        }
        
        try {
            String sensorip = arp.getIp(sensor.getDevice().getMacAddress());
        } catch(NoSuchElementException e) {
            // not found
        }
        
        // do ssh deply here
    }

}
