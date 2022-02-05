package de.ipvs.as.mbp.repository.discovery;

import de.ipvs.as.mbp.domain.discovery.deployment.DynamicDeployment;
import de.ipvs.as.mbp.domain.discovery.deployment.log.DiscoveryLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for storing and managing {@link DiscoveryLog}s that belong to {@link DynamicDeployment}s.
 */
@Repository
public interface DiscoveryLogRepository extends MongoRepository<DiscoveryLog, String> {
    /**
     * Retrieves all {@link DiscoveryLog}s that are available for a certain {@link DynamicDeployment}, given by its ID.
     *
     * @param dynamicDeploymentId The ID of the {@link DynamicDeployment} to retrieve the {@link DiscoveryLog}s for
     * @return The retrieved list of {@link DiscoveryLog}s
     */
    List<DiscoveryLog> findAllByDynamicDeploymentId(String dynamicDeploymentId);

    /**
     * Retrieves a {@link Page} of {@link DiscoveryLog}s that are available for a certain {@link DynamicDeployment},
     * given by its ID, by applying the {@link Page} configuration of a given {@link Pageable}.
     *
     * @param dynamicDeploymentId The ID of the {@link DynamicDeployment} to retrieve the {@link DiscoveryLog}s for
     * @param pageable            The {@link Page} configuration to use
     * @return The retrieved {@link Page} of {@link DiscoveryLog}s
     */
    Page<DiscoveryLog> findByDynamicDeploymentId(String dynamicDeploymentId, Pageable pageable);

    /**
     * Deletes all {@link DiscoveryLog}s that are available for a certain {@link DynamicDeployment}, given by its ID.
     *
     * @param dynamicDeploymentId The ID of the {@link DynamicDeployment} for which all {@link DiscoveryLog}s are
     *                            supposed to be deleted
     */
    void deleteByDynamicDeploymentId(String dynamicDeploymentId);
}
