package de.ipvs.as.mbp.service;

import de.ipvs.as.mbp.error.MBPException;
import de.ipvs.as.mbp.domain.rules.RuleAction;
import de.ipvs.as.mbp.repository.RuleRepository;
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
