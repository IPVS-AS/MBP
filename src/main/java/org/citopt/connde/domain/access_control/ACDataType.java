package org.citopt.connde.domain.access_control;

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
	NUNERIC(ACArgumentFunction.ALL),
	
	/**
	 * Only letters.
	 */
	ALPHABETIC(Arrays.asList(ACArgumentFunction.EQUALS, ACArgumentFunction.NOT_EQUALS)),
	
	/**
	 * Only letters and/or numbers.
	 */
	ALPHANUMERIC(Arrays.asList(ACArgumentFunction.EQUALS, ACArgumentFunction.NOT_EQUALS)),
	
	/**
	 * Only ASCII symbols.
	 */
	ASCII(Arrays.asList(ACArgumentFunction.EQUALS, ACArgumentFunction.NOT_EQUALS)),
	
	/**
	 * ISO local date. Pattern: yyyy-MM-dd.
	 */
	DATE(ACArgumentFunction.ALL),
	
	/**
	 * ISO local time. Pattern: HH:mm:ss.
	 */
	TIME(ACArgumentFunction.ALL),
	
	/**
	 * ISO local date-time. Pattern: yyyy-MM-ddTHH:mm:ss.
	 */
	DATETIME(ACArgumentFunction.ALL),
	
	/**
	 * Location identified by a latitude and a longitude value. 
	 */
	LOCATION(Arrays.asList(ACArgumentFunction.EQUALS, ACArgumentFunction.NOT_EQUALS));
	
	// - - -
	
	/**
	 * The list of applicable {@link ACArgumentFunction functions}.
	 */
	private List<ACArgumentFunction> applicableArgumentFunctions;
	
	// - - -
	
	/**
	 * All-args constructor.
	 * 
	 * @param applicableArgumentFunctions the list of applicable
	 * 		  {@link ACArgumentFunction functions}.
	 */
	private ACDataType(List<ACArgumentFunction> applicableArgumentFunctions) {
		this.applicableArgumentFunctions = applicableArgumentFunctions;
	}
	
	// - - -
	
	public List<ACArgumentFunction> getApplicableArgumentFunctions() {
		return applicableArgumentFunctions;
	}

}
