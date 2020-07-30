package org.citopt.connde.domain.access_control;

import org.citopt.connde.service.cep.engine.core.queries.CEPQuery;

/**
 * A simple condition that can be used to compare two arguments.
 * 
 * @author Jakob Benz
 */
public class ACSituationCondition implements IACCondition {
	
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
	 * @param query the {@link CEPQuery query} used to evaluate this condition.
	 */
	public ACSituationCondition(CEPQuery query) {
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
	
}
