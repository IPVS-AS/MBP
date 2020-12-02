package de.ipvs.as.mbp.domain.access_control.jquerybuilder;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

/**
 * Custom deserializer for jQuery QueryBuilder output.
 * 
 * @author Jakob Benz
 */
public class JQBRuleGroupDeserializer extends StdDeserializer<JQBRuleGroup> {

	private static final long serialVersionUID = -3823731878810346203L;

	public JQBRuleGroupDeserializer() {
		super(JQBRuleGroup.class);
	}

	@Override
	public JQBRuleGroup deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		// Get the root node and the node with all the top level rules
		JsonNode rootNode = p.readValueAsTree();
		JsonNode rulesNode = rootNode.get("rules");
		
		JQBRuleGroup ruleGroup = new JQBRuleGroup();
		ruleGroup.setCondition(rootNode.get("condition").asText());
		
		ObjectMapper om = new ObjectMapper();
		for (JsonNode rule : rulesNode) {
			if (rule.has("condition")) {
				// It's a rule group
				JQBRuleGroup rg = om.treeToValue(rule, JQBRuleGroup.class);
				ruleGroup.getRules().add(rg);
			} else {
				// It's a simple rule
				JQBRule r = om.treeToValue(rule, JQBRule.class);
				ruleGroup.getRules().add(r);
			}
		}
		
		return ruleGroup;
	}
	
	
	
}
