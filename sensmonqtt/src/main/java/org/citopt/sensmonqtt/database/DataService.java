/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.citopt.sensmonqtt.database;

import java.net.UnknownHostException;
import org.bson.types.ObjectId;
import org.citopt.sensmonqtt.device.Sensor;
import org.citopt.sensmonqtt.device.Location;
import org.mongodb.morphia.query.Query;

/**
 * 
 * @author rafaelkperes
 */
public class DataService {
    
   public void storeDevice(Sensor d) throws UnknownHostException {
       MorphiaUtil.getDatastore().save(d);
   }
   
   public Sensor getDevice(ObjectId id) throws UnknownHostException {
       return MorphiaUtil.getDatastore().get(Sensor.class, id);
   }
   
   public Query<Sensor> queryDevice() throws UnknownHostException {
       return MorphiaUtil.getDatastore().createQuery(Sensor.class);
   }
   
   public void storeLocation(Location l) throws UnknownHostException {
       MorphiaUtil.getDatastore().save(l);
   }
   
   public Location getLocation(ObjectId id) throws UnknownHostException {
       return MorphiaUtil.getDatastore().get(Location.class, id);
   }

   public Query<Location> queryLocation() throws UnknownHostException {
       return MorphiaUtil.getDatastore().createQuery(Location.class);
   }
   
}
