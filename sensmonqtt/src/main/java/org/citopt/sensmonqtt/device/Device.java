/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.citopt.sensmonqtt.device;

import java.util.ArrayList;
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
    private Status status;
    private NetworkStatus netStatus;

    public enum Status {
        /**
         * Device is active (user option) and reachable
         */
        ACTIVE,
        /**
         * Device is inactive (user option)
         */
        INACTIVE
    }
    
    public enum NetworkStatus {
        /**
         * Device reachable (only when is active)
         */
        REACHABLE,
        /**
         * Device unreachable (can be active or inactive)
         */
        UNREACHABLE,
    }
    
    /**
     * Status set as INACTIVE.
     * Network Status will always start as UNREACHABLE.
     * @param macAddress Device MAC Address
     * @param pinSet List of pins used to read values on device
     * @param type Type of the device will determine which script will be deployed
     */
    public Device(String macAddress, List<Integer> pinSet, String type) {
        this.devId = new DeviceID(macAddress, pinSet);
        this.macAddress = macAddress;
        this.pinSet = new ArrayList<>(pinSet);
        this.type = type;
        this.status = Status.INACTIVE;
        this.netStatus = NetworkStatus.UNREACHABLE;
    }
    
    /**
     * Network Status will always start as UNREACHABLE.
     * @param macAddress Device MAC Address
     * @param pinSet List of pins used to read values on device
     * @param type Type of the device will determine which script will be deployed
     * @param status Initial status of the Device
     */
    public Device(String macAddress, List<Integer> pinSet, String type, Status status) {
        this(macAddress, pinSet, type);
        this.status = status;
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

    public Status getStatus() {
        return status;
    }

    public NetworkStatus getNetStatus() {
        return netStatus;
    }

    /**
     * If status is set do INACTIVE, netStatus is automatically set to
     * UNREACHABLE.
     * @param status if INACTIVE, netStatus set to UNREACHABLE
     */
    public void setStatus(Status status) {        
        this.status = status;
        if(Status.INACTIVE.equals(status)) {
            this.netStatus = NetworkStatus.UNREACHABLE;
        }
    }

    /**
     * If device status is INACTIVE and argument netStatus is REACHABLE,
     * IllegalArgumentException is thrown.
     * @param netStatus 
     * @throws IllegalArgumentException when netStatus is REACHABLE but device status is INACTIVE
     */
    public void setNetStatus(NetworkStatus netStatus) 
            throws IllegalArgumentException {
        if (Status.INACTIVE.equals(this.status)
                && NetworkStatus.REACHABLE.equals(netStatus)) {
            throw new IllegalArgumentException("Cannot set netStatus to REACHABLE"
                    + " while device is INACTIVE");
        }
        this.netStatus = netStatus;
    }
}
