package de.ipvs.as.mbp.domain.discovery.topic;

import de.ipvs.as.mbp.error.MBPException;
import de.ipvs.as.mbp.repository.discovery.DeviceTemplateRepository;
import de.ipvs.as.mbp.service.validation.IDeleteValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Delete validator for {@link RequestTopic}s.
 */
@Component
public class RequestTopicDeleteValidator implements IDeleteValidator<RequestTopic> {

    @Autowired
    private DeviceTemplateRepository deviceTemplateRepository;

    /**
     * Indicates whether an entity can be deleted, i.e., whether all
     * preconditions required for the delete operation are fulfilled.
     * If the entity cannot be deleted, an appropriate exception is thrown.
     *
     * @param requestTopic The {@link RequestTopic} to delete.
     */
    @Override
    public void validateDeletable(RequestTopic requestTopic) {
        //Null check
        if (requestTopic == null) return;

        //Check if there are device templates owned by the request topic owner
        if (!deviceTemplateRepository.findByOwner(requestTopic.getOwner().getId()).isEmpty()) {
            throw new MBPException(HttpStatus.CONFLICT, "The request topic is still used by at least one device template.");
        }
    }
}
