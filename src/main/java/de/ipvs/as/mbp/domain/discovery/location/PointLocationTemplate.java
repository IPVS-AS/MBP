package de.ipvs.as.mbp.domain.discovery.location;

import org.springframework.data.mongodb.core.mapping.Document;

public class PointLocationTemplate extends LocationTemplate {
    private double latitude;
    private double longitude;

    public PointLocationTemplate() {

    }

    public double getLatitude() {
        return latitude;
    }

    public PointLocationTemplate setLatitude(double latitude) {
        this.latitude = latitude;
        return this;
    }

    public double getLongitude() {
        return longitude;
    }

    public PointLocationTemplate setLongitude(double longitude) {
        this.longitude = longitude;
        return this;
    }
}
