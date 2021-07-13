package de.ipvs.as.mbp.domain.discovery.device.location.circle;

import de.ipvs.as.mbp.error.EntityValidationException;
import de.ipvs.as.mbp.service.validation.ICreateValidator;
import de.ipvs.as.mbp.util.Validation;
import org.springframework.stereotype.Service;

/**
 * Creation validator for {@link CircleLocationTemplate} entities.
 */
@Service
public class CircleLocationTemplateCreateValidator implements ICreateValidator<CircleLocationTemplate> {

    /**
     * Validates a given entity that is supposed to be created and throws an exception with explanations
     * in case fields are invalid.
     *
     * @param circleLocationTemplate The entity to validate on creation
     */
    @Override
    public void validateCreatable(CircleLocationTemplate circleLocationTemplate) {
        //Sanity check
        if (circleLocationTemplate == null) {
            throw new EntityValidationException("The location template is invalid.");
        }

        //Create exception to collect invalid fields
        EntityValidationException exception = new EntityValidationException("Could not create location template, because some fields are invalid.");

        //Check name
        if (Validation.isNullOrEmpty(circleLocationTemplate.getName())) {
            exception.addInvalidField("name", "The name must not be empty.");
        }

        //Check latitude
        if ((circleLocationTemplate.getLatitude() > 90) || (circleLocationTemplate.getLatitude() < -90) || (circleLocationTemplate.getLatitude() == 0.0)) {
            exception.addInvalidField("latitude", "The latitude is invalid.");
        }

        //Check longitude
        if ((circleLocationTemplate.getLongitude() > 180) || (circleLocationTemplate.getLongitude() < -180) || (circleLocationTemplate.getLongitude() == 0.0)) {
            exception.addInvalidField("longitude", "The longitude is invalid.");
        }

        //Check radius
        if (circleLocationTemplate.getRadius() < 1) {
            exception.addInvalidField("radius", "The radius is invalid.");
        }

        //Throw exception if there are invalid fields
        if (exception.hasInvalidFields()) {
            throw exception;
        }
    }
}
