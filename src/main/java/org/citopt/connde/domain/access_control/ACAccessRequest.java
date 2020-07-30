package org.citopt.connde.domain.access_control;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Abstraction for access requests. Contains the context of the requesting entity
 * as a list of {@link ACAttribute attributes}. The purpose of this wrapper is to
 * provide extendability to the access control framework, e.g., access requests
 * in the future may contain an additional access purpose for more fine-granular
 * decision making.
 * 
 * @author Jakob Benz
 */
public class ACAccessRequest {
	
	/**
	 * The context of the requesting entity as list of {@link ACAttribute attributes}.
	 */
	private List<ACAttribute<? extends Comparable<?>>> context;
	
	// - - -
	
	/**
	 * No-args constructor.
	 */
	public ACAccessRequest() {}
	
	/**
	 * All-args constructor.
	 * 
	 * @param context the context of the requesting entity as list of {@link ACAttribute attributes}.
	 */
	@JsonCreator
	public ACAccessRequest(@JsonProperty("context") List<ACAttribute<? extends Comparable<?>>> context) {
		this.context = context;
	}
	
	// - - -

	public List<ACAttribute<? extends Comparable<?>>> getContext() {
		return context;
	}

	public ACAccessRequest setContext(List<ACAttribute<? extends Comparable<?>>> context) {
		this.context = context;
		return this;
	}
	
}
