package de.ipvs.as.mbp.repository.discovery;

import de.ipvs.as.mbp.domain.discovery.collections.CandidateDevicesResult;
import de.ipvs.as.mbp.domain.discovery.device.DeviceTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for storing and managing {@link DeviceTemplate}s.
 */
@Repository
public interface CandidateDevicesRepository extends MongoRepository<CandidateDevicesResult, String> {

}
