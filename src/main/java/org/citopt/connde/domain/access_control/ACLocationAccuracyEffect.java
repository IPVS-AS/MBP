package org.citopt.connde.domain.access_control;

import java.util.Map;

import org.citopt.connde.domain.user.User;
import org.citopt.connde.domain.valueLog.ValueLog;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.hateoas.server.core.Relation;

/**
 * This {@link IACModifyingEffect effect} manipulates the accuracy of a {@link Double} value.
 * 
 * @author Jakob Benz
 */
@Document
@ACEffect(type = ACEffectType.LOCATION_ACCURACY_MODIFICATION)
@Relation(collectionRelation = "policy-effects", itemRelation = "policy-effects")
public class ACLocationAccuracyEffect extends ACAbstractEffect {
	
	public static final String PARAM_KEY_ACCURACY = "accuracy";
	
	// - - -
	
	/**
	 * No-args constructor.
	 */
	public ACLocationAccuracyEffect() {
		super();
	}
	
	/**
	 * All-args constructor.
	 * 
	 * @param name the name of this effect.
	 * @param description the description of this effect.
	 * @param parameters the list of parameters this required to be applied.
	 * @param ownerId the id of the {@link User} that owns this policy.
	 */
	public ACLocationAccuracyEffect(String name, String description, Map<String, String> parameters, String ownerId) {
		super(name, description, parameters, ownerId);
	}
	
	// - - -
	
	@Override
	public ValueLog apply(ValueLog valueLog) {
		throw new UnsupportedOperationException("Location value logs are currently not supported!");
	}
	
	// - - -

	private double getAccuracy() {
		return Double.parseDouble(getParameters().get(PARAM_KEY_ACCURACY));
	}

}
