package de.ipvs.as.mbp.error;

import de.ipvs.as.mbp.util.S;

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