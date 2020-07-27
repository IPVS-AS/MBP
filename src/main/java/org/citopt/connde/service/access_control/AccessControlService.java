package org.citopt.connde.service.access_control;

import org.citopt.connde.domain.access_control.ACAccess;
import org.citopt.connde.domain.access_control.ACAccessDecision;
import org.citopt.connde.domain.access_control.ACAccessDecisionResult;
import org.citopt.connde.domain.access_control.ACAccessRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * TODO: Class comment.
 * 
 * @author Jakob Benz
 */
@Service
public class AccessControlService {

//	@Autowired
//	private UserEntityRepository<? extends UserEntity> userEntityRepository;

	@Autowired
	private ACPolicyEvaluationService policyEvaluationService;

	public ACAccessDecision check(ACAccess access, ACAccessRequest request) {
		// Check whether the requesting entity is the owner of the requested entity
		if (access.getRequestedEntity().getOwner().getId().equals(access.getRequestingEntity().getId())) {
			return new ACAccessDecision(ACAccessDecisionResult.GRANTED);
		}

//		List<ACPolicy<?>> policies = access.getRequestedEntity().getAccessControlPolicies();
//		for (ACPolicy<?> policy : policies) {
//
//		}

		// Check whether policy exists that grants access to the requested entity
		// TODO: Implement
		return null;
	}

}
