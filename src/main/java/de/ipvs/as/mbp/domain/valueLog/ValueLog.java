package de.ipvs.as.mbp.domain.valueLog;

import de.ipvs.as.mbp.domain.access_control.IACValueLog;
import de.ipvs.as.mbp.service.receiver.ValueLogReceiver;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.Instant;

/**
 * Objects of this class represent value logs that were received by the {@link ValueLogReceiver} from devices via
 * publish-subscribe-based messaging.
 */
@ApiModel(description = "Model for received value logs")
public class ValueLog implements IACValueLog<Double> {

    @ApiModelProperty(notes = "Receiving time", example = "{\"nano\":0,\"epochSecond\":1570635657}", accessMode = ApiModelProperty.AccessMode.READ_ONLY, readOnly = true)
    private Instant time;

    @ApiModelProperty(notes = "Topic", example = "sensor/5c97dc2583aeb6078c5ab672", accessMode = ApiModelProperty.AccessMode.READ_ONLY, readOnly = true)
    private String topic;

    @ApiModelProperty(notes = "Full received message", example = "{ \"component\": \"SENSOR\", \"id\": \"5d9dfeafb1c4d32a86e5b73d\", \"value\": \"434880.000000\"}", accessMode = ApiModelProperty.AccessMode.READ_ONLY, readOnly = true)
    private String message;

    @ApiModelProperty(notes = "ID of the pertaining component", example = "5c97dc2583aeb6078c5ab672", accessMode = ApiModelProperty.AccessMode.READ_ONLY, readOnly = true)
    private String idref;

    @ApiModelProperty(notes = "Type of the pertaining component", example = "SENSOR", accessMode = ApiModelProperty.AccessMode.READ_ONLY, readOnly = true)
    private String component; //Component type

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
    public ValueLog setTime(Instant time) {
        this.time = time;
        return this;
    }

    /**
     * Returns the topic under which the value log message was published.
     *
     * @return The topic
     */
    public String getTopic() {
        return topic;
    }

    /**
     * Sets the topic under which the value log message was received.
     *
     * @param topic The topic to set
     */
    public ValueLog setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    /**
     * Returns the message that was originally received.
     *
     * @return The message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the message that was originally received.
     *
     * @param message The message to set
     */
    public ValueLog setMessage(String message) {
        this.message = message;
        return this;
    }

    public String getIdref() {
        return idref;
    }

    /**
     * Sets the id of the component to which this value log belongs.
     *
     * @param idref The component id to set
     */
    public ValueLog setIdref(String idref) {
        this.idref = idref;
        return this;
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
    public ValueLog setComponent(String component) {
        this.component = component;
        return this;
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
    public ValueLog setValue(double value) {
        this.value = value;
        return this;
    }
}