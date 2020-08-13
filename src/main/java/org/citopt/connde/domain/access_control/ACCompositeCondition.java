package org.citopt.connde.domain.access_control;

import java.util.List;

import javax.annotation.Nonnull;
import javax.persistence.GeneratedValue;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;

import org.citopt.connde.service.access_control.ACCompositeConditionEvaluator;
import org.springframework.data.annotation.Id;

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
	 * The id of this policy.
	 */
	@Id
    @GeneratedValue
	private String id;
	
	/**
	 * The name of this condition.
	 */
	@NotEmpty
	private String name;
	
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
	 * No-args constructor.
	 */
	public ACCompositeCondition() {}
	
	/**
	 * All-args constructor.
	 * 
	 * @param the name of this condition.
	 * @param operator the {@link ACLogicalOperator operator} that combines the conditions.
	 * @param conditions the conditions (at least 2).
	 */
	public ACCompositeCondition(String name, ACLogicalOperator operator, List<IACCondition> conditions) {
		this.name = name;
		this.operator = operator;
		this.conditions = conditions;
	}
	
	// - - -
	
	@Override
	public String getId() {
		return id;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	public ACCompositeCondition setName(String name) {
		this.name = name;
		return this;
	}
	
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
