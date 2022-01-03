package de.ipvs.as.mbp.util;

/**
 * Collection of geospatial utility methods, e.g. for calculating distances between geographic points.
 */
public class GeoUtils {

    //Radius of the earth in kilometers
    private static final double EARTH_RADIUS_KM = 6371;

    /**
     * Calculates the distance (in meters) between two geographic points that are given as longitude and latitude
     * values.
     *
     * @param lat1 The latitude of the first point
     * @param lon1 The longitude of the first point
     * @param lat2 The latitude of the second point
     * @param lon2 The longitude of the second point
     * @return The resulting distance in meters
     */
    public static double getGeoDistance(double lat1, double lon1, double lat2, double lon2) {
        //Delegate call
        return getGeoDistance(lat1, lon1, 0, lat2, lon2, 0);
    }

    /**
     * Calculates the distance (in meters) between two geographic points that are given as longitude, latitude
     * and altitude (height) values.
     * <p>
     * Taken from https://stackoverflow.com/a/16794680/
     *
     * @param lat1 The latitude of the first point
     * @param lon1 The longitude of the first point
     * @param el1  The altitude of the first point
     * @param lat2 The latitude of the second point
     * @param lon2 The longitude of the second point
     * @param el2  The altitude of the second point
     * @return The resulting distance in meters
     */
    public static double getGeoDistance(double lat1, double lon1, double el1, double lat2, double lon2, double el2) {
        //Calculate latitude/longitude distances and height
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double height = el1 - el2;

        //Put the latitude/longitude distances into relation
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        //Transform to final distance in meters
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = EARTH_RADIUS_KM * c * 1000;

        //Include height
        return Math.sqrt(Math.pow(distance, 2) + Math.pow(height, 2));
    }
}
