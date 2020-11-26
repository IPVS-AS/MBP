package org.citopt.connde.service;

import org.citopt.connde.domain.rules.RuleTrigger;
import org.citopt.connde.error.MBPException;
import org.citopt.connde.repository.RuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * @author Jakob Benz
 */
@Service
public class RuleTriggerDeleteValidator implements IDeleteValidator<RuleTrigger> {
	
	@Autowired
	private RuleRepository ruleRepository;
	
	@Override
	public void validateDeletable(RuleTrigger ruleTrigger) {
		if (!ruleRepository.findAllByTriggerId(ruleTrigger.getId()).isEmpty()) {
			throw new MBPException(HttpStatus.CONFLICT, "Rule trigger '" + ruleTrigger.getName() + "' cannot be deleted since it is still used by one or more rules.");
		}
	}

}
