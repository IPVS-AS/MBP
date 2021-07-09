package de.ipvs.as.mbp.domain.discovery.device;

import de.ipvs.as.mbp.domain.discovery.device.requirements.DeviceRequirement;
import de.ipvs.as.mbp.domain.discovery.device.scoring.ScoringCriterion;
import de.ipvs.as.mbp.error.EntityValidationException;
import de.ipvs.as.mbp.service.validation.ICreateValidator;
import de.ipvs.as.mbp.util.Validation;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Creation validator for {@link DeviceTemplate} entities.
 */
@Service
public class DeviceTemplateCreateValidator implements ICreateValidator<DeviceTemplate> {
    /**
     * Validates a given entity that is supposed to be created and throws an exception with explanations
     * in case fields are invalid.
     *
     * @param deviceTemplate The entity to validate on creation
     */
    @Override
    public void validateCreatable(DeviceTemplate deviceTemplate) {
        //Sanity check
        if (deviceTemplate == null) {
            throw new EntityValidationException("The device template is invalid.");
        }

        //Create exception to collect invalid fields
        EntityValidationException exception = new EntityValidationException("Could not create device template, because some fields are invalid.");

        //Check name
        if (Validation.isNullOrEmpty(deviceTemplate.getName())) {
            exception.addInvalidField("name", "The name must not be empty.");
        }

        //Iterate over all requirements for validation
        List<DeviceRequirement> requirements = deviceTemplate.getRequirements();
        for (int i = 0; i < requirements.size(); i++) {
            //Ask current requirement to validate itself
            requirements.get(i).validate(exception, "requirements[" + i + "]");
        }

        //Iterate over all scoring criteria for validation
        List<ScoringCriterion> criteria = deviceTemplate.getScoringCriteria();
        for (int i = 0; i < criteria.size(); i++) {
            //Ask current scoring criterion to validate itself
            criteria.get(i).validate(exception, "scoringCriteria[" + i + "]");
        }

        //Throw exception if there are invalid fields
        if (exception.hasInvalidFields()) {
            throw exception;
        }
    }
}
