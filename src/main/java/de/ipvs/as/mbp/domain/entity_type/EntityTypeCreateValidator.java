package de.ipvs.as.mbp.domain.entity_type;

import de.ipvs.as.mbp.error.EntityValidationException;
import de.ipvs.as.mbp.repository.ActuatorTypeRepository;
import de.ipvs.as.mbp.repository.DeviceTypeRepository;
import de.ipvs.as.mbp.repository.SensorTypeRepository;
import de.ipvs.as.mbp.service.validation.ICreateValidator;
import de.ipvs.as.mbp.util.Validation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;

/**
 * Creation validator for entity type entities.
 */
@Service
public class EntityTypeCreateValidator implements ICreateValidator<EntityType> {

    private static final long MIN_ALLOWED_ICON_SIZE = 20; //Bytes
    private static final long MAX_ALLOWED_ICON_SIZE = 5000000; //Bytes
    private static final int MIN_WIDTH = 10;
    private static final int MAX_WIDTH = 300;
    private static final int MIN_HEIGHT = 10;
    private static final int MAX_HEIGHT = 300;

    @Autowired
    private DeviceTypeRepository deviceTypeRepository;

    @Autowired
    private ActuatorTypeRepository actuatorTypeRepository;

    @Autowired
    private SensorTypeRepository sensorTypeRepository;

    /**
     * Validates a given entity that is supposed to be created and throws an exception with explanations
     * in case fields are invalid.
     *
     * @param entity The entity to validate on creation
     */
    @Override
    public void validateCreatable(EntityType entity) {
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

        // Check if name exists
        switch (entity.getEntityClass().getSimpleName()) {
            case "Device":
                if (deviceTypeRepository.findOneByName(entity.getName()).isPresent()) {
                    exception.addInvalidField("name", "The name already exists.");
                }
                break;
            case "Actuator":
                if (actuatorTypeRepository.findOneByName(entity.getName()).isPresent()) {
                    exception.addInvalidField("name", "The name already exists.");
                }
                break;
            case "Sensor":
                if (sensorTypeRepository.findOneByName(entity.getName()).isPresent()) {
                    exception.addInvalidField("name", "The name already exists.");
                }
                break;
        }

        //Get icon
        EntityTypeIcon icon = entity.getIcon();

        //Check if available
        if (icon == null) {
            exception.addInvalidField("icon", "An icon must be provided.");
            throw exception;
        }

        //Check icon size
        long size = icon.getSize();
        if ((size < MIN_ALLOWED_ICON_SIZE) || (size > MAX_ALLOWED_ICON_SIZE)) {
            exception.addInvalidField("icon", "The file is of an invalid size.");
            throw exception;
        }

        //Get image object from icon
        BufferedImage iconImage = icon.toImageObject();

        //Check for null
        if (iconImage == null) {
            exception.addInvalidField("icon", "Invalid file type provided.");
            throw exception;
        }

        //Check icon dimensions (min, max and square)
        if ((iconImage.getWidth() < MIN_WIDTH) ||
                (iconImage.getWidth() > MAX_WIDTH) ||
                (iconImage.getHeight() < MIN_HEIGHT) ||
                (iconImage.getHeight() > MAX_HEIGHT) ||
                (iconImage.getWidth() != iconImage.getHeight())) {

            exception.addInvalidField("icon", "The image must be a square and between " + MIN_WIDTH +
                    "x" + MIN_HEIGHT + " and " + MAX_WIDTH + "x" + MAX_HEIGHT + " pixels in size.");
        }

        //Throw exception if there are invalid fields
        if (exception.hasInvalidFields()) {
            throw exception;
        }
    }
}
