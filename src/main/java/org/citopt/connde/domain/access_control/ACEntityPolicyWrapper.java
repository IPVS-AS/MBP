package org.citopt.connde.domain.access_control;

import org.citopt.connde.domain.user_entity.UserEntity;

/**
 * Used to wrap a {@link UserEntity} and the {@link ACPolicy} that grants the access to it.
 * 
 * @author Jakob Benz
 */
public class ACEntityPolicyWrapper<E extends UserEntity>  {
	
	private E entity;
	private ACPolicy policy;
	
	// - - -
	
	public ACEntityPolicyWrapper(E entity, ACPolicy policy) {
		this.entity = entity;
		this.policy = policy;
	}
	
	// - - -
	
	public E getEntity() {
		return entity;
	}
	
	public ACEntityPolicyWrapper<E> setEntity(E entity) {
		this.entity = entity;
		return this;
	}
	
	public ACPolicy getPolicy() {
		return policy;
	}
	
	public ACEntityPolicyWrapper<E> setPolicy(ACPolicy policy) {
		this.policy = policy;
		return this;
	}
	
}
