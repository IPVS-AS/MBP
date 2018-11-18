package org.citopt.connde.domain.valueLog;

import org.citopt.connde.domain.component.Actuator;
import org.citopt.connde.domain.component.Sensor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Reference;

import javax.persistence.GeneratedValue;
import java.text.SimpleDateFormat;

/**
 * @author rafaelkperes
 */
public class ValueLog {

    @Id
    @GeneratedValue
    private String id;

    // mqtt default fields
    private Integer qos;
    private String topic;
    private String message;
    private String date;

    // parsed fields
    private String idref;
    private String component; // sensor or actuator
    private String value;

    @Reference
    private Sensor sensorRef;

    @Reference
    private Actuator actuatorRef;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getQos() {
        return qos;
    }

    public void setQos(Integer qos) {
        this.qos = qos;
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getIdref() {
        return idref;
    }

    public void setIdref(String idref) {
        this.idref = idref;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Sensor getSensorRef() {
        return sensorRef;
    }

    public void setSensorRef(Sensor sensorRef) {
        this.sensorRef = sensorRef;
    }

    public Actuator getActuatorRef() {
        return actuatorRef;
    }

    public void setActuatorRef(Actuator actuatorRef) {
        this.actuatorRef = actuatorRef;
    }

}
