package de.ipvs.as.mbp.domain.access_control;

import de.ipvs.as.mbp.domain.user.User;
import de.ipvs.as.mbp.service.access_control.ACSituationConditionEvaluator;
import de.ipvs.as.mbp.service.cep.engine.core.queries.CEPQuery;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.hateoas.server.core.Relation;

/**
 * A simple condition that can be used to compare two arguments.
 * 
 * @author Jakob Benz
 */
@Document
@ACEvaluate(using = ACSituationConditionEvaluator.class)
@Relation(collectionRelation = "policy-conditions", itemRelation = "policy-conditions")
public class ACSituationCondition extends ACAbstractCondition {
	
	/**
	 * The {@link CEPQuery query} used to evaluate this condition.
	 */
	private CEPQuery query;
	
	// - - -
	
	/**
	 * No-args constructor.
	 */
	public ACSituationCondition() {}
	
	/**
	 * All-args constructor.
	 * 
	 * @param name the name of this condition.
	 * @param description the description of this condition.
	 * @param query the {@link CEPQuery query} used to evaluate this condition.
	 * @param ownerId the id of the {@link User} that owns this policy.
	 */
	public ACSituationCondition(String name, String description, CEPQuery query, String ownerId) {
		super(name, description, ownerId);
		this.query = query;
	}
	
	// - - -

	public CEPQuery getQuery() {
		return query;
	}

	public ACSituationCondition setQuery(CEPQuery query) {
		this.query = query;
		return this;
	}
	
	// - - -
	
	@Override
	public String toHumanReadableString() {
		return query.toString();
	}
	
}
