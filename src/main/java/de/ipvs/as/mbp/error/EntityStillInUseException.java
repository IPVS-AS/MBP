package de.ipvs.as.mbp.error;

/**
 * @author Jakob Benz
 */
public class EntityStillInUseException extends EntityException {
	
	private static final long serialVersionUID = -5338601671182319915L;
	
	// - - -
	
	public EntityStillInUseException(String entityType) {
		super(entityType);
	}
	
	public EntityStillInUseException(String entityType, String entityId) {
		super(entityType, entityId);
	}
	
	// - - -
	
	@Override
	public String getMessage() {
		return getEntityDescription() + " is still in use.";
	}
	
}
