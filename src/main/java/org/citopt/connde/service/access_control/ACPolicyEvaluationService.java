
package org.citopt.connde.service.access_control;

import org.citopt.connde.domain.access_control.ACAccess;
import org.citopt.connde.domain.access_control.ACAccessRequest;
import org.citopt.connde.domain.access_control.ACConditionEvaluatorNotAvailableException;
import org.citopt.connde.domain.access_control.ACPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * TODO: Class comment.
 * 
 * @author Jakob Benz
 */
@Service
public class ACPolicyEvaluationService {
	
	/**
	 * TODO: Method comment (check params).
	 * 
	 * @param policy
	 * @param access
	 * @param request
	 * @return
	 */
	public boolean evaluate(ACPolicy<?> policy, ACAccess access, ACAccessRequest request) {
		// TODO: Next steps
		/*
		 *  Implement condition evaluation service
		 *  Implement this service
		 *  ...
		 */
		
		try {
			return policy.getCondition().evaluate(access, request);
		} catch (ACConditionEvaluatorNotAvailableException e) {
			e.printStackTrace();
			return false;
		}
	}

}
