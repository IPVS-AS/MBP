package org.citopt.connde.domain.access_control;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

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
	NUNERIC(Long.class, ACArgumentFunction.ALL),
	
	/**
	 * Only decimal numbers. 
	 */
	DECIMAL(Double.class, ACArgumentFunction.ALL),
	
	/**
	 * Only letters.
	 */
	ALPHABETIC(String.class, Arrays.asList(ACArgumentFunction.EQUALS, ACArgumentFunction.NOT_EQUALS)),
	
	/**
	 * Only letters and/or numbers.
	 */
	ALPHANUMERIC(String.class, Arrays.asList(ACArgumentFunction.EQUALS, ACArgumentFunction.NOT_EQUALS)),
	
	/**
	 * Only ASCII symbols.
	 */
	ASCII(String.class, Arrays.asList(ACArgumentFunction.EQUALS, ACArgumentFunction.NOT_EQUALS)),
	
	/**
	 * ISO local date. Pattern: yyyy-MM-dd.
	 */
	DATE(LocalDate.class, ACArgumentFunction.ALL),
	
	/**
	 * ISO local time. Pattern: HH:mm:ss.
	 */
	TIME(LocalTime.class, ACArgumentFunction.ALL),
	
	/**
	 * ISO local date-time. Pattern: yyyy-MM-ddTHH:mm:ss.
	 */
	DATETIME(LocalDateTime.class, ACArgumentFunction.ALL),
	
	/**
	 * Location identified by a latitude and a longitude value. 
	 */
	LOCATION(Location.class, Arrays.asList(ACArgumentFunction.EQUALS, ACArgumentFunction.NOT_EQUALS));
	
	// - - -
	
	/**
	 * The class to use, e.g., for deserializing and comparing.
	 */
	private Class<?> clazz;
	
	/**
	 * The list of applicable {@link ACArgumentFunction functions}.
	 */
	private List<ACArgumentFunction> applicableArgumentFunctions;
	
	// - - -
	
	/**
	 * All-args constructor.
	 * 
	 * @param clazz the class to use, e.g., for deserializing and comparing. 
	 * @param applicableArgumentFunctions the list of applicable
	 * 		  {@link ACArgumentFunction functions}.
	 */
	private ACDataType(Class<?> clazz, List<ACArgumentFunction> applicableArgumentFunctions) {
		this.applicableArgumentFunctions = applicableArgumentFunctions;
	}
	
	// - - -
	
	public Class<?> getClazz() {
		return clazz;
	}
	
	public List<ACArgumentFunction> getApplicableArgumentFunctions() {
		return applicableArgumentFunctions;
	}

}
