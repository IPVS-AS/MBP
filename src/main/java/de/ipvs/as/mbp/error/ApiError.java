package de.ipvs.as.mbp.error;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.ipvs.as.mbp.util.InstantToStringSerializer;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Wrapper for generic error handling.
 *
 * @author Jakob Benz
 */
public class ApiError {
    private HttpStatus status;
    private int statusCode;
    @JsonSerialize(using = InstantToStringSerializer.class)
    private Instant timestamp;
    private String message;
    private Map<String, String> detailMessages = new HashMap<>();

    public ApiError(HttpStatus status, Instant timestamp, String message) {
        super();
        this.status = status;
        this.statusCode = status.value();
        this.timestamp = timestamp;
        this.message = message;
    }

    public ApiError(HttpStatus status, Instant timestamp, String message, Map<String, String> detailMessages) {
        super();
        this.status = status;
        this.statusCode = status.value();
        this.timestamp = timestamp;
        this.message = message;
        this.detailMessages = detailMessages;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public ApiError setStatus(HttpStatus status) {
        this.status = status;
        this.statusCode = status.value();
        return this;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public ApiError setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public ApiError setMessage(String message) {
        this.message = message;
        return this;
    }

    public Map<String, String> getDetailMessages() {
        return detailMessages;
    }

    public ApiError setDetailMessages(Map<String, String> detailMessages) {
        this.detailMessages = detailMessages;
        return this;
    }

    public ApiError addDetailMessage(String key, String message) {
        detailMessages.put(key, message);
        return this;
    }

}
