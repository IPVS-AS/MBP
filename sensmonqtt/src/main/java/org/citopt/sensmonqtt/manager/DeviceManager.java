/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.citopt.sensmonqtt.manager;

import java.util.NoSuchElementException;
import org.citopt.sensmonqtt.database.DatabaseConnector;
import org.citopt.sensmonqtt.device.Device;
import org.citopt.sensmonqtt.device.DeviceID;

/**
 * Responsible for registering devices and retrieving its info, including its
 * statuses.
 *
 * @author rafaelkperes
 */
public class DeviceManager {

    DatabaseConnector db;

    public DeviceManager() {
        db = DatabaseConnector.getInstance();
    }

    public void registerDevice(Device device) {
        db.addDevice(device);
    }

    public void activateDevice(DeviceID id) {
        Device d = db.getDevice(id);
        if (d != null) {
            d.setStatus(Device.Status.ACTIVE);
            db.updateDevice(d);
        } else {
            throw new NoSuchElementException();
        }
    }

    public void deactivateDevice(DeviceID id) {
        Device d = db.getDevice(id);
        if (d != null) {
            d.setStatus(Device.Status.INACTIVE);
            db.updateDevice(d);
        } else {
            throw new NoSuchElementException();
        }
    }

}
