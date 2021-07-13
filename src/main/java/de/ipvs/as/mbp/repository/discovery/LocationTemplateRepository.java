package de.ipvs.as.mbp.repository.discovery;

import de.ipvs.as.mbp.domain.discovery.device.location.LocationTemplate;
import de.ipvs.as.mbp.repository.UserEntityRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for storing and managing {@link LocationTemplate}s.
 */
@Repository
public interface LocationTemplateRepository extends UserEntityRepository<LocationTemplate> {

}
