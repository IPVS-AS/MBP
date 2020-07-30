package org.citopt.connde.domain.access_control;

/**
 * An extension of a basic access decision that may include an effect
 * to apply to the data before presenting it to the requesting entity.
 * 
 * @param <T> the data type of the requested data.
 * @author Jakob Benz
 */
public class ACDataAccessDecision<T> extends ACAccessDecision {
	
	/**
	 * The {@link IACEffect effect} to apply to the data. Can be {@code null}.
	 */
	private IACEffect<T> effect;
	
	// - - -
	
	/**
	 * No-args constructor.
	 */
	public ACDataAccessDecision() {
		super();
	}
	
	/**
	 * Required-args constructor.
	 * 
	 * @param result
	 */
	public ACDataAccessDecision(ACAccessDecisionResult result) {
		super(result);
	}

	/**
	 * Required-args constructor.
	 * 
	 * @param result the {@link ACAccessDecisionResult result} of the decision.
	 * @param effect the {@link IACEffect effect} to apply to the data. Can be {@code null}.
	 */
	public ACDataAccessDecision(ACAccessDecisionResult result, IACEffect<T> effect) {
		super(result);
		this.effect = effect;
	}
	
	// - - -

	public IACEffect<T> getEffect() {
		return effect;
	}

	public ACDataAccessDecision<T> setEffect(IACEffect<T> effect) {
		this.effect = effect;
		return this;
	}
	
}
