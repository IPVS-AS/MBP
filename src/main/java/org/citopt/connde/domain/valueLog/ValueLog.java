package org.citopt.connde.domain.valueLog;

import java.time.Instant;
import java.util.concurrent.TimeUnit;
import org.citopt.connde.InfluxDBConfiguration;
import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;import java.text.SimpleDateFormat;import java.util.Date;

/**
 * Objects of this class represent value logs that were received by the MQTT broker and
 * are recorded by this application. Value logs are stored within an InfluxDB time series database.
 */
@Measurement(name = InfluxDBConfiguration.MEASUREMENT_NAME,
        database = InfluxDBConfiguration.DATABASE_NAME,
        retentionPolicy = InfluxDBConfiguration.RETENTION_POLICY_NAME,
        timeUnit = TimeUnit.SECONDS)
public class ValueLog {
    //Format that is used for the date
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Column(name = "time")
    private Instant time;

    //Default MQTT fields
    @Column(name = "qos")
    private Integer qos;
    @Column(name = "topic")
    private String topic;
    @Column(name = "message")
    private String message;

    //Fields parsed from the MQTT message
    @Column(name = "idref")
    private String idref;
    @Column(name = "component")
    private String component; //Component type
    @Column(name = "value")
    private double value;

    /**
     * Returns the time at which the value log was received.
     *
     * @return The time
     */
    public Instant getTime() {
        return time;
    }

    /**
     * Sets the time at which the value log was received.
     *
     * @param time The time to set
     */
    public void setTime(Instant time) {
        this.time = time;
    }

    /**
     * Returns the quality of service with which the value log was sent.
     *
     * @return The quality of service
     */
    public Integer getQos() {
        return qos;
    }

    /**
     * Sets the quality of service with which the value log was sent.
     *
     * @param qos The quality of service to set
     */
    public void setQos(Integer qos) {
        this.qos = qos;
    }

    /**
     * Returns the MQTT topic under which the value log was received.
     *
     * @return The topic
     */
    public String getTopic() {
        return topic;
    }

    /**
     * Sets the MQTT topic under which the value log was received.
     *
     * @param topic The topic to set
     */
    public void setTopic(String topic) {
        this.topic = topic;
    }

    /**
     * Returns the MQTT message that was originally received.
     *
     * @return The message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the MQTT message that was originally received.
     *
     * @param message The message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    public String getIdref() {
        return idref;
    }

    /**
     * Sets the id of the component to which this value log belongs.
     *
     * @param idref The component id to set
     */
    public void setIdref(String idref) {
        this.idref = idref;
    }

    /**
     * Returns the type of the component to which this value log belongs.
     *
     * @return The component type
     */
    public String getComponent() {
        return component;
    }

    /**
     * Sets the type of the component to which this value log belongs.
     *
     * @param component The component type to set
     */
    public void setComponent(String component) {
        this.component = component;
    }

    /**
     * Returns the value that was received.
     *
     * @return The value
     */
    public double getValue() {
        return value;
    }

    /**
     * Sets the value that was received.
     *
     * @param value The value to set
     */
    public void setValue(double value) {
        this.value = value;
    }
}