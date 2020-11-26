package org.citopt.connde.domain.access_control;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Enumeration for different logical operators, such as AND, OR, ...
 * to be used with {@link ACCompositeCondition}. Note that the expressions
 * are specified for use with the Spring Expression Language 
 * <a href="https://docs.spring.io/spring/docs/current/spring-framework-reference/core.html#expressions">(SpEL)</a>.
 * 
 * @author Jakob Benz
 */
public enum ACLogicalOperator {
	
	/**
	 * Logical AND, both arguments have to be {@code true}.
	 */
	AND(Integer.MAX_VALUE, l -> l.stream().collect(Collectors.joining(" AND "))),
	
	/**
	 * Logical OR, at least one the arguments has to be {@code true}.
	 */
	OR(Integer.MAX_VALUE, l -> l.stream().collect(Collectors.joining(" OR ")));
	
//	/**
//	 * Logical NAND, at least one the arguments has to be {@code false}.
//	 */
//	NAND(Integer.MAX_VALUE, l -> "NOT (" + l.stream().collect(Collectors.joining(" AND ")) + ")"),
//	
//	/**
//	 * Logical NOR, both arguments have to be {@code false}. 
//	 */
//	NOR(Integer.MAX_VALUE, l -> "NOT (" + l.stream().collect(Collectors.joining(" OR ")) + ")"),
//	
//	/**
//	 * Logical XOR, the arguments have to be different, i.e. one has
//	 * to be {@code true}, the other has to {@code false}.
//	 */
//	XOR(2, l -> l.get(0) + " != " + l.get(1));
	
	// - - -
	
	private int maxArguments;
	private Function<List<String>, String> expressionSupplier;
	
	private ACLogicalOperator(int maxArguments, Function<List<String>, String> expressionSupplier) {
		this.maxArguments = maxArguments;
		this.expressionSupplier = expressionSupplier;
	}
	
	public int getMaxArguments() {
		return maxArguments;
	}
	
	public Function<List<String>, String> getExpressionSupplier() {
		return expressionSupplier;
	}
	
	// - - -
	
	public String createExpressionString(List<String> arguments) {
		return expressionSupplier.apply(arguments);
	}
	
}
