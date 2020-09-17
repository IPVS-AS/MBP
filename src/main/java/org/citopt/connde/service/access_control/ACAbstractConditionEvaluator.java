package org.citopt.connde.service.access_control;

import org.citopt.connde.domain.access_control.ACAbstractCondition;
import org.citopt.connde.domain.access_control.ACAccess;
import org.citopt.connde.domain.access_control.ACAccessRequest;

/**
 * TODO: Class comment!
 * 
 * @author Jakob Benz
 */
public abstract class ACAbstractConditionEvaluator<T extends ACAbstractCondition> {
	
	/**
	 * TODO: Method comment!
	 * 
	 * @param condition
	 * @param access
	 * @param request
	 * @return
	 */
	public abstract boolean evaluate(T condition, ACAccess access, ACAccessRequest<?> request);
	
}
