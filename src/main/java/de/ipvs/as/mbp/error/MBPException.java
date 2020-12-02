package de.ipvs.as.mbp.error;

import org.springframework.http.HttpStatus;

/**
 * Generic class intended for all errors that are not covered
 * by a specific exception class. This way, a mapping to {@link ApiError}
 * is enabled.
 * 
 * @author Jakob Benz
 *
 */
public class MBPException extends RuntimeException {

	private static final long serialVersionUID = -6655283848175258933L;
	
	private HttpStatus status;
	
	// - - -
	
	public MBPException(HttpStatus status) {
		super();
		this.status = status;
	}
	
	public MBPException(HttpStatus status, String message) {
		super(message);
		this.status = status;
	}
	
	// - - -
	
	public HttpStatus getStatus() {
		return status;
	}

}
