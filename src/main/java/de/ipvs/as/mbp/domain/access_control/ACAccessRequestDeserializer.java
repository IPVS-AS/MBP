//package org.citopt.connde.domain.access_control;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
//import com.fasterxml.jackson.core.JsonParser;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.DeserializationContext;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
//import com.fasterxml.jackson.databind.node.ArrayNode;
//
///**
// * Custom deserializer for access requests.
// * 
// * @author Jakob Benz
// */
//@Deprecated
//public class ACAccessRequestDeserializer extends StdDeserializer<ACAccessRequest> {
//
//	private static final long serialVersionUID = 5132992557544890793L;
//	
//	public ACAccessRequestDeserializer() {
//		super(ACAccessRequest.class);
//	}
//
//	@Override
//	public ACAccessRequest deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
//		JsonNode rootNode = p.readValueAsTree();
//		ArrayNode contextNode = (ArrayNode) rootNode.get("context");
////		JsonNode requestBodyNode = rootNode.get("requestBody");
//		
//		ObjectMapper om = new ObjectMapper();
//		
//		List<ACAttribute> context = new ArrayList<>();
//		for (JsonNode attribute : contextNode) {
//			ACAttributeKey key = ACAttributeKey.forId(attribute.get("key").asText());
//			if (key != ACAttributeKey.NULL) {
//				context.add(new ACAttribute(key, attribute.get("value").asText()));
//			}
//		}
//		
//		Object requestBody = om.treeToValue(requestBodyNode, Object.class);
//		
//		return new ACAccessRequest(context, requestBody);
//	}
//	
//}
