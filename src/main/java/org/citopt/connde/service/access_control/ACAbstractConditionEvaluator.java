package org.citopt.connde.service.access_control;

import org.citopt.connde.domain.access_control.ACAccess;
import org.citopt.connde.domain.access_control.ACAccessRequest;

/**
 * TODO: Class comment!
 * 
 * @author Jakob Benz
 */
public abstract class ACAbstractConditionEvaluator<IACCondition> {
	
	/**
	 * TODO: Method comment!
	 * 
	 * @param condition
	 * @param access
	 * @param request
	 * @return
	 */
	public abstract boolean evaluate(IACCondition condition, ACAccess access, ACAccessRequest request);
	
}
