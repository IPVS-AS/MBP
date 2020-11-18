package de.ipvs.as.mbp.error;

import de.ipvs.as.mbp.util.S;

/**
 * @author Jakob Benz
 */
public class MissingOwnerPrivilegesException extends Exception {
	
	private static final long serialVersionUID = 1912168943975360169L;
	
	// - - -

	public MissingOwnerPrivilegesException() {
		super();
	}
	
	public MissingOwnerPrivilegesException(String message) {
		super(message);
	}
	
	// - - -
	
	@Override
	public String getMessage() {
		return S.notEmpty(super.getMessage()) ? super.getMessage() : "Missing owner privileges!";
	}

}
