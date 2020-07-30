package org.citopt.connde.domain.access_control;

import org.citopt.connde.service.access_control.ACAbstractConditionEvaluator;

/**
 * Interface implemented by all policy condition classes.
 * 
 * @author Jakob Benz
 */
public interface IACCondition {
	
	/**
	 * TODO: Method comment (check params).
	 * 
	 * @param access
	 * @param request
	 * @return
	 * @throws ACConditionEvaluatorNotAvailableException 
	 */
	default public boolean evaluate(ACAccess access, ACAccessRequest request) throws ACConditionEvaluatorNotAvailableException {
		return findEvaluator().evaluate(this, access, request);
	}
	
	/**
	 * @return
	 * @throws ACConditionEvaluatorNotAvailableException
	 */
	default ACAbstractConditionEvaluator<IACCondition> findEvaluator() throws ACConditionEvaluatorNotAvailableException {
		// Check whether the ACEvaluate annotation has been specified for the condition to evaluate
		if (!getClass().isAnnotationPresent(ACEvaluate.class)) {
			throw new MissingAnnotationException(ACEvaluate.class, getClass().getName());
		}
		
		// Lookup ACEvalute annotation
		ACEvaluate evaluateAnnotation = getClass().getAnnotation(ACEvaluate.class);
		
		// Lookup the condition evaluator class
		@SuppressWarnings("unchecked") // to work around JDK8 bug regarding Class-valued annotation properties
		Class<? extends ACAbstractConditionEvaluator<IACCondition>> evaluatorClass = (Class<? extends ACAbstractConditionEvaluator<IACCondition>>) evaluateAnnotation.using();
		
		// Create and return new instance of the evaluator class
		try {
			return evaluatorClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
			throw new ACConditionEvaluatorNotAvailableException(e.getMessage(), e);
		}
	}
	
}
