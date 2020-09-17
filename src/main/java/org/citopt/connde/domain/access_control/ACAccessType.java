package org.citopt.connde.domain.access_control;

/**
 * Enumeration for all {@link ACAccess} types. 
 * 
 * @author Jakob Benz
 */
public enum ACAccessType {
	
	/**
	 * Create, e.g., a new sensor. 
	 */
	CREATE,
	
	/**
	 * Read, e.g., sensor data.
	 */
	READ,
	
	/**
	 * Update an existing, e.g., rule.
	 */
	UPDATE,
	
	/**
	 * Delete an existing, e.g., device.
	 */
	DELETE,
	
	/**
	 * Monitor an existing, e.g., device.
	 */
	MONITOR,
	
	/**
	 * Deploy, e.g., an actuator. 
	 */
	DEPLOY,
	
	/**
	 * Undeploy, e.g., a sensor.
	 */
	UNDEPLOY,
	
	/**
	 * Start, e.g., monitoring.
	 */
	START,
	
	/**
	 * Stop, e.g., monitoring.
	 */
	STOP,
	
	/**
	 * Execute, e.g., a actuator action (via a rule action).
	 */
	EXECUTE;
	
}
