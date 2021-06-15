package de.ipvs.as.mbp.domain.discovery.location.point;

import de.ipvs.as.mbp.error.EntityValidationException;
import de.ipvs.as.mbp.service.validation.ICreateValidator;
import de.ipvs.as.mbp.util.Validation;
import org.springframework.stereotype.Service;

/**
 * Creation validator for {@link PointLocationTemplate} entities.
 */
@Service
public class PointLocationTemplateCreateValidator implements ICreateValidator<PointLocationTemplate> {

    /**
     * Validates a given entity that is supposed to be created and throws an exception with explanations
     * in case fields are invalid.
     *
     * @param pointLocationTemplate The entity to validate on creation
     */
    @Override
    public void validateCreatable(PointLocationTemplate pointLocationTemplate) {
        //Sanity check
        if (pointLocationTemplate == null) {
            throw new EntityValidationException("The location template is invalid.");
        }

        //Create exception to collect invalid fields
        EntityValidationException exception = new EntityValidationException("Could not create location template, because some fields are invalid.");

        //Check name
        if (Validation.isNullOrEmpty(pointLocationTemplate.getName())) {
            exception.addInvalidField("name", "The name must not be empty.");
        }

        //Check latitude
        if ((pointLocationTemplate.getLatitude() > 90) || (pointLocationTemplate.getLatitude() < -90) || (pointLocationTemplate.getLatitude() == 0.0)) {
            exception.addInvalidField("latitude", "The latitude is invalid.");
        }

        //Check longitude
        if ((pointLocationTemplate.getLongitude() > 180) || (pointLocationTemplate.getLongitude() < -180) || (pointLocationTemplate.getLongitude() == 0.0)) {
            exception.addInvalidField("longitude", "The longitude is invalid.");
        }

        //Throw exception if there are invalid fields
        if (exception.hasInvalidFields()) {
            throw exception;
        }
    }
}
