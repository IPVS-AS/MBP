/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.citopt.sensmonqtt.database;

import com.mongodb.MongoClient;
import java.net.UnknownHostException;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

/**
 *
 * @author rafaelkperes
 */
public class MorphiaUtil {

    private static Datastore ds = null;

    public static Datastore getDatastore() throws UnknownHostException {
        if (ds == null) {
            MongoClient mongoClient = new MongoClient();
            final Morphia morphia = new Morphia();
            final Datastore datastore = morphia.createDatastore(mongoClient, "sensor");
            datastore.ensureIndexes();

            morphia.mapPackage("org.citopt.sensmonqtt.device");
            morphia.mapPackage("org.citopt.sensmonqtt.user");

            ds = datastore;
        }
        return ds;
    }

}
