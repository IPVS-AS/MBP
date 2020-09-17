package org.citopt.connde.service.access_control;

import java.util.List;
import java.util.stream.Collectors;

import org.citopt.connde.domain.access_control.ACAbstractCondition;
import org.citopt.connde.domain.access_control.ACAccess;
import org.citopt.connde.domain.access_control.ACAccessDecision;
import org.citopt.connde.domain.access_control.ACAccessDecisionResult;
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
@Deprecated
// TODO: Probably not required after all...
public class AccessControlService {
	
	@Autowired
	private ACPolicyService policyService;
	
	@Autowired
	private ACConditionService conditionService;

	/**
	 * TODO: Method comment.
	 * 
	 * @param access
	 * @param request
	 * @return
	 */
	public ACAccessDecision check(ACAccess access, ACAccessRequest<?> request) {
		// Check whether the requesting entity is the owner of the requested entity
		if (access.getRequestedEntity().getOwner().getId().equals(access.getRequestingEntity().getId())) {
			return new ACAccessDecision(ACAccessDecisionResult.GRANTED);
		}

		// Check whether there is an applicable policy that grants access
		List<ACPolicy> policies = access.getRequestedEntity().getAccessControlPolicyIds().stream().map(policyService::getForId).collect(Collectors.toList());
		for (ACPolicy policy : policies) {
			try {
				ACAbstractCondition condition = conditionService.getForId(policy.getConditionId());
				if (condition.evaluate(access, request)) {
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
