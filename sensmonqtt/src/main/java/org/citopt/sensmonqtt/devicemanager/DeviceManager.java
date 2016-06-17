/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.citopt.sensmonqtt.devicemanager;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import org.citopt.sensmonqtt.device.Device;
import org.citopt.sensmonqtt.device.DeviceID;

/**
 * Responsible for registering devices and retrieving its info,
 * including its statuses.
 * @author rafaelkperes
 */
public class DeviceManager {
    
    DeviceManagerDatabaseConnector db;
    Map<DeviceID, Device> activeDevices;

    public DeviceManager() {
        db = new DeviceManagerDatabaseConnector();
        activeDevices = new HashMap<>();
    }
    
    public void registerDevice(Device device) {
        db.addDevice(device);
    }
    
    public void activateDevice(DeviceID id) {
        Device d = db.getDevice(id);
        if(d != null) {
            activeDevices.put(d.getId(), d);
        }        
        throw new NoSuchElementException();
    }
    
    public void deactivateDevice(DeviceID id) {
        activeDevices.remove(id);
    }
    
}
