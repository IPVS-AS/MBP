package de.ipvs.as.mbp.domain.discovery.description;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Objects of this class represent geographic points, consisting out of a latitude and a longitude value.
 */
public class DeviceDescriptionGeoPoint {
    //Latitude and longitude
    @JsonProperty("lat")
    private double latitude;

    @JsonProperty("lon")
    private double longitude;

    public DeviceDescriptionGeoPoint() {

    }

    /**
     * Creates a new geographic point from given latitude and longitude values.
     *
     * @param latitude  The latitude value to use
     * @param longitude The longitude value to use
     */
    @JsonCreator
    public DeviceDescriptionGeoPoint(@JsonProperty("lat") double latitude, @JsonProperty("lon") double longitude) {
        //Set fields
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Returns the latitude of the geographic point.
     *
     * @return The latitude
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Sets the latitude of the geographic point.
     *
     * @param latitude The latitude to set
     * @return The geographic point
     */
    public DeviceDescriptionGeoPoint setLatitude(double latitude) {
        this.latitude = latitude;
        return this;
    }

    /**
     * Returns the longitude of the geographic point.
     *
     * @return The longitude
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Sets the longitude of the geographic point.
     *
     * @param longitude The longitude to set
     * @return The geographic point
     */
    public DeviceDescriptionGeoPoint setLongitude(double longitude) {
        this.longitude = longitude;
        return this;
    }
}
