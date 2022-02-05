package de.ipvs.as.mbp.error;

import de.ipvs.as.mbp.domain.access_control.ACAccessType;

/**
 * @author Jakob Benz
 */
public class MissingPermissionException extends EntityException {

	private static final long serialVersionUID = 2264282354782414640L;
	
	private final ACAccessType accessType;
	
	// - - -
	
	public MissingPermissionException(String entityType, ACAccessType accessType) {
		super(entityType);
		this.accessType = accessType;
	}
	
	public MissingPermissionException(String entityType, String entityId, ACAccessType accessType) {
		super(entityType, entityId);
		this.accessType = accessType;
	}
	
	// - - -
	
	public ACAccessType getAccessType() {
		return accessType;
	}
	
	// - - -
	
	@Override
	public String getMessage() {
		return "Missing permission '" + accessType.toString() + "'" + (getEntityDescription() == null ? "." : (" for " + getEntityDescription() + "."));
	}

}
