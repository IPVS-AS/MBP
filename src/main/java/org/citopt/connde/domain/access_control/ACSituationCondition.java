package org.citopt.connde.domain.access_control;

import javax.persistence.GeneratedValue;
import javax.validation.constraints.NotEmpty;

import org.citopt.connde.service.cep.engine.core.queries.CEPQuery;
import org.springframework.data.annotation.Id;

/**
 * A simple condition that can be used to compare two arguments.
 * 
 * @author Jakob Benz
 */
public class ACSituationCondition implements IACCondition {
	
	/**
	 * The id of this policy.
	 */
	@Id
    @GeneratedValue
	private String id;
	
	/**
	 * The name of this condition.
	 */
	@NotEmpty
	private String name;
	
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
	 * @param the name of this condition.
	 * @param query the {@link CEPQuery query} used to evaluate this condition.
	 */
	public ACSituationCondition(String name, CEPQuery query) {
		this.name = name;
		this.query = query;
	}
	
	// - - -
	
	@Override
	public String getId() {
		return id;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	public ACSituationCondition setName(String name) {
		this.name = name;
		return this;
	}

	public CEPQuery getQuery() {
		return query;
	}

	public ACSituationCondition setQuery(CEPQuery query) {
		this.query = query;
		return this;
	}
	
}
