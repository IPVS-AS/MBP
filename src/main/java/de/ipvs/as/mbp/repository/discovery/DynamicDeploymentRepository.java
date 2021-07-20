package de.ipvs.as.mbp.repository.discovery;

import de.ipvs.as.mbp.domain.discovery.device.DeviceTemplate;
import de.ipvs.as.mbp.domain.discovery.deployment.DynamicDeployment;
import de.ipvs.as.mbp.repository.UserEntityRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for storing and managing {@link DynamicDeployment}s.
 */
@Repository
public interface DynamicDeploymentRepository extends UserEntityRepository<DynamicDeployment> {
    /**
     * Retrieves all {@link DynamicDeployment}s from the repository that refer to a {@link DeviceTemplate}
     * of a certain ID.
     *
     * @param deviceTemplateId The ID of the device template
     * @return The resulting list of matching {@link DynamicDeployment}s
     */
    List<DynamicDeployment> findByDeviceTemplate_Id(String deviceTemplateId);

    /**
     * Retrieves all {@link DynamicDeployment}s from the repository that refer to a {@link DeviceTemplate}
     * of a certain ID and have a certain active intention.
     *
     * @param deviceTemplateId The ID of the device template
     * @param activeIntended   The active intention (true for activate, false for deactivate)
     * @return The resulting list of matching {@link DynamicDeployment}s
     */
    List<DynamicDeployment> findByDeviceTemplate_IdAndActivatingIntended(String deviceTemplateId, boolean activeIntended);
}
