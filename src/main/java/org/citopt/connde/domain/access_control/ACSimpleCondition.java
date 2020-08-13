package org.citopt.connde.domain.access_control;

import javax.annotation.Nonnull;
import javax.persistence.GeneratedValue;
import javax.validation.constraints.NotEmpty;

import org.citopt.connde.service.access_control.ACSimpleConditionEvaluator;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * A simple condition that can be used to compare two arguments.
 * 
 * @param <T> the data type of the condition arguments
 * @author Jakob Benz
 */
@Document
@ACEvaluate(using = ACSimpleConditionEvaluator.class)
public class ACSimpleCondition<T extends Comparable<T>> implements IACCondition {
	
	/**
	 * The id of this condition.
	 */
	@Id
    @GeneratedValue
	private String id;
	
	/**
	 * The name of this condition;
	 */
	@NotEmpty
	private String name;
	
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
	public ACSimpleCondition() {}
	
	/**
	 * All-args constructor.
	 * 
	 * @param name the name of this condition.
	 * @param function the {@link ACArgumentFunction function}.
	 * @param left the first (left) {@link IACConditionArgument argument}.
	 * @param right the second (right) {@link IACConditionArgument argument}.
	 */
	public ACSimpleCondition(String name, ACArgumentFunction function, IACConditionArgument left, IACConditionArgument right) {
		this.name = name;
		this.function = function;
		this.left = left;
		this.right = right;
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
	
	public ACSimpleCondition<T> setName(String name) {
		this.name = name;
		return this;
	}

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
	 * @param key the key of the attribute.
	 * @param right the second (right) argument.
	 * @return the created {@link ACSimpleCondition}.
	 */
	public static <T extends Comparable<T>> ACSimpleCondition<T> create(String name, ACArgumentFunction function, ACEntityType entityType, String key, T right) {
		return new ACSimpleCondition<T>(name, function, new ACConditionSimpleAttributeArgument<>(entityType, key), new ACConditionSimpleValueArgument<>(right));
	}
	
}
