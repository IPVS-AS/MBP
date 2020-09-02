package org.citopt.connde.domain.access_control.jquerybuilder;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Abstract base class for jQuery QueryBuilder rules.
 * 
 * @author Jakob Benz
 */
@JsonDeserialize(using = JQBRuleGroupDeserializer.class)
public class JQBRuleGroup extends JQBAbstractRuleElement {
	
	private String condition;
	private List<JQBAbstractRuleElement> rules = new ArrayList<>();

	// - - -
	
	/**
	 * No-args constructor.
	 */
	public JQBRuleGroup() {}
	
	/**
	 * All-args constructor.
	 */
	public JQBRuleGroup(String condition, List<JQBAbstractRuleElement> rules) {
		this.condition = condition;
		this.rules = rules;
	}
	
	// - - -
	
	public String getCondition() {
		return condition;
	}
	
	public JQBRuleGroup setCondition(String condition) {
		this.condition = condition;
		return this;
	}
	
	public List<JQBAbstractRuleElement> getRules() {
		return rules;
	}
	
	public JQBRuleGroup setRules(List<JQBAbstractRuleElement> rules) {
		this.rules = rules;
		return this;
	}
	
}
