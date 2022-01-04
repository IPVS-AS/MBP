package de.ipvs.as.mbp.domain.discovery.topic;

import de.ipvs.as.mbp.error.EntityValidationException;
import de.ipvs.as.mbp.repository.discovery.RequestTopicRepository;
import de.ipvs.as.mbp.service.user.UserService;
import de.ipvs.as.mbp.service.validation.ICreateValidator;
import de.ipvs.as.mbp.util.Validation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Creation validator for {@link RequestTopic} entities.
 */
@Service
public class RequestTopicCreateValidator implements ICreateValidator<RequestTopic> {

    //Allowed value ranges
    private static final int MIN_TIMEOUT = 10;
    private static final int MAX_TIMEOUT = 60 * 1000;
    private static final int MIN_REPLIES = 1;
    private static final int MAX_REPLIES = 100;

    @Autowired
    private RequestTopicRepository topicRepository;

    @Autowired
    private UserService userService;

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
        } else if (!suffix.matches("[A-Za-z0-9]+")) {
            exception.addInvalidField("suffix", "The suffix must consist out of alphanumeric characters only.");
        }

        //Retrieve all request topics for current user
        boolean duplicate = topicRepository.findByOwner(userService.getLoggedInUser().getId()).stream()
                .map(RequestTopic::getSuffix)
                .anyMatch(suffix::equals);

        //Check result
        if (duplicate) {
            exception.addInvalidField("suffix", "A request topic with this suffix does already exist.");
        }

        //Validate timeout
        int timeout = requestTopic.getTimeout();
        if ((timeout < MIN_TIMEOUT) || (timeout > MAX_TIMEOUT)) {
            exception.addInvalidField("timeout", String.format("The timeout value must be in the range between %d and %d milliseconds.", MIN_TIMEOUT, MAX_TIMEOUT));
        }

        //Validate expected number of replies
        int replies = requestTopic.getExpectedReplies();
        if ((replies < MIN_REPLIES) || (replies > MAX_REPLIES)) {
            exception.addInvalidField("expectedReplies", String.format("The expected number of replies must be in the range between %d and %d.", MIN_REPLIES, MAX_REPLIES));
        }

        //Throw exception if there are invalid fields
        if (exception.hasInvalidFields()) {
            throw exception;
        }
    }
}
