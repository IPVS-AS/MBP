package de.ipvs.as.mbp.repository.discovery;

import de.ipvs.as.mbp.domain.discovery.device.DeviceTemplate;
import de.ipvs.as.mbp.domain.discovery.peripheral.DynamicPeripheral;
import de.ipvs.as.mbp.repository.UserEntityRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for storing and managing {@link DynamicPeripheral}s.
 */
@Repository
public interface DynamicPeripheralRepository extends UserEntityRepository<DynamicPeripheral> {
    /**
     * Retrieves all {@link DynamicPeripheral}s from the repository that refer to a {@link DeviceTemplate}
     * of a certain ID.
     *
     * @param deviceTemplateId The ID of the device template
     * @return The resulting list of matching {@link DynamicPeripheral}s
     */
    List<DynamicPeripheral> findByDeviceTemplate_Id(String deviceTemplateId);

    /**
     * Retrieves all {@link DynamicPeripheral}s from the repository that refer to a {@link DeviceTemplate}
     * of a certain ID and have a certain active intention.
     *
     * @param deviceTemplateId The ID of the device template
     * @param activeIntended   The active intention (true for activate, false for deactivate)
     * @return The resulting list of matching {@link DynamicPeripheral}s
     */
    List<DynamicPeripheral> findByDeviceTemplate_IdAndActiveIntended(String deviceTemplateId, boolean activeIntended);
}
