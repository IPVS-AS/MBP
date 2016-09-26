package org.citopt.websensor.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.bson.types.ObjectId;

public final class MQTTLoggerResult {
    private String id;
    private String topic;
    private String message;
    private String value;
    private String sensorId;
    private String sensorName;
    private Date date;
    private String rawDate;

    public MQTTLoggerResult(String id, String topic, String message, 
            String value, String sensorId, String date) {
        this.id = id;
        this.topic = topic;
        this.message = message;
        this.value = value;
        this.sensorId = sensorId;
        this.setDate(date);
    }

    public String getId() {
        return id;
    }
    
    public void setId(ObjectId id) {
        this.id = id.toString();
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getSensorId() {
        return sensorId;
    }

    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }

    public String getSensorName() {
        return sensorName;
    }

    public void setSensorName(String sensorName) {
        this.sensorName = sensorName;
    }
    
    public String getDate() {
        if (this.date != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return sdf.format(date);
        } else {
            return this.rawDate;
        }
    }

    public Date getParsedDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
    
    public void setDate(String date) {
        this.rawDate = date;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
            this.date = sdf.parse(date);
        } catch (ParseException e) {
        }
    }
    
}
