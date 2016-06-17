/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.citopt.sensmonqtt.device;

import java.util.List;
import java.util.Objects;

/**
 * Class for the id used on Device. Generated from given MAC Address and pin set
 * of one Device.
 *
 * @author rafaelkperes
 */
public class DeviceID {

    private final String id;

    /**
     *
     * @param macAddress Device MAC Address
     * @param pinSet List of pins used to read values on device
     */
    public DeviceID(String macAddress, List<Integer> pinSet) {
        String id_ = macAddress;
        /*for (Integer pin : pinSet) {
            id_ += "." + pin;
            } converted to: */
        id_ = pinSet.stream().map((pin) -> "," + pin).reduce(id_, String::concat);
        this.id = id_.toUpperCase();
    }

    /**
     *
     * @return ID as String
     */
    public String getId() {
        return this.id;
    }

    @Override
    public String toString() {
        return this.getId();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DeviceID other = (DeviceID) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

}