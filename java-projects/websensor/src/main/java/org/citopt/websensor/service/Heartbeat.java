package org.citopt.websensor.service;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Heartbeat {

    private static Mongo mongo;
    private static DB db;
    private static DBCollection mac;
    private static DBCollection heartbeat;

    @Autowired
    public void setMongo(Mongo mongo) {
        System.out.println("autowiring Mongo to Heartbeat");
        Heartbeat.mongo = mongo;
        Heartbeat.db = Heartbeat.mongo.getDB("sensmonqtt");
        Heartbeat.mac = Heartbeat.db.getCollection("mac");
        Heartbeat.heartbeat = Heartbeat.db.getCollection("heartbeat");
    }

    public boolean isRegistered(String id) {
        boolean ret = Heartbeat.mac.findOne(
                new BasicDBObject().append("_id", id)) != null;
        System.out.println(ret);
        return ret;
    }

    public void registerMac(String mac, String id) {
        DBObject obj = new BasicDBObject();
        obj.put("_id", id);
        obj.put("mac", mac);
        Heartbeat.mac.insert(obj);
    }
    
    public void removeMac(String id) {
        DBObject obj = new BasicDBObject();
        obj.put("_id", id);
        Heartbeat.mac.remove(obj);
    }

    public HeartbeatResult getResult(String id) throws ParseException {
        DBObject obj = heartbeat.findOne(
                new BasicDBObject().append("_id", id));
        if (obj != null) {
            HeartbeatResult result = new HeartbeatResult();
            result.setIp((String) obj.get("ip"));
            result.setMac((String) obj.get("mac"));
            result.setStatus((String) obj.get("status"));
            result.setDate((String) obj.get("date"));
            
            return result;
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
            return new HeartbeatResult(null, null, 
                    HeartbeatResult.Status.UNDEFINED, sdf.format(new Date()));
        }
    }
}
