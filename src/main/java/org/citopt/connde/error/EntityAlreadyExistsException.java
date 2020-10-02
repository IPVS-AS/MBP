package org.citopt.connde.error;

/**
 * @author Jakob Benz
 */
public class EntityAlreadyExistsException extends EntityException {

	private static final long serialVersionUID = 704049898509726408L;
	
	private String entityName;
	
	// - - -
	
	public EntityAlreadyExistsException(String entityType) {
		super(entityType);
	}
	
	public EntityAlreadyExistsException(String entityType, String entityName) {
		super(entityType, null);
		this.entityName = entityName;
	}
	
	// - - -
	
	public String getEntityName() {
		return entityName;
	}
	
	@Override
	public String getMessage() {
		return getEntityDescription() + " already exists.";
	}
	
	@Override
	public String getEntityDescription() {
		StringBuilder sb = new StringBuilder();
		sb.append(entityName);
		if (entityName != null) {
			sb.append(" with name '").append(entityName).append("'");
		}
		return sb.toString();
	}
	
}
