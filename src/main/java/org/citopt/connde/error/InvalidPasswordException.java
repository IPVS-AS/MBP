package org.citopt.connde.error;

import org.citopt.connde.util.S;

/**
 * @author Jakob Benz
 */
public class InvalidPasswordException extends Exception {
	
	private static final long serialVersionUID = 4851538175180706721L;
	
	// - - -

	public InvalidPasswordException() {
		super();
	}
	
	public InvalidPasswordException(String message) {
		super(message);
	}
	
	// - - -
	
	@Override
	public String getMessage() {
		return S.notEmpty(super.getMessage()) ? super.getMessage() : "Invalid password!";
	}

}
