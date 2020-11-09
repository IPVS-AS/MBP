package org.citopt.connde.service.access_control;

import java.util.Optional;

import org.citopt.connde.domain.access_control.ACAccess;
import org.citopt.connde.domain.access_control.ACAccessRequest;
import org.citopt.connde.domain.access_control.ACAttributeNotAvailableException;
import org.citopt.connde.domain.access_control.ACConditionSimpleAttributeArgument;
import org.citopt.connde.domain.access_control.ACConditionSimpleValueArgument;
import org.citopt.connde.domain.access_control.ACSimpleCondition;
import org.citopt.connde.domain.access_control.IACConditionArgument;

/**
 * Evaluator for simple {@link ACSimpleCondition conditions}.
 * 
 * @param <T> the data type of the simple condition arguments.
 * @author Jakob Benz
 */
public class ACSimpleConditionEvaluator<T extends Comparable<T>> extends ACAbstractConditionEvaluator<ACSimpleCondition<T>> {
	
	private ACAttributeProvider attributeProvider = new ACAttributeProvider();
	
 	@Override
	public boolean evaluate(ACSimpleCondition<T> condition, ACAccess access, ACAccessRequest request) {
		Optional<T> leftValue = getValueForArgument(condition.getLeft(), access, request);
		Optional<T> rightValue = getValueForArgument(condition.getRight(), access, request);
		
		// Non-present (null) values are currently not supported
		if (!leftValue.isPresent()  || !rightValue.isPresent()) {
			return false;
		}
		
		// Compare arguments using the specified function and return result
		return condition.getFunction().apply(leftValue.get(), rightValue.get());
	}
	
	@SuppressWarnings("unchecked")
	private Optional<T> getValueForArgument(IACConditionArgument argument, ACAccess access, ACAccessRequest request) {
		if (argument instanceof ACConditionSimpleAttributeArgument<?>) {
			try {
				return attributeProvider.getValueForAttributeArgument((ACConditionSimpleAttributeArgument<T>) argument, access, request);
			} catch (ACAttributeNotAvailableException e) {
				return Optional.empty();
			}
		} else if (argument instanceof ACConditionSimpleValueArgument<?>) {
			return Optional.of((T) ((ACConditionSimpleValueArgument<T>) argument).getValue());
		} else {
			return null;
		}
	}
	
}
