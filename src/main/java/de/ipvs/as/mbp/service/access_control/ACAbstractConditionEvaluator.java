package de.ipvs.as.mbp.service.access_control;

import de.ipvs.as.mbp.domain.access_control.ACAbstractCondition;
import de.ipvs.as.mbp.domain.access_control.ACAccess;
import de.ipvs.as.mbp.domain.access_control.ACAccessRequest;

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
