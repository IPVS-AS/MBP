/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.citopt.sensmonqtt.database;

import org.bson.types.ObjectId;
import org.citopt.sensmonqtt.device.Device;
import org.citopt.sensmonqtt.device.Sensor;
import org.citopt.sensmonqtt.device.Location;
import org.citopt.sensmonqtt.device.Pin;
import org.citopt.sensmonqtt.device.Script;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

/**
 *
 * @author rafaelkperes
 */
public class DataService {

    private Datastore ds;

    public DataService(Datastore ds) {
        this.ds = ds;
    }

    public void storeDevice(Sensor d) {
        this.ds.save(d);
    }

    public Sensor getSensor(ObjectId id) {
        return this.ds.get(Sensor.class, id);
    }

    public Query<Sensor> querySensor() {
        return this.ds.createQuery(Sensor.class);
    }

    public void storeLocation(Location l) {
        this.ds.save(l);
    }

    public Location getLocation(ObjectId id) {
        return this.ds.get(Location.class, id);
    }

    public Query<Location> queryLocation() {
        return this.ds.createQuery(Location.class);
    }

    public void storePin(Pin p) {
        this.ds.save(p);
    }

    public Pin getPin(ObjectId id) {
        return this.ds.get(Pin.class, id);
    }

    public Query<Pin> queryPin() {
        return this.ds.createQuery(Pin.class);
    }

    public void storeDevice(Device p) {
        this.ds.save(p);
    }

    public Device getDevice(ObjectId id) {
        return this.ds.get(Device.class, id);
    }

    public Query<Device> queryDevice() {
        return this.ds.createQuery(Device.class);
    }

    public void storeScript(Script p) {
        this.ds.save(p);
    }

    public Script getScript(ObjectId id) {
        return this.ds.get(Script.class, id);
    }

    public Query<Script> queryScript() {
        return this.ds.createQuery(Script.class);
    }
}
