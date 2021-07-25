package de.ipvs.as.mbp.repository.discovery;

import de.ipvs.as.mbp.domain.discovery.deployment.DynamicDeployment;
import de.ipvs.as.mbp.domain.discovery.deployment.log.DynamicDeploymentLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for storing and managing {@link DynamicDeploymentLog}s.
 */
@Repository
public interface DynamicDeploymentLogRepository extends MongoRepository<DynamicDeploymentLog, String> {
    /**
     * Returns the {@link DynamicDeploymentLog} that is associated with a certain {@link DynamicDeployment}, given
     * by its ID.
     *
     * @param dynamicDeploymentId The ID of the dynamic deployment to retrieve the log for
     * @return The dynamic deployment log
     */
    Optional<DynamicDeploymentLog> findByDynamicDeploymentId(String dynamicDeploymentId);

    /**
     * Checks and returns whether a {@link DynamicDeploymentLog} does exist for a certain {@link DynamicDeployment},
     * given by its ID.
     *
     * @param dynamicDeploymentId The ID of the dynamic deployment to check
     * @return True, if a log does exist for this dynamic deployment; false otherwise
     */
    boolean existsByDynamicDeploymentId(String dynamicDeploymentId);
}
