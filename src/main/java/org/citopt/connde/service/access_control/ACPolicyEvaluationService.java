
package org.citopt.connde.service.access_control;

import org.citopt.connde.domain.access_control.ACAbstractCondition;
import org.citopt.connde.domain.access_control.ACAccess;
import org.citopt.connde.domain.access_control.ACAccessRequest;
import org.citopt.connde.domain.access_control.ACConditionEvaluatorNotAvailableException;
import org.citopt.connde.domain.access_control.ACPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for evaluating instances of {@link ACPolicy}.
 * 
 * @author Jakob Benz
 */
@Service
public class ACPolicyEvaluationService {
	
	@Autowired
	private ACConditionService conditionService;
	
	/**
	 * Evaluates a given policy based on an access request.
	 * 
	 * @param policy the {@link ACPolicy}.
	 * @param access the {@link ACAccess} holding the access type as well as
	 * 		  the information about the requesting and the requested identity.
	 * @param request the {@link ACAccessRequest} holding the attributes of the requesting entity.
	 * @return {@code true} if and only if the {@link IACCondition} of the policy holds; {@code false} otherwise.
	 */
	public boolean evaluate(ACPolicy policy, ACAccess access, ACAccessRequest request) {
		ACAbstractCondition condition = conditionService.getForId(policy.getConditionId());
		try {
			return condition.evaluate(access, request);
		} catch (ACConditionEvaluatorNotAvailableException e) {
			e.printStackTrace();
			return false;
		}
	}

}
