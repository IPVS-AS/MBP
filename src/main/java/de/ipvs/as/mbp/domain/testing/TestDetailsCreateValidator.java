package de.ipvs.as.mbp.domain.testing;

import de.ipvs.as.mbp.error.EntityValidationException;
import de.ipvs.as.mbp.repository.TestDetailsRepository;
import de.ipvs.as.mbp.service.validation.ICreateValidator;
import de.ipvs.as.mbp.util.Validation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class TestDetailsCreateValidator implements ICreateValidator<TestDetails> {

    @Autowired
    private  TestDetailsRepository testDetailsRepository;



    /**
     * Validates a given entity that is supposed to be created and throws an exception with explanations
     * in case fields are invalid.
     *
     * @param entity The entity to validate on creation
     */
    public void validateCreatable(TestDetails entity) {

        //Sanity check
        if (entity == null) {
            throw new EntityValidationException("The entity is invalid.");
        }

        //Create exception to collect invalid fields
        EntityValidationException exception = new EntityValidationException("Could not create, because some fields are invalid.");

        //Check name
        if (Validation.isNullOrEmpty(entity.getName())) {
            exception.addInvalidField("name", "The name must not be empty.");
        }

        // Check if rule choice is empty
        if (entity.getSensor() ==  null || entity.getSensor().isEmpty()) {
            exception.addInvalidField("sensors", "At least one Sensor Simulator or Real Sensor must be selected!");
        }

        // Check if rule choice is empty
        if (entity.getRules() == null || entity.getRules().isEmpty()) {
            exception.addInvalidField("rules", "At least one rule must be selected!");
        }


        //Throw exception if there are invalid fields
        if (exception.hasInvalidFields()) {
            throw exception;
        }
    }
}
