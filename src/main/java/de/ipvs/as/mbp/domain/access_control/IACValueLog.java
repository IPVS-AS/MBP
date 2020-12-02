package de.ipvs.as.mbp.domain.access_control;

/**
 * Marker-interface implemented by all value log classes. 
 * 
 * @author Jakob Benz
 */
public interface IACValueLog<T> {
	
	/**
	 * Returns the value of this value log.
	 * 
	 * @return the value of this value log.
	 */
	public T getValue();
	
}
