package org.citopt.connde.domain.access_control;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.citopt.connde.service.access_control.ACAbstractConditionEvaluator;

/**
 * Annotation used for {@link ACAbstractCondition conditions} to indicate
 * the which evaluator to used for evaluation.
 * 
 * @author Jakob Benz
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ACEvaluate {

	/**
	 * Condition evaluator class to be used for evaluating associated condition(s).
	 */
	@SuppressWarnings("rawtypes") // to work around JDK8 bug regarding Class-valued annotation properties
	public Class<? extends ACAbstractConditionEvaluator> using();

}
