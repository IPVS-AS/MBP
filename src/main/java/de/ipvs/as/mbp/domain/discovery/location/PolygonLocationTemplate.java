package de.ipvs.as.mbp.domain.discovery.location;

import de.ipvs.as.mbp.domain.user_entity.MBPEntity;
import org.springframework.data.mongodb.core.mapping.Document;

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
