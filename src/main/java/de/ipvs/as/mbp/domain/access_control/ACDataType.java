package de.ipvs.as.mbp.domain.access_control;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Enumeration for all data types supported within the access-control framework,
 * e.g., in {@link ACAttribute attributes} or condition {@link IACConditionArgument arguments}.
 * 
 * @author Jakob Benz
 */
public enum ACDataType {
	
	/**
	 * Only numbers. 
	 */
	NUNERIC(Long.class),
	
	/**
	 * Only decimal numbers. 
	 */
	DECIMAL(Double.class),
	
	/**
	 * Only letters.
	 */
	ALPHABETIC(String.class),
	
	/**
	 * Only letters and/or numbers.
	 */
	ALPHANUMERIC(String.class),
	
	/**
	 * Only ASCII symbols.
	 */
	ASCII(String.class),
	
	/**
	 * ISO local date. Pattern: yyyy-MM-dd.
	 */
	DATE(LocalDate.class),
	
	/**
	 * ISO local time. Pattern: HH:mm:ss.
	 */
	TIME(LocalTime.class),
	
	/**
	 * ISO local date-time. Pattern: yyyy-MM-ddTHH:mm:ss.
	 */
	DATETIME(LocalDateTime.class),
	
	/**
	 * Location identified by a latitude and a longitude value. 
	 */
	LOCATION(Location.class);
	
	// - - -
	
	/**
	 * The class to use, e.g., for deserializing and comparing.
	 */
	private final Class<?> clazz;
	
	// - - -
	
	/**
	 * All-args constructor.
	 * 
	 * @param clazz the class to use, e.g., for deserializing and comparing. 
	 */
	private ACDataType(Class<?> clazz) {
		this.clazz = clazz;
	}
	
	// - - -
	
	public Class<?> getClazz() {
		return clazz;
	}
	
}
