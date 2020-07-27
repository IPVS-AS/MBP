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
	 * Start (deploy) something, e.g. a sensor.
	 */
	START,
	
	/**
	 * Stop (undeploy) something, e.g., a sensor.
	 */
	STOP,
	
	/**
	 * Execute, e.g., a actuator action (via a rule action).
	 */
	EXECUTE;
	
}
