package de.ipvs.as.mbp.domain.access_control;

/**
 * Enumeration for all MBP access-control framework entity types.
 * 
 * @author Jakob Benz
 */
public enum ACEntityType {
	
	/**
	 * A requesting entity from outside the MBP (backend), e.g.,
	 * a user via the MBP frontend or another application via the MBP REST API.
	 */
	REQUESTING_ENTITY,
	
	/**
	 * A requested entity, e.g., a sensor or an actuator (action).
	 */
	REQUESTED_ENTITY;

}
