package de.ipvs.as.mbp.domain.access_control;

/**
 * Simple wrapper for a location value identified by a latitude and longitude value.
 * 
 * @author Jakob Benz
 */
public class Location {
	
	/**
	 * The latitude value.
	 */
	private double latitude;
	
	/**
	 * The longitude value. 
	 */
	private double longitude;
	
	// - - -
	
	/**
	 * No-args constructor.
	 */
	public Location() {}
	
	/**
	 * All-args constructor.
	 * 
	 * @param latitude
	 * @param longitude
	 */
	public Location(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	// - - -
	
	public double getLatitude() {
		return latitude;
	}
	
	public Location setLatitude(double latitude) {
		this.latitude = latitude;
		return this;
	}
	
	public double getLongitude() {
		return longitude;
	}
	
	public Location setLongitude(double longitude) {
		this.longitude = longitude;
		return this;
	}

}
