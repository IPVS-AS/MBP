package org.citopt.connde.domain.access_control.jquerybuilder;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Abstract base class for jQuery QueryBuilder rules.
 * 
 * @author Jakob Benz
 */
@JsonDeserialize(using = JQBOutputDeserializer.class)
public class JQBOutput extends JQBRuleGroup {
	
	private boolean valid;

	// - - -
	
	/**
	 * No-args constructor.
	 */
	public JQBOutput() {}
	
	/**
	 * All-args constructor.
	 */
	public JQBOutput(String condition, List<JQBAbstractRuleElement> rules, boolean valid) {
		super(condition, rules);
		this.valid = valid;
	}
	
	// - - -
	
	public boolean isValid() {
		return valid;
	}
	
	public JQBOutput setValid(boolean valid) {
		this.valid = valid;
		return this;
	}
	
}
