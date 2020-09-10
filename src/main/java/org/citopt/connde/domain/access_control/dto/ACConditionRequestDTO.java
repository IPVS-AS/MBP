package org.citopt.connde.domain.access_control.dto;

import org.citopt.connde.domain.access_control.ACAbstractCondition;
import org.citopt.connde.domain.access_control.ACSimpleCondition;
import org.citopt.connde.domain.access_control.jquerybuilder.JQBOutput;
import org.citopt.connde.domain.access_control.jquerybuilder.JQBRule;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Request DTO for {@link ACAbstractCondition}.
 * 
 * @author Jakob Benz
 */
public class ACConditionRequestDTO extends ACAbstractEntityRequestDTO {
	
	private String condition;
	
	// - - -
	
	public ACConditionRequestDTO() {}
	
	public ACConditionRequestDTO(String name, String description, String condition) {
		super(name, description);
		this.condition = condition;
	}
	
	// - - -
	
	public String getCondition() {
		return condition;
	}
	
	public ACConditionRequestDTO setCondition(String condition) {
		this.condition = condition;
		return this;
	}
	
	// -----------------------------------------------------------------------------
	
	public static void main(String[] args) throws JsonMappingException, JsonProcessingException {
		ObjectMapper om = new ObjectMapper();
		
		String output = "{\n" + 
				"  \"condition\": \"AND\",\n" + 
				"  \"rules\": [\n" + 
				"    {\n" + 
				"      \"id\": \"entity-owner-id\",\n" + 
				"      \"field\": \"entity-owner-id\",\n" + 
				"      \"type\": \"string\",\n" + 
				"      \"input\": \"text\",\n" + 
				"      \"operator\": \"not_equal\",\n" + 
				"      \"value\": \"valueXY\"\n" + 
				"    },\n" + 
				"    {\n" + 
				"      \"condition\": \"OR\",\n" + 
				"      \"rules\": [\n" + 
				"        {\n" + 
				"          \"id\": \"category\",\n" + 
				"          \"field\": \"category\",\n" + 
				"          \"type\": \"integer\",\n" + 
				"          \"input\": \"select\",\n" + 
				"          \"operator\": \"equal\",\n" + 
				"          \"value\": 2\n" + 
				"        },\n" + 
				"        {\n" + 
				"          \"id\": \"category\",\n" + 
				"          \"field\": \"category\",\n" + 
				"          \"type\": \"integer\",\n" + 
				"          \"input\": \"select\",\n" + 
				"          \"operator\": \"equal\",\n" + 
				"          \"value\": 1\n" + 
				"        }\n" + 
				"      ]\n" + 
				"    }\n" + 
				"  ],\n" + 
				"  \"valid\": true\n" + 
				"}";
		
		JQBOutput o = om.readValue(output, JQBOutput.class);
		System.out.println(om.writerWithDefaultPrettyPrinter().writeValueAsString(o));
		System.out.println(om.writerWithDefaultPrettyPrinter().writeValueAsString(ACSimpleCondition.forJQBRule((JQBRule) o.getRules().get(0))));
	}
	
}
