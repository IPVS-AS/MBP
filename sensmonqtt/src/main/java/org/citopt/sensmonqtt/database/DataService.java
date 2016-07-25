/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.citopt.sensmonqtt.database;

import java.net.UnknownHostException;
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

    public void storeDevice(Sensor d) throws UnknownHostException {
        this.ds.save(d);
    }

    public Sensor getSensor(ObjectId id) throws UnknownHostException {
        return this.ds.get(Sensor.class, id);
    }

    public Query<Sensor> querySensor() throws UnknownHostException {
        return this.ds.createQuery(Sensor.class);
    }

    public void storeLocation(Location l) throws UnknownHostException {
        this.ds.save(l);
    }

    public Location getLocation(ObjectId id) throws UnknownHostException {
        return this.ds.get(Location.class, id);
    }

    public Query<Location> queryLocation() throws UnknownHostException {
        return this.ds.createQuery(Location.class);
    }

    public void storePin(Pin p) throws UnknownHostException {
        this.ds.save(p);
    }

    public Pin getPin(ObjectId id) throws UnknownHostException {
        return this.ds.get(Pin.class, id);
    }

    public Query<Pin> queryPin() throws UnknownHostException {
        return this.ds.createQuery(Pin.class);
    }

    public void storeDevice(Device p) throws UnknownHostException {
        this.ds.save(p);
    }

    public Device getDevice(ObjectId id) throws UnknownHostException {
        return this.ds.get(Device.class, id);
    }

    public Query<Device> queryDevice() throws UnknownHostException {
        return this.ds.createQuery(Device.class);
    }

    public void storeScript(Script p) throws UnknownHostException {
        this.ds.save(p);
    }

    public Script getScript(ObjectId id) throws UnknownHostException {
        return this.ds.get(Script.class, id);
    }

    public Query<Script> queryScript() throws UnknownHostException {
        return this.ds.createQuery(Script.class);
    }
}
