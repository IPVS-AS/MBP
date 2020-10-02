package org.citopt.connde.error;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Jakob Benz
 */
public class EnvironmentModelParseException extends RuntimeException {

	private static final long serialVersionUID = 7790387455913659687L;
	
	private Map<String, String> parseErrors = new HashMap<>();
	
	// - - -
	
	public EnvironmentModelParseException() {
		super();
	}
	
	public EnvironmentModelParseException(String message) {
		super(message);
	}
	
	public EnvironmentModelParseException(Map<String, String> parseErrors) {
		super();
		this.parseErrors = parseErrors;
	}
	
	public EnvironmentModelParseException(String message, Map<String, String> parseErrors) {
		super(message);
		this.parseErrors = parseErrors;
	}
	
	// - - -
	
	public Map<String, String> getParseErrors() {
		return parseErrors;
	}
	
	// - - -
	
	public EnvironmentModelParseException addParseError(String key, String reason) {
		parseErrors.put(key, reason);
		return this;
	}
	
}
