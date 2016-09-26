package org.citopt.websensor.service;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import java.util.ArrayList;
import java.util.List;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.limit;
import org.springframework.stereotype.Component;

@Component
public class MQTTLoggerReader {

    private static Mongo mongo;
    private static DB db;
    private static DBCollection coll;

    @Autowired
    public void setMongo(Mongo mongo) {
        System.out.println("autowiring Mongo to MQTTLoggerReader");
        MQTTLoggerReader.mongo = mongo;
        MQTTLoggerReader.db = MQTTLoggerReader.mongo.getDB("sensmonqtt");
        MQTTLoggerReader.coll = MQTTLoggerReader.db.getCollection("mqttlog");
    }

    public List<MQTTLoggerResult> loadLog(int qty) {
        List<MQTTLoggerResult> log = new ArrayList<>();

        DBCursor cursor = coll.find().sort(new BasicDBObject("$natural", -1)).limit(qty);
        while (cursor.hasNext()) {
            DBObject next = cursor.next();
            log.add(map(next));
        }
        return log;
    }
    
    public MQTTLoggerResult loadEntry(ObjectId id) {
        return map(coll.findOne(id));
    }

    private MQTTLoggerResult map(DBObject obj) {
        MQTTLoggerResult result;

        String id = ((ObjectId) obj.get("_id")).toString();
        String topic = (String) obj.get("topic");
        String message = (String) obj.get("message");
        String date = (String) obj.get("date");
        String sensorId = (String) obj.get("id");
        String value = (String) obj.get("value");

        result = new MQTTLoggerResult(id, topic, message, value, sensorId, date);
        return result;
    }

}
