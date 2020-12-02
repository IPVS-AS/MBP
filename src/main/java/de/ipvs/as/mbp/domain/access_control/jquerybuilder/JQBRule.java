package de.ipvs.as.mbp.domain.access_control.jquerybuilder;

/**
 * Abstract base class for jQuery QueryBuilder rules.
 * 
 * @author Jakob Benz
 */
public class JQBRule extends JQBAbstractRuleElement {
	
	private String id;
	private String field;
	private String type;
	private String input;
	private String operator;
	private String value;

	// - - -
	
	/**
	 * No-args constructor.
	 */
	public JQBRule() {}
	
	/**
	 * All-args constructor.
	 */
	public JQBRule(String id, String field, String type, String input, String operator, String value) {
		this.id = id;
		this.field = field;
		this.type = type;
		this.input = input;
		this.operator = operator;
		this.value = value;
	}
	
	// - - -
	
	public String getId() {
		return id;
	}
	
	public JQBRule setId(String id) {
		this.id = id;
		return this;
	}
	
	public String getField() {
		return field;
	}
	
	public JQBRule setField(String field) {
		this.field = field;
		return this;
	}
	
	public String getType() {
		return type;
	}
	
	public JQBRule setType(String type) {
		this.type = type;
		return this;
	}
	
	public String getInput() {
		return input;
	}
	
	public JQBRule setInput(String input) {
		this.input = input;
		return this;
	}
	
	public String getOperator() {
		return operator;
	}
	
	public JQBRule setOperator(String operator) {
		this.operator = operator;
		return this;
	}
	
	public String getValue() {
		return value;
	}
	
	public JQBRule setValue(String value) {
		this.value = value;
		return this;
	}
	
}
