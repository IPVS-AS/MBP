package org.citopt.connde.domain.access_control;

import java.util.List;

import javax.annotation.Nonnull;
import javax.validation.constraints.Min;

import org.citopt.connde.service.access_control.ACCompositeConditionEvaluator;

/**
 * A composite condition combines two or more {@link IACCondition conditions}
 * using a logical {@link ACLogicalOperator operator}. The logical operator
 * is applied to the conditions in the order they are provided. 
 * 
 * @author Jakob Benz
 */
@ACEvaluate(using = ACCompositeConditionEvaluator.class)
public class ACCompositeCondition implements IACCondition {
	
	/**
	 * The {@link ACLogicalOperator operator} that combines the conditions.
	 */
	@Nonnull
	private ACLogicalOperator operator;
	
	/**
	 * The conditions (at least 2).
	 */
	@Min(2)
	private List<IACCondition> conditions;
	
	// - - -
	
	/**
	 * All-args constructor.
	 * 
	 * @param operator the {@link ACLogicalOperator operator} that combines the conditions.
	 * @param conditions the conditions (at least 2).
	 */
	public ACCompositeCondition(ACLogicalOperator operator, List<IACCondition> conditions) {
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
	
	public List<IACCondition> getConditions() {
		return conditions;
	}
	
	public ACCompositeCondition setConditions(List<IACCondition> conditions) {
		this.conditions = conditions;
		return this;
	}

}
