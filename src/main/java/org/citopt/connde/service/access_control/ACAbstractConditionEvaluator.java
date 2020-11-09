package org.citopt.connde.service.access_control;

import org.citopt.connde.domain.access_control.ACAbstractCondition;
import org.citopt.connde.domain.access_control.ACAccess;
import org.citopt.connde.domain.access_control.ACAccessRequest;

/**
 * Abstract base class for all {@link ACAbstractCondition} evaluator implementations.
 * 
 * @author Jakob Benz
 */
public abstract class ACAbstractConditionEvaluator<T extends ACAbstractCondition> {
	
	/**
	 * Evaluates a condition.
	 * 
	 * @param condition the condition.
	 * @param access the {@link ACAccess}.
	 * @param requestthe {@link ACAccessRequest}.
	 * @return the evaluation result.
	 */
	public abstract boolean evaluate(T condition, ACAccess access, ACAccessRequest request);
	
}
