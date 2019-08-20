package org.citopt.connde.service.cep.trigger;

import org.citopt.connde.domain.rules.RuleTrigger;
import org.citopt.connde.service.cep.engine.core.output.CEPOutput;

/**
 * Interface for fire callbacks of rule triggers.
 */
public interface RuleTriggerCallback {
    /**
     * This method is called in case a dedicated trigger fires.
     *
     * @param ruleTrigger The rule trigger which fires
     * @param output      The output of the CEP query of the rule trigger
     */
    void onTriggerFired(RuleTrigger ruleTrigger, CEPOutput output);
}
