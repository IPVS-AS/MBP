package org.citopt.websensor.service;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ARPReader {
    
    private static Mongo mongo;
    private static DB db;
    private static DBCollection coll;
    
    @Autowired
    public void setMongo(Mongo mongo) {
        System.out.println("autowiring Mongo to ARPReader");
        ARPReader.mongo = mongo;
        ARPReader.db = ARPReader.mongo.getDB("sensmonqtt");
        ARPReader.coll = ARPReader.db.getCollection("arping");
    }
    
    public String getIp(String macAddress) {
        BasicDBObject query = new BasicDBObject("mac", macAddress);
        DBObject result = ARPReader.coll.findOne(query);
        
        if (result != null) {
            return (String) result.get("ip");
        } else {
            return null;
        }
    }
    
    public List<ARPResult> getTable() {
        List<ARPResult> result = new ArrayList<>();
        try (DBCursor cursor = coll.find()) {
            while (cursor.hasNext()) {
                DBObject element = cursor.next();
                String ip = (String) element.get("ip");
                String mac = (String) element.get("mac");
                result.add(new ARPResult(ip, mac));
            }
        }
        return result;
    } 
    
    public void resetTable() {
        ARPReader.coll.remove(new BasicDBObject());
    }
}
