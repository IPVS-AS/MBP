package org.citopt.connde.domain.access_control;

import javax.annotation.Nonnull;

/**
 * Represents the result of the access control decision made for
 * an access request based on the policies for the requested access.
 * 
 * @author Jakob Benz
 */
public class ACAccessDecision {
	
	/**
	 * The {@link ACAccessDecisionResult result} of the decision.
	 */
	@Nonnull
	private final ACAccessDecisionResult result;
	
	// - - -
	
	/**
	 * All-args constructor.
	 * 
	 * @param result the {@link ACAccessDecisionResult result} of the decision.
	 */
	public ACAccessDecision(ACAccessDecisionResult result) {
		this.result = result;
	}
	
	// - - -
	
	public ACAccessDecisionResult getResult() {
		return result;
	}

}
