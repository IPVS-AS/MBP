package org.citopt.connde.domain.valueLog;

import org.citopt.connde.InfluxDBConfiguration;
import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.concurrent.TimeUnit;


@Measurement(name = InfluxDBConfiguration.MEASUREMENT_NAME,
        database = InfluxDBConfiguration.DATABASE_NAME,
        timeUnit = TimeUnit.SECONDS)
public class ValueLog {
    @Column(name = "time")
    private Instant time;

    @Column(name = "id")
    private String id;

    // mqtt default fields
    @Column(name = "qos")
    private Integer qos;
    @Column(name = "topic")
    private String topic;
    @Column(name = "message")
    private String message;

    // parsed fields
    @Column(name = "idref")
    private String idref;
    @Column(name = "component")
    private String component; // sensor or actuator
    @Column(name = "value")
    private String value;

    public Instant getTime() {
        return time;
    }

    public void setTime(Instant time) {
        this.time = time;
    }

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
}