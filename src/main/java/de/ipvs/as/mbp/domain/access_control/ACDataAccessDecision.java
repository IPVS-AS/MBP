package de.ipvs.as.mbp.domain.access_control;

/**
 * An extension of a basic access decision that may include an effect
 * to apply to the data before presenting it to the requesting entity.
 * 
 * @author Jakob Benz
 */
public class ACDataAccessDecision<T> extends ACAccessDecision {
	
	/**
	 * The {@link ACAbstractEffect effect} to apply to the data. Can be {@code null}.
	 */
	private ACAbstractEffect effect;
	
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
	 * @param effect the {@link ACAbstractEffect effect} to apply to the data. Can be {@code null}.
	 */
	public ACDataAccessDecision(ACAccessDecisionResult result, ACAbstractEffect effect) {
		super(result);
		this.effect = effect;
	}
	
	// - - -

	public ACAbstractEffect getEffect() {
		return effect;
	}

	public ACDataAccessDecision<T> setEffect(ACAbstractEffect effect) {
		this.effect = effect;
		return this;
	}
	
}
