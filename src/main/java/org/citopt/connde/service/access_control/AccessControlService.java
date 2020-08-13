package org.citopt.connde.service.access_control;

import java.util.Comparator;
import java.util.List;

import org.citopt.connde.domain.access_control.ACAccess;
import org.citopt.connde.domain.access_control.ACAccessDecision;
import org.citopt.connde.domain.access_control.ACAccessDecisionResult;
import org.citopt.connde.domain.access_control.ACAccessRequest;
import org.citopt.connde.domain.access_control.ACConditionEvaluatorNotAvailableException;
import org.citopt.connde.domain.access_control.ACPolicy;
import org.springframework.stereotype.Service;

/**
 * TODO: Class comment.
 * 
 * @author Jakob Benz
 */
@Service
public class AccessControlService {

	/**
	 * TODO: Method comment.
	 * 
	 * @param access
	 * @param request
	 * @return
	 */
	public ACAccessDecision check(ACAccess access, ACAccessRequest request) {
		// Check whether the requesting entity is the owner of the requested entity
		if (access.getRequestedEntity().getOwner().getId().equals(access.getRequestingEntity().getId())) {
			return new ACAccessDecision(ACAccessDecisionResult.GRANTED);
		}

		// Get access control policies and sort them by their respective priority
		List<ACPolicy> policies = access.getRequestedEntity().getAccessControlPolicies();
		policies.sort(Comparator.comparing(ACPolicy::getPriority));
		
		// Check whether there is an applicable policy that grants access
		for (ACPolicy policy : policies) {
			try {
				if (policy.getCondition().evaluate(access, request)) {
					return new ACAccessDecision(ACAccessDecisionResult.GRANTED);
				}
			} catch (ACConditionEvaluatorNotAvailableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// No policy specified that grants access
		return new ACAccessDecision(ACAccessDecisionResult.DENIED);
	}

}
