package de.ipvs.as.mbp.error;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import de.ipvs.as.mbp.util.LocalDateTimeSerializer;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Wrapper for generic error handling.
 * 
 * @author Jakob Benz
 */
public class ApiError {
	
//	public static void main(String[] args) throws JsonProcessingException {
//		ApiError e = new ApiError(HttpStatus.OK, LocalDateTime.now(), "This is the global error message.");
//		e.addDetailMessage("detail-message-1", "This is details message 1.");
//		e.addDetailMessage("detail-message-2", "This is details message 2.");
//		System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(e));
//	}
	
	private HttpStatus status;
	private int statusCode;
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	private LocalDateTime timestamp;
	private String message;
	private Map<String, String> detailMessages = new HashMap<>();
	
	// - - -

	public ApiError(HttpStatus status, LocalDateTime timestamp, String message) {
		super();
		this.status = status;
		this.statusCode = status.value();
		this.timestamp = timestamp;
		this.message = message;
	}
	
	public ApiError(HttpStatus status, LocalDateTime timestamp, String message, Map<String, String> detailMessages) {
		super();
		this.status = status;
		this.statusCode = status.value();
		this.timestamp = timestamp;
		this.message = message;
		this.detailMessages = detailMessages;
	}

	// - - -
	
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

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public ApiError setTimestamp(LocalDateTime timestamp) {
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
	
	// - - -
	
	public ApiError addDetailMessage(String key, String message) {
		detailMessages.put(key, message);
		return this;
	}
	
}
