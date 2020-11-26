package org.citopt.connde.domain.access_control;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.validation.constraints.Min;

import org.citopt.connde.domain.access_control.jquerybuilder.JQBAbstractRuleElement;
import org.citopt.connde.domain.access_control.jquerybuilder.JQBRule;
import org.citopt.connde.domain.access_control.jquerybuilder.JQBRuleGroup;
import org.citopt.connde.domain.user.User;
import org.citopt.connde.service.access_control.ACCompositeConditionEvaluator;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.hateoas.server.core.Relation;

/**
 * A composite condition combines two or more {@link IACCondition conditions}
 * using a logical {@link ACLogicalOperator operator}. The logical operator
 * is applied to the conditions in the order they are provided. 
 * 
 * @author Jakob Benz
 */
@Document
@ACEvaluate(using = ACCompositeConditionEvaluator.class)
@Relation(collectionRelation = "policy-conditions", itemRelation = "policy-conditions")
public class ACCompositeCondition extends ACAbstractCondition {
	
	/**
	 * The {@link ACLogicalOperator operator} that combines the conditions.
	 */
	@Nonnull
	private ACLogicalOperator operator;
	
	/**
	 * The conditions (at least 2).
	 */
	@Min(2)
	private List<ACAbstractCondition> conditions = new ArrayList<>();
	
	// - - -
	
	/**
	 * No-args constructor.
	 */
	public ACCompositeCondition() {}
	
	/**
	 * All-args constructor.
	 * 
	 * @param name the name of this condition.
	 * @param description the description of this condition.
	 * @param operator the {@link ACLogicalOperator operator} that combines the conditions.
	 * @param conditions the conditions (at least 2).
	 * @param ownerId the id of the {@link User} that owns this policy.
	 */
	public ACCompositeCondition(String name, String description, ACLogicalOperator operator, List<ACAbstractCondition> conditions, String ownerId) {
		super(name, description, ownerId);
		this.operator = operator;
		this.conditions = conditions;
	}
	
	// - - -
	
	public ACLogicalOperator getOperator() {
		return operator;
	}
	
	public ACCompositeCondition setOperator(ACLogicalOperator operator) {
		this.operator = operator;
		return this;
	}
	
	public List<ACAbstractCondition> getConditions() {
		return conditions;
	}
	
	public ACCompositeCondition setConditions(List<ACAbstractCondition> conditions) {
		this.conditions = conditions;
		return this;
	}
	
	// - - -
	
	public ACCompositeCondition addCondition(ACAbstractCondition condition) {
		conditions.add(condition);
		return this;
	}
	
	@Override
	public ACAbstractCondition computeAndSetHumanReadableDescription() {
		super.computeAndSetHumanReadableDescription();
		conditions.forEach(c -> c.computeAndSetHumanReadableDescription());
		return this;
	}
	
	@Override
	public String toHumanReadableString() {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		for (int i = 0; i < conditions.size(); i++) {
			if (i > 0 && i < conditions.size()) {
				sb.append(" ").append(operator.toString().toUpperCase()).append(" ");
			}
			sb.append(conditions.get(i).toHumanReadableString());
		}
		sb.append(")");
		return sb.toString();
	}

	// - - -
	
	/**
	 * Builds a composite condition based on a single rule from the jQuery QueryBuilder.
	 * 
	 * @param ruleGroup the {@link JQBRuleGroup}.
	 * @return the {@link ACCompositeCondition}.
	 */
	public static ACCompositeCondition forJQBRuleGroup(JQBRuleGroup ruleGroup) {
		ACCompositeCondition compositeCondition = new ACCompositeCondition()
				.setOperator(ACLogicalOperator.valueOf(ruleGroup.getCondition()));
		
		for (JQBAbstractRuleElement element : ruleGroup.getRules()) {
			if (element instanceof JQBRule) {
				compositeCondition.addCondition(ACSimpleCondition.forJQBRule((JQBRule) element));
			} else if (element instanceof JQBRuleGroup) {
				compositeCondition.addCondition(ACCompositeCondition.forJQBRuleGroup((JQBRuleGroup) element));
			}
		}
		
		return compositeCondition;
	}

}
