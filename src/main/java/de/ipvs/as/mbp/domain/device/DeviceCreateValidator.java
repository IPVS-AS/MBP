package de.ipvs.as.mbp.domain.device;

import de.ipvs.as.mbp.error.EntityValidationException;
import de.ipvs.as.mbp.repository.DeviceRepository;
import de.ipvs.as.mbp.service.validation.ICreateValidator;
import de.ipvs.as.mbp.util.Validation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * Creation validator for device entities.
 */
@Service
public class DeviceCreateValidator implements ICreateValidator<Device> {

    @Autowired
    private DeviceRepository deviceRepository;


    /**
     * Validates a given entity that is supposed to be created and throws an exception with explanations
     * in case fields are invalid.
     *
     * @param entity The entity to validate on creation
     */
    @Override
    public void validateCreatable(Device entity) {
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

        //Check component type
        if (Validation.isNullOrEmpty(entity.getComponentType())) {
            exception.addInvalidField("componentType", "The component type must not be empty.");
        }

        //Check IP address
        String ipAddress = entity.getIpAddress();
        if (Validation.isNullOrEmpty(ipAddress)) {
            exception.addInvalidField("ipAddress", "The IP address must not be empty.");
        } else if (!Validation.isValidIPAddress(ipAddress)) {
            exception.addInvalidField("ipAddress", "The IP address is invalid.");
        }

        int port = entity.getPort();
        if(port < 1 || port > 65535) {
            exception.addInvalidField("port", "Invalid port. The port must be in between 1 and 65535");
        }

        //Check user name
        if (Validation.isNullOrEmpty(entity.getUsername())) {
            exception.addInvalidField("username", "The user name must not be empty.");
        }

        //Check whether key or password have been provided
        if ((!entity.hasRSAKey()) && ((!entity.hasPassword()) || Validation.isNullOrEmpty(entity.getPassword()))) {
            exception.addInvalidField("password", "Either a RSA key or a password must be provided.");
        }

        //Throw exception if there are invalid fields
        if (exception.hasInvalidFields()) {
            throw exception;
        }
    }
}
