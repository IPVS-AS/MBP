package org.citopt.connde.service.access_control;

import org.citopt.connde.repository.ACConditionRepository;
import org.citopt.connde.repository.ACEffectRepository;
import org.citopt.connde.repository.ACPolicyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service that provides additional attributes used for evaluating access requests.
 * 
 * @author Jakob Benz
 */
@Service
public class ACAttributeProvider {
	
	@Autowired
	private ACPolicyRepository policyRepository;
	
	@Autowired
	private ACConditionService conditionService;
	
	@Autowired
	private ACEffectService effectService;
	
	@Autowired
	private ACConditionRepository conditionRepository;
	
	@Autowired
	private ACEffectRepository effectRepository;
	
	// - - -

	

}
