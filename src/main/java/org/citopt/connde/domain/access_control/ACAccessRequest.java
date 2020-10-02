package org.citopt.connde.domain.access_control;

import java.util.ArrayList;
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
//@JsonDeserialize(using = ACAccessRequestDeserializer.class)
public class ACAccessRequest {
	
	/**
	 * The context of the requesting entity as list of {@link ACAttribute attributes}. // X-MBP
	 */
	private List<ACAttribute> context = new ArrayList<>();
	
	// - - -
	
	/**
	 * All-args constructor.
	 * 
	 * @param context the context of the requesting entity as list of {@link ACAttribute attributes}.
	 */
	@JsonCreator
	public ACAccessRequest(@JsonProperty("context") List<ACAttribute> context) {
		this.context = context;
	}
	
	// - - -

	public List<ACAttribute> getContext() {
		return context;
	}

	public ACAccessRequest setContext(List<ACAttribute> context) {
		this.context = context;
		return this;
	}
	
	// - - -
	
	public static ACAccessRequest valueOf(String accessRequestHeader) {
		List<ACAttribute> context = new ArrayList<>();
		
		String[] attributes = accessRequestHeader.split(";;");
		for (String attribute : attributes) {
			String[] keyValue = attribute.split("=");
			context.add(new ACAttribute(ACAttributeKey.forId(keyValue[0]), keyValue[1]));
		}
		
		return new ACAccessRequest(context);
	}
	
}
