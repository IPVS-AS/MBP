package de.ipvs.as.mbp.domain.discovery.location.polygon;

import de.ipvs.as.mbp.domain.discovery.location.LocationTemplate;
import de.ipvs.as.mbp.domain.user_entity.MBPEntity;

import java.util.List;

/**
 * Objects of this class represent location templates for polygon areas.
 */
@MBPEntity(createValidator = PolygonLocationTemplateCreateValidator.class)
public class PolygonLocationTemplate extends LocationTemplate {
    private List<List<Double>> points;

    public PolygonLocationTemplate() {

    }

    public List<List<Double>> getPoints() {
        return points;
    }

    public PolygonLocationTemplate setPoints(List<List<Double>> points) {
        this.points = points;
        return this;
    }
}
