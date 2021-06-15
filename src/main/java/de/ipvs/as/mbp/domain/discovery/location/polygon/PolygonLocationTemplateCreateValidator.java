package de.ipvs.as.mbp.domain.discovery.location.polygon;

import de.ipvs.as.mbp.error.EntityValidationException;
import de.ipvs.as.mbp.service.validation.ICreateValidator;
import de.ipvs.as.mbp.util.Validation;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Creation validator for {@link PolygonLocationTemplate} entities.
 */
@Service
public class PolygonLocationTemplateCreateValidator implements ICreateValidator<PolygonLocationTemplate> {

    /**
     * Validates a given entity that is supposed to be created and throws an exception with explanations
     * in case fields are invalid.
     *
     * @param polygonLocationTemplate The entity to validate on creation
     */
    @Override
    public void validateCreatable(PolygonLocationTemplate polygonLocationTemplate) {
        //Sanity check
        if (polygonLocationTemplate == null) {
            throw new EntityValidationException("The location template is invalid.");
        }

        //Create exception to collect invalid fields
        EntityValidationException exception = new EntityValidationException("Could not create location template, because some fields are invalid.");

        //Check name
        if (Validation.isNullOrEmpty(polygonLocationTemplate.getName())) {
            exception.addInvalidField("name", "The name must not be empty.");
        }

        //Retrieve polygon points
        List<List<Double>> polygonPoints = polygonLocationTemplate.getPoints();

        //Check points
        if ((polygonPoints == null) || (polygonPoints.size() < 3)) {
            exception.addInvalidField("pointsList", "At least three polygon points need to be provided.");
        } else {
            for (List<Double> point : polygonPoints) {
                if ((point == null) || (point.size() != 2) || (point.get(0) > 90) || (point.get(0) < -90) || (point.get(0) == 0.0) || (point.get(1) > 180) || (point.get(1) < -180) || (point.get(1) == 0.0)) {
                    exception.addInvalidField("pointsList", "All points need to consist out of one latitude and one longitude value.");
                    break;
                }
            }
        }


        //Throw exception if there are invalid fields
        if (exception.hasInvalidFields()) {
            throw exception;
        }
    }
}
