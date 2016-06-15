/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.citopt.sensmonqtt.devicemanager;

import java.util.HashMap;
import java.util.Map;
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
    
    
    public enum Status {
        /**
         * Device is active (user option) and reachable
         */
        ACTIVE,
        /**
         * Device is active (user option) but unreachable
         */
        UNREACHABLE,
        /**
         * Device is inactive (user option)
         */
        INACTIVE
    }

    public DeviceManager() {
        db = new DeviceManagerDatabaseConnector();
        activeDevices = new HashMap<>();
    }
    
}
