package de.ipvs.as.mbp.domain.discovery.device;

import de.ipvs.as.mbp.error.MBPException;
import de.ipvs.as.mbp.repository.discovery.DynamicDeploymentRepository;
import de.ipvs.as.mbp.service.validation.IDeleteValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * Delete validator for {@link DeviceTemplate}s.
 */
@Service
public class DeviceTemplateDeleteValidator implements IDeleteValidator<DeviceTemplate> {

    @Autowired
    private DynamicDeploymentRepository dynamicDeploymentRepository;

    /**
     * Indicates whether an entity can be deleted, i.e., whether all
     * preconditions required for the delete operation are fulfilled.
     * If the entity cannot be deleted, an appropriate exception is thrown.
     *
     * @param deviceTemplate The {@link DeviceTemplate} to delete.
     */
    @Override
    public void validateDeletable(DeviceTemplate deviceTemplate) {
        //Null check
        if (deviceTemplate == null) throw new MBPException(HttpStatus.NOT_FOUND, "The device template does not exist.");

        //Check whether the device template is still used by dynamic deployments
        if (!this.dynamicDeploymentRepository.findByDeviceTemplate_Id(deviceTemplate.getId()).isEmpty())
            throw new MBPException(HttpStatus.CONFLICT, "The device template is still used by dynamic deployments.");
    }

}
