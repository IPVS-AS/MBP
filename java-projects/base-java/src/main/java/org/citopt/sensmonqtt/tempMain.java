package org.citopt.sensmonqtt;

import com.mongodb.MongoClient;
import java.net.UnknownHostException;
import org.citopt.sensmonqtt.database.DataService;
import org.citopt.sensmonqtt.database.MongoUtils;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author rafaelkperes
 */
public class tempMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws UnknownHostException {
        MongoClient mc = new MongoClient();
        DataService ds = new DataService(MongoUtils.getDatastore(mc, "sensmonqtt"), MongoUtils.getMongoDB(mc));
        ds.cleanUp();
    }
    
}
