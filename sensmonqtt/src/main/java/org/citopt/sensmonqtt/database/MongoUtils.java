/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.citopt.sensmonqtt.database;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import java.net.UnknownHostException;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

/**
 *
 * @author rafaelkperes
 */
public class MongoUtils {

    private static MongoClient mongo = null;
    private static Datastore ds = null;
    private static DB db = null;

    public static MongoClient getMongoClient() throws UnknownHostException {
        if (mongo == null) {
            mongo = new MongoClient();
        }
        return mongo;
    }
    
    public static DB getMongoDB(MongoClient mongoClient) {
        if (db == null) {
            db = mongoClient.getDB("sensmonqtt");
        }
        return db;
    }

    public static Datastore getMorphiaDatastore(MongoClient mongoClient) {
        if (ds == null) {
            final Morphia morphia = new Morphia();
            final Datastore datastore = morphia.createDatastore(mongoClient, "sensmonqtt");

            morphia.mapPackage("org.citopt.sensmonqtt.device");
            morphia.mapPackage("org.citopt.sensmonqtt.user");

            datastore.ensureIndexes();

            ds = datastore;
        }
        return ds;
    }

    public static Datastore getDatastore(MongoClient mongoClient, String name) {
        final Morphia morphia = new Morphia();
        final Datastore datastore = morphia.createDatastore(mongoClient, name);

        morphia.mapPackage("org.citopt.sensmonqtt.device");
        morphia.mapPackage("org.citopt.sensmonqtt.user");

        datastore.ensureIndexes();

        return datastore;
    }

}
