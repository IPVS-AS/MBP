package org.citopt.sensmonqtt.domain.valueLog;

import javax.persistence.GeneratedValue;
import org.springframework.data.annotation.Id;

/**
 *
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
    private String value;

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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    
}
