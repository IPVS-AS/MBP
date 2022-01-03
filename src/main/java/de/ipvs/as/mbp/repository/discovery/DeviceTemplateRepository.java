package de.ipvs.as.mbp.repository.discovery;

import de.ipvs.as.mbp.domain.discovery.device.DeviceTemplate;
import de.ipvs.as.mbp.repository.UserEntityRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for storing and managing {@link DeviceTemplate}s.
 */
@Repository
public interface DeviceTemplateRepository extends UserEntityRepository<DeviceTemplate> {

}
