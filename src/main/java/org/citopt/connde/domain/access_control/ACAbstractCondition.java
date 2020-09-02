package org.citopt.connde.domain.access_control;

import org.citopt.connde.domain.access_control.jquerybuilder.JQBOutput;
import org.citopt.connde.domain.access_control.jquerybuilder.JQBRule;
import org.citopt.connde.domain.user.User;
import org.citopt.connde.service.access_control.ACAbstractConditionEvaluator;

/**
 * Abstract base class for all access-control policy conditions.
 * 
 * @author Jakob Benz
 */
public abstract class ACAbstractCondition extends ACAbstractEntity {
	
	/**
	 * No-args constructor.
	 */
	public ACAbstractCondition() {}
	
	/**
	 * All-args constructor.
	 * 
	 * @param name the name of this condition.
	 * @param description the description of this condition.
	 * @param owner the {@link User} that owns this condition.
	 */
	public ACAbstractCondition(String name, String description, User owner) {
		super(name, description, owner);
	}

	// - - -
	
	public static ACAbstractCondition forJQBOutput(JQBOutput output) {
		if (output.getRules().size() == 1) {
			// Only one simple condition
			return ACSimpleCondition.forJQBRule((JQBRule) output.getRules().get(0));
		} else {
			return ACCompositeCondition.forJQBRuleGroup(output);
		}
	}
	
	/**
	 * TODO: Method comment (check params).
	 * 
	 * @param access
	 * @param request
	 * @return
	 * @throws ACConditionEvaluatorNotAvailableException 
	 */
	public boolean evaluate(ACAccess access, ACAccessRequest request) throws ACConditionEvaluatorNotAvailableException {
		return findEvaluator().evaluate(this, access, request);
	}
	
	/**
	 * @return
	 * @throws ACConditionEvaluatorNotAvailableException
	 */
	ACAbstractConditionEvaluator<ACAbstractCondition> findEvaluator() throws ACConditionEvaluatorNotAvailableException {
		// Check whether the ACEvaluate annotation has been specified for the condition to evaluate
		if (!getClass().isAnnotationPresent(ACEvaluate.class)) {
			throw new MissingAnnotationException(ACEvaluate.class, getClass().getName());
		}
		
		// Lookup ACEvalute annotation
		ACEvaluate evaluateAnnotation = getClass().getAnnotation(ACEvaluate.class);
		
		// Lookup the condition evaluator class
		@SuppressWarnings("unchecked")
		Class<? extends ACAbstractConditionEvaluator<ACAbstractCondition>> evaluatorClass = (Class<? extends ACAbstractConditionEvaluator<ACAbstractCondition>>) evaluateAnnotation.using();
		
		// Create and return new instance of the evaluator class
		try {
			return evaluatorClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
			throw new ACConditionEvaluatorNotAvailableException(e.getMessage(), e);
		}
	}
	
}
