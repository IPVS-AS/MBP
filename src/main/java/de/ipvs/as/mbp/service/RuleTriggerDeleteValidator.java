package de.ipvs.as.mbp.service;

import de.ipvs.as.mbp.error.MBPException;
import de.ipvs.as.mbp.domain.rules.RuleTrigger;
import de.ipvs.as.mbp.repository.RuleRepository;
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
