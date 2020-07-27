package org.citopt.connde.domain.access_control;

/**
 * An entity within the MBP access-control framework. Either a
 * {@link IACRequestingEntity} or a {@link IACRequestedEntity}.
 * 
 * @author Jakob Benz
 */
public interface IACEntity {
	
	/**
	 * Return the id of this entity.
	 * 
	 * @return the id of this entity as {@code String}.
	 */
	public String getId();
	
	/**
	 * Return the type of this entity.
	 * 
	 * @return the {@link ACEntityType} of this entity.
	 */
	public ACEntityType getEntityType();

//	/**
//	 * Returns the contextual information which is directly available with this entity.
//	 * 
//	 * @return the contextual information as an {@link ACAttribute} list.
//	 */
//	public List<ACAttribute<? extends Comparable<?>>> getContext();
	
}
