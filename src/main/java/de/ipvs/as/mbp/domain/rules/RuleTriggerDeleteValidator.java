package de.ipvs.as.mbp.domain.rules;

import de.ipvs.as.mbp.error.MBPException;
import de.ipvs.as.mbp.repository.RuleRepository;
import de.ipvs.as.mbp.service.validation.IDeleteValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class RuleTriggerDeleteValidator implements IDeleteValidator<RuleTrigger> {

    @Autowired
    private RuleRepository ruleRepository;

    @Override
    public void validateDeletable(RuleTrigger ruleTrigger) {
        //Iterate over all rules
        for (Rule rule : ruleRepository.findAll()) {
            //Check if trigger is part of this rule
            if (rule.getTrigger().equals(ruleTrigger)) {
                throw new MBPException(HttpStatus.CONFLICT, "Rule condition '" + ruleTrigger.getName() + "' cannot be deleted since it is still used by one or more rules.");
            }
        }
    }

}
