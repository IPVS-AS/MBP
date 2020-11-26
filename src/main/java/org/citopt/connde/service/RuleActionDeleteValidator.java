package org.citopt.connde.service;

import org.citopt.connde.domain.rules.RuleAction;
import org.citopt.connde.error.MBPException;
import org.citopt.connde.repository.RuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * @author Jakob Benz
 */
@Service
public class RuleActionDeleteValidator implements IDeleteValidator<RuleAction> {
	
	@Autowired
	private RuleRepository ruleRepository;
	
	@Override
	public void validateDeletable(RuleAction ruleAction) {
		if (!ruleRepository.findAllByActionId(ruleAction.getId()).isEmpty()) {
			throw new MBPException(HttpStatus.CONFLICT, "Rule action '" + ruleAction.getName() + "' cannot be deleted since it is still used by one or more rules.");
		}
	}

}
