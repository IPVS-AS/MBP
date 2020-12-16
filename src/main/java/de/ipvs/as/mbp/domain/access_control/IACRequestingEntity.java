package de.ipvs.as.mbp.domain.access_control;

/**
 * A requesting entity within the MBP access-control framework,
 * e.g., a user or a another application.
 * 
 * @author Jakob Benz
 */
public interface IACRequestingEntity extends IACEntity {
	
	/* (non-Javadoc)
	 * @see org.citopt.connde.domain.access_control.IACEntity#getType()
	 */
	@Override
	public default ACEntityType getEntityType() {
		return ACEntityType.REQUESTING_ENTITY;
	}
	
}
