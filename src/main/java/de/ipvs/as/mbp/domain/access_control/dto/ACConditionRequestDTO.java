package de.ipvs.as.mbp.domain.access_control.dto;

import de.ipvs.as.mbp.domain.access_control.ACAbstractCondition;

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
				"      \"value\": \"value111\"\n" + 
				"    },\n" + 
				"    {\n" + 
				"      \"condition\": \"OR\",\n" + 
				"      \"rules\": [\n" + 
				"        {\n" + 
				"          \"id\": \"requesting-entity-firstname\",\n" + 
				"          \"field\": \"requesting-entity-firstname\",\n" + 
				"          \"type\": \"string\",\n" + 
				"          \"input\": \"text\",\n" + 
				"          \"operator\": \"equal\",\n" + 
				"      	   \"value\": \"value222\"\n" +  
				"        },\n" + 
				"        {\n" + 
				"          \"id\": \"requesting-entity-lastname\",\n" + 
				"          \"field\": \"requesting-entity-lastname\",\n" + 
				"          \"type\": \"string\",\n" + 
				"          \"input\": \"text\",\n" + 
				"          \"operator\": \"equal\",\n" + 
				"          \"value\": \"value333\"\n" + 
				"        }\n" + 
				"      ]\n" + 
				"    }\n" + 
				"  ],\n" + 
				"  \"valid\": true\n" + 
				"}";
		
//		JQBOutput o = om.readValue(output, JQBOutput.class);
//		System.out.println(om.writerWithDefaultPrettyPrinter().writeValueAsString(o));
//		System.out.println(om.writerWithDefaultPrettyPrinter().writeValueAsString(ACSimpleCondition.forJQBRule((JQBRule) o.getRules().get(0))));
		
		ACAbstractCondition c = ACAbstractCondition.forJQBOutput(output);
		System.err.println(c.toHumanReadableString());
		System.out.println(c.getHumanReadableDescription());
	}
	
}
