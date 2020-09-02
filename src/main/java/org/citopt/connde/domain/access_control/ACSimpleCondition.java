package org.citopt.connde.domain.access_control;

import javax.annotation.Nonnull;

import org.citopt.connde.domain.access_control.jquerybuilder.JQBRule;
import org.citopt.connde.domain.user.User;
import org.citopt.connde.service.access_control.ACSimpleConditionEvaluator;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * A simple condition that can be used to compare two arguments.
 * 
 * @param <T> the data type of the condition arguments.
 * @author Jakob Benz
 */
@Document
@ACEvaluate(using = ACSimpleConditionEvaluator.class)
public class ACSimpleCondition<T extends Comparable<T>> extends ACAbstractCondition {
	
	/**
	 * The {@link ACArgumentFunction function}.
	 */
	@Nonnull
	private ACArgumentFunction function;
	
	/**
	 * The first (left) {@link IACConditionArgument argument}.
	 */
	@Nonnull
	private IACConditionArgument left;
	
	/**
	 * The second (right) {@link IACConditionArgument argument}.
	 */
	@Nonnull
	private IACConditionArgument right;
	
	// - - -
	
	/**
	 * No-args constructor.
	 */
	public ACSimpleCondition() {
		super();
	}
	
	/**
	 * All-args constructor.
	 * 
	 * @param name the name of this condition.
	 * @param description the description of this condition.
	 * @param function the {@link ACArgumentFunction function}.
	 * @param left the first (left) {@link IACConditionArgument argument}.
	 * @param right the second (right) {@link IACConditionArgument argument}.
	 * @param owner the {@link User} that owns this condition.
	 */
	public ACSimpleCondition(String name, String description, ACArgumentFunction function, IACConditionArgument left, IACConditionArgument right, User owner) {
		super(name, description, owner);
		this.function = function;
		this.left = left;
		this.right = right;
	}
	
	// - - -
	
	public ACArgumentFunction getFunction() {
		return function;
	}

	public ACSimpleCondition<T> setFunction(ACArgumentFunction function) {
		this.function = function;
		return this;
	}

	public IACConditionArgument getLeft() {
		return left;
	}

	public ACSimpleCondition<T> setLeft(IACConditionArgument left) {
		this.left = left;
		return this;
	}

	public IACConditionArgument getRight() {
		return right;
	}

	public ACSimpleCondition<T> setRight(IACConditionArgument right) {
		this.right = right;
		return this;
	}
	
	// - - -
	
	/**
	 * Convenience function to create a new simple condition for comparing
	 * an attribute value with a fixed value.
	 * 
	 * @param the name of the condition.
	 * @param function the {@link ACArgumentFunction}.
	 * @param entityType the {@link ACEntityType} of the entity the attribute refers to.
	 * @param key the ACAttributeKey of the attribute.
	 * @param right the second (right) argument.
	 * @return the created {@link ACSimpleCondition}.
	 * @param the {@link User} that owns this condition.
	 */
	public static <T extends Comparable<T>> ACSimpleCondition<T> create(String name, String description, ACArgumentFunction function, ACEntityType entityType, ACAttributeKey key, T right, User owner) {
		return new ACSimpleCondition<T>(name, description, function, new ACConditionSimpleAttributeArgument<>(entityType, key), new ACConditionSimpleValueArgument<>(right), owner);
	}
	
	/**
	 * Builds a simple condition based on a single rule from the jQuery QueryBuilder.
	 * 
	 * @param <T> the data type of the condition arguments.
	 * @param rule the {@link JQBRule}.
	 * @return the {@link ACSimpleCondition}.
	 */
	public static <T extends Comparable<T>> ACSimpleCondition<T> forJQBRule(JQBRule rule) {
		return new ACSimpleCondition<T>()
				.setFunction(ACArgumentFunction.basedOn(rule.getOperator()))
				.setLeft(ACConditionSimpleAttributeArgument.basedOn(rule))
				.setRight(ACConditionSimpleValueArgument.basedOn(rule));
	}
	
}
