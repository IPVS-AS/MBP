package de.ipvs.as.mbp.domain.discovery.location.point;

import de.ipvs.as.mbp.domain.discovery.location.LocationTemplate;
import de.ipvs.as.mbp.domain.user_entity.MBPEntity;

/**
 * Objects of this class represent location templates for location points.
 */
@MBPEntity(createValidator = PointLocationTemplateCreateValidator.class)
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
