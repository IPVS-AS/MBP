package de.ipvs.as.mbp.domain.rules;

/**
 * Enumeration of possible results of executing a rule.
 * <p>
 * NONE: Rule has not been executed so far
 * FAILURE: Execution failed
 * SUCCESS: Execution was successful
 */
public enum RuleExecutionResult {
    NONE, FAILURE, SUCCESS
}