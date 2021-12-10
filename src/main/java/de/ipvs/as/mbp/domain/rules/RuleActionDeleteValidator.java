package de.ipvs.as.mbp.domain.rules;

import de.ipvs.as.mbp.error.MBPException;
import de.ipvs.as.mbp.repository.RuleRepository;
import de.ipvs.as.mbp.service.validation.IDeleteValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;


@Service
public class RuleActionDeleteValidator implements IDeleteValidator<RuleAction> {

    @Autowired
    private RuleRepository ruleRepository;

    @Override
    public void validateDeletable(RuleAction ruleAction) {
        //Iterate over all rules
        for (Rule rule : ruleRepository.findAll()) {
            //Check if action is part of this rule
            if (rule.getActions().contains(ruleAction)) {
                throw new MBPException(HttpStatus.CONFLICT, "Rule action '" + ruleAction.getName() + "' cannot be deleted since it is still used by one or more rules.");
            }
        }
    }
}
