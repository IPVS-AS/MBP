package org.citopt.connde.service.access_control;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.citopt.connde.domain.access_control.ACAccess;
import org.citopt.connde.domain.access_control.ACAccessRequest;
import org.citopt.connde.domain.access_control.ACCompositeCondition;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * Evaluator for {@link ACCompositeCondition conditions} which uses the Spring Expression Language 
 * <a href="https://docs.spring.io/spring/docs/current/spring-framework-reference/core.html#expressions">(SpEL)</a>
 * to evaluate the logical condition expressions.
 * 
 * @author Jakob Benz
 */
public class ACCompositeConditionEvaluator extends ACAbstractConditionEvaluator<ACCompositeCondition> {
	
	public ACCompositeConditionEvaluator() {}

	@Override
	public boolean evaluate(ACCompositeCondition condition, ACAccess access, ACAccessRequest request) {
		// Create a list with callbacks (as String) that evaluate the conditions, respectively
		List<String> argumentExpressions = IntStream.range(0, condition.getConditions().size()).mapToObj(i -> "get(" + i + ").evaluate(#access, #request)").collect(Collectors.toList());
		
		// Create the expression string using the conditions's logical operator
		String expressionString = condition.getOperator().createExpressionString(argumentExpressions);
		
		// Setup the evaluation context and the expression parser
		StandardEvaluationContext context = new StandardEvaluationContext(condition.getConditions());
		context.setVariable("access", access);
		context.setVariable("request", request);
		ExpressionParser parser = new SpelExpressionParser();
		
		// Parse & evaluate the expression and return the result
		Expression expression = parser.parseExpression(expressionString);
		return expression.getValue(context, Boolean.class);
	}
	
//	public static void main(String[] args) {
//		ACSimpleCondition<Integer> s1 = new ACSimpleCondition<Integer>(ACArgumentFunction.EQUALS, new ACConditionSimpleValueArgument<>(1), new ACConditionSimpleValueArgument<>(1));
//		ACSimpleCondition<Integer> s2 = new ACSimpleCondition<Integer>(ACArgumentFunction.EQUALS, new ACConditionSimpleValueArgument<>(1), new ACConditionSimpleValueArgument<>(2));
//		List<IACCondition> conditions = new ArrayList<>();
//		conditions.add(s1);
//		conditions.add(s2);
//		
//		ACCompositeCondition compositeCondition = new ACCompositeCondition(ACLogicalOperator.AND, conditions);
//		try {
//			System.out.println(compositeCondition.evaluate(null, null));
//		} catch (ACConditionEvaluatorNotAvailableException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
	
}
