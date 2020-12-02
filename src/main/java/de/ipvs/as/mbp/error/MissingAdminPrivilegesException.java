package de.ipvs.as.mbp.error;

import de.ipvs.as.mbp.util.S;

/**
 * @author Jakob Benz
 */
public class MissingAdminPrivilegesException extends Exception {

	private static final long serialVersionUID = -1820146786963471610L;
	
	// - - -
	
	public MissingAdminPrivilegesException() {
		super();
	}
	
	public MissingAdminPrivilegesException(String message) {
		super(message);
	}
	
	// - - -
	
	@Override
	public String getMessage() {
		return S.notEmpty(super.getMessage()) ? super.getMessage() : "Missing admin privileges!";
	}

}
