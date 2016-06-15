/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.citopt.sensmonqtt.device;

import java.util.List;

/**
 *
 * @author rafaelkperes
 */
public class Device {
        
    private final DeviceID devId;
    private final String macAddress;
    private final List<Integer> pinSet;
    private final String type;

    /**
     *
     * @param macAddress Device MAC Address
     * @param pinSet List of pins used to read values on device
     * @param type Type of the device will determine which script will be deployed
     */
    public Device(String macAddress, List<Integer> pinSet, String type) {
        this.devId = new DeviceID(macAddress, pinSet);
        this.macAddress = macAddress;
        this.pinSet = pinSet;
        this.type = type;
    }

    /**
     *
     * @return DeviceID that contains this Device id as a String
     */
    public DeviceID getId() {
        return devId;
    }

    /**
     *
     * @return Type of this Device as a String
     */
    public String getType() {
        return type;
    }

    /**
     *
     * @return MAC Address as String of this Device
     */
    public String getMacAddress() {
        return macAddress;
    }

    /**
     *
     * @return List of pin used by this Device
     */
    public List<Integer> getPinSet() {
        return pinSet;
    }
    
}
