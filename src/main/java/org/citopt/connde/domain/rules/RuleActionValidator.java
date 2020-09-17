package org.citopt.connde.domain.rules;

import org.citopt.connde.repository.RuleActionRepository;
import org.citopt.connde.service.rules.execution.RuleActionExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.util.HashMap;
import java.util.Optional;

/**
 * Validator for rule action objects.
 */
@org.springframework.stereotype.Component
public class RuleActionValidator implements Validator {

	// Repository for rule actions
	private static RuleActionRepository ruleActionRepository;

	/**
	 * Sets the rule action repository (autowired).
	 *
	 * @param ruleActionRepository The rule action repository to set
	 */
	@Autowired
	public void setRuleActionRepository(RuleActionRepository ruleActionRepository) {
		RuleActionValidator.ruleActionRepository = ruleActionRepository;
	}

	/**
	 * Checks whether the validator can be applied to objects of a given class.
	 * However, this validator can only be applied to rule action objects.
	 *
	 * @param type The class to check
	 * @return True, if the validator can be applied to objects of this class; false
	 *         otherwise
	 */
	@Override
	public boolean supports(Class<?> type) {
		return RuleAction.class.equals(type);
	}

	/**
	 * Validates the fields of a rule action object and adds error messages to the
	 * errors object accordingly.
	 *
	 * @param o      The object to validate
	 * @param errors The errors object to add the error messages to
	 */
	@Override
	public void validate(Object o, Errors errors) {
		// Cast to rule action
		RuleAction ruleAction = (RuleAction) o;

		// Check if name was provided (mandatory)
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "ruleAction.name.empty",
				"The name must not be empty.");

		// Check if name is unique
		Optional<RuleAction> anotherRuleAction = ruleActionRepository.findByName(ruleAction.getName());
		if (anotherRuleAction.isPresent()) {
			errors.rejectValue("name", "ruleAction.name.duplicate", "The name is already registered.");
		}

		// Check if type was provided
		RuleActionType actionType = ruleAction.getType();
		if (actionType == null) {
			errors.rejectValue("type", "ruleAction.type.empty", "A rule action type needs to be selected.");
			return;
		}

		// If null is provided instead of a map, replace it with an empty map
		if (ruleAction.getParameters() == null) {
			ruleAction.setParameters(new HashMap<>());
		}

		// Get executor for this action type
		RuleActionExecutor executor = actionType.getExecutor();

		// Use executor to validate the provided parameters
		executor.validateParameters(errors, ruleAction.getParameters());
	}
}
