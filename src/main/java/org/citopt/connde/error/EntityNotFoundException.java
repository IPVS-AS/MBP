package org.citopt.connde.error;

/**
 * @author Jakob Benz
 */
public class EntityNotFoundException extends EntityException {

	private static final long serialVersionUID = 7350340642621723996L;
	
	// - - -
	
	public EntityNotFoundException(String entityType) {
		super(entityType);
	}
	
	public EntityNotFoundException(String entityType, String entityId) {
		super(entityType, entityId);
	}
	
	// - - -
	
	@Override
	public String getMessage() {
		return getEntityDescription() + " not found.";
	}
	
}
