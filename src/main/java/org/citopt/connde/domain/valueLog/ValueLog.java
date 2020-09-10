package org.citopt.connde.domain.valueLog;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import org.citopt.connde.InfluxDBConfiguration;
import org.citopt.connde.domain.access_control.IACValueLog;
import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Objects of this class represent value logs that were received by the MQTT broker and
 * are recorded by this application.
 */
@ApiModel(description = "Model for value logs of components")
public class ValueLog implements IACValueLog<Double> {

    @ApiModelProperty(notes = "Receive time", example = "{\"nano\":0,\"epochSecond\":1570635657}", accessMode = ApiModelProperty.AccessMode.READ_ONLY, readOnly = true)
    private Instant time;

    // Default MQTT fields
    @Column(name = "qos")
    @ApiModelProperty(notes = "MQTT Quality of Service", example = "0", accessMode = ApiModelProperty.AccessMode.READ_ONLY, readOnly = true)
    private Integer qos;
    
    @Column(name = "topic")
    @ApiModelProperty(notes = "MQTT topic", example = "sensor/5c97dc2583aeb6078c5ab672", accessMode = ApiModelProperty.AccessMode.READ_ONLY, readOnly = true)
    private String topic;
    
    @Column(name = "message")
    @ApiModelProperty(notes = "Full received MQTT message", example = "{ \"component\": \"SENSOR\", \"id\": \"5d9dfeafb1c4d32a86e5b73d\", \"value\": \"434880.000000\"}", accessMode = ApiModelProperty.AccessMode.READ_ONLY, readOnly = true)
    private String message;

    // Fields parsed from the MQTT message
    @Column(name = "idref")
    @ApiModelProperty(notes = "ID of the pertaining component", example = "5c97dc2583aeb6078c5ab672", accessMode = ApiModelProperty.AccessMode.READ_ONLY, readOnly = true)
    private String idref;
    
    @Column(name = "component")
    @ApiModelProperty(notes = "Type of the pertaining component", example = "SENSOR", accessMode = ApiModelProperty.AccessMode.READ_ONLY, readOnly = true)
    private String component; //Component type
    
    @Column(name = "value")
    @ApiModelProperty(notes = "Received value", example = "27.5", accessMode = ApiModelProperty.AccessMode.READ_ONLY, readOnly = true)
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
    public Double getValue() {
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