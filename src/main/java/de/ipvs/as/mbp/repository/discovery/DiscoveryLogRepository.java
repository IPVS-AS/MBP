package de.ipvs.as.mbp.repository.discovery;

import de.ipvs.as.mbp.domain.discovery.deployment.DynamicDeployment;
import de.ipvs.as.mbp.domain.discovery.deployment.log.DiscoveryLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for storing and managing {@link DiscoveryLog}s.
 */
@Repository
public interface DiscoveryLogRepository extends MongoRepository<DiscoveryLog, String> {
    /**
     * Returns the {@link DiscoveryLog} that is associated with a certain {@link DynamicDeployment}, given
     * by its ID.
     *
     * @param dynamicDeploymentId The ID of the {@link DynamicDeployment} to retrieve the {@link DiscoveryLog} for
     * @return The {@link DiscoveryLog}
     */
    Optional<DiscoveryLog> findByDynamicDeploymentId(String dynamicDeploymentId);

    /**
     * Checks and returns whether a {@link DiscoveryLog} exists for a certain {@link DynamicDeployment},
     * given by its ID.
     *
     * @param dynamicDeploymentId The ID of the {@link DynamicDeployment} to check
     * @return True, if a {@link DiscoveryLog} exists for this dynamic deployment; false otherwise
     */
    boolean existsByDynamicDeploymentId(String dynamicDeploymentId);
}
