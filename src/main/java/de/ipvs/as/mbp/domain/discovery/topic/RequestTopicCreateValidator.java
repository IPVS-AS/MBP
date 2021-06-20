package de.ipvs.as.mbp.domain.discovery.topic;

import de.ipvs.as.mbp.domain.discovery.topic.condition.CompletenessCondition;
import de.ipvs.as.mbp.error.EntityValidationException;
import de.ipvs.as.mbp.service.validation.ICreateValidator;
import de.ipvs.as.mbp.util.Validation;
import org.springframework.stereotype.Service;

/**
 * Creation validator for {@link RequestTopic} entities.
 */
@Service
public class RequestTopicCreateValidator implements ICreateValidator<RequestTopic> {
    /**
     * Validates a given entity that is supposed to be created and throws an exception with explanations
     * in case fields are invalid.
     *
     * @param requestTopic The entity to validate on creation
     */
    @Override
    public void validateCreatable(RequestTopic requestTopic) {
        //Sanity check
        if (requestTopic == null) {
            throw new EntityValidationException("The request topic is invalid.");
        }

        //Create exception to collect invalid fields
        EntityValidationException exception = new EntityValidationException("Could not create request topic, because some fields are invalid.");

        //Validate suffix
        String suffix = requestTopic.getSuffix();
        if (Validation.isNullOrEmpty(suffix)) {
            exception.addInvalidField("suffix", "The suffix must not be empty.");
        } else if (suffix.length() < 2) {
            exception.addInvalidField("suffix", "The suffix must consist out of at least two characters.");
        } else if (suffix.matches("[A-Za-z0-9]+")) {
            exception.addInvalidField("suffix", "The suffix must consist out of alphanumeric characters only.");
        }

        //Validate completeness condition
        CompletenessCondition condition = requestTopic.getCompletenessCondition();
        if (condition == null) {
            exception.addInvalidField("completenessCondition", "A completeness condition must be provided.");
        } else {
            //Ask condition to check its validity
            condition.validate(exception);
        }

        //Throw exception if there are invalid fields
        if (exception.hasInvalidFields()) {
            throw exception;
        }
    }
}
