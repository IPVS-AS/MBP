package de.ipvs.as.mbp.service.access_control;

import de.ipvs.as.mbp.domain.access_control.ACAccess;
import de.ipvs.as.mbp.domain.access_control.ACAccessRequest;
import de.ipvs.as.mbp.domain.access_control.ACCompositeCondition;
import de.ipvs.as.mbp.domain.access_control.ACSituationCondition;

/**
 * Evaluator for {@link ACSituationCondition conditions}. <b>NOT IMPLEMENTED YET</b> at this point.
 * 
 * @author Jakob Benz
 */
public class ACSituationConditionEvaluator extends ACAbstractConditionEvaluator<ACCompositeCondition> {
	
	public ACSituationConditionEvaluator() {}

	@Override
	public boolean evaluate(ACCompositeCondition condition, ACAccess access, ACAccessRequest request) {
		throw new UnsupportedOperationException("Not implemented yet!");
	}
	
}
