package de.ipvs.as.mbp.service.discovery.log;

import de.ipvs.as.mbp.domain.discovery.deployment.DynamicDeployment;
import de.ipvs.as.mbp.domain.discovery.deployment.log.DiscoveryLog;
import de.ipvs.as.mbp.repository.discovery.DiscoveryLogRepository;
import de.ipvs.as.mbp.repository.discovery.DynamicDeploymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * Offers services for reading and writing {@link DiscoveryLog}s that belong to {@link DynamicDeployment}s. It
 * takes care of synchronizing write requests for {@link DiscoveryLog}s, such that typical issues like lost
 * updates are avoided.
 */
@Service
public class DiscoveryLogService {

    @Autowired
    private DynamicDeploymentRepository dynamicDeploymentRepository;

    @Autowired
    private DiscoveryLogRepository discoveryLogRepository;

    /**
     * Creates the {@link DiscoveryLogService}.
     */
    public DiscoveryLogService() {

    }

    /**
     * Initializes the {@link DiscoveryLogService}.
     */
    @PostConstruct
    public void initialize() {

    }

    /**
     * Retrieves and returns all {@link DiscoveryLog}s that are available for a certain {@link DynamicDeployment},
     * given by its ID.
     *
     * @param dynamicDeploymentId The ID of the {@link DynamicDeployment} to retrieve the {@link DiscoveryLog}s for
     * @return The retrieved list of {@link DiscoveryLog}s
     */
    public List<DiscoveryLog> getDiscoveryLogs(String dynamicDeploymentId) {
        //Validate the dynamic deployment ID
        validateDynamicDeploymentID(dynamicDeploymentId);

        //Retrieve all available discovery logs for this dynamic deployment
        return discoveryLogRepository.findAllByDynamicDeploymentId(dynamicDeploymentId);
    }

    /**
     * Retrieves and returns a {@link Page} of {@link DiscoveryLog}s that are available for a certain
     * {@link DynamicDeployment}, given by its ID, by applying the {@link Page} configuration
     * of a given {@link Pageable}.
     *
     * @param dynamicDeploymentId The ID of the {@link DynamicDeployment} to retrieve the {@link DiscoveryLog}s for
     * @param pageable            The {@link Page} configuration to use
     * @return The retrieved {@link Page} of {@link DiscoveryLog}s
     */
    public Page<DiscoveryLog> getDiscoveryLogs(String dynamicDeploymentId, Pageable pageable) {
        //Validate the dynamic deployment ID
        validateDynamicDeploymentID(dynamicDeploymentId);

        //Retrieve all available discovery logs for this dynamic deployment and page configuration
        return discoveryLogRepository.findByDynamicDeploymentId(dynamicDeploymentId, pageable);
    }

    /**
     * Writes a given {@link DiscoveryLog} that belongs to a certain {@link DynamicDeployment}, given by its ID,
     * to the corresponding repository and stores it persistently.
     *
     * @param dynamicDeploymentID The ID of the {@link DynamicDeployment} to which the {@link DiscoveryLog} belongs
     * @param discoveryLog        The {@link DiscoveryLog} to write
     */
    public void writeDiscoveryLog(String dynamicDeploymentID, DiscoveryLog discoveryLog) {
        //Null check
        if (discoveryLog == null) throw new IllegalArgumentException("The discovery log must not be null.");

        //Validate the ID of the dynamic deployment
        validateDynamicDeploymentID(dynamicDeploymentID);

        //Reference the dynamic deployment within the discovery log
        discoveryLog.setDynamicDeploymentId(dynamicDeploymentID);

        //Write the log to the repository
        this.discoveryLogRepository.insert(discoveryLog);
    }

    /**
     * Writes a given {@link DiscoveryLog} that belongs to a certain {@link DynamicDeployment} to the corresponding
     * repository and stores it persistently.
     *
     * @param discoveryLog The {@link DiscoveryLog} to write
     */
    public void writeDiscoveryLog(DiscoveryLog discoveryLog) {
        //Null check
        if (discoveryLog == null) throw new IllegalArgumentException("The discovery log must not be null.");

        //Validate the referenced ID of the dynamic deployment
        validateDynamicDeploymentID(discoveryLog.getDynamicDeploymentId());

        //Write the log to the repository
        this.discoveryLogRepository.insert(discoveryLog);
    }

    /**
     * Deletes all {@link DiscoveryLog}s that are available for a certain {@link DynamicDeployment}, given by its ID.
     *
     * @param dynamicDeploymentId The ID of the {@link DynamicDeployment}
     */
    public void deleteDiscoveryLogs(String dynamicDeploymentId) {
        //Sanity check
        if ((dynamicDeploymentId == null) || dynamicDeploymentId.isEmpty())
            throw new IllegalArgumentException("The dynamic deployment ID must not be null or empty.");

        //Delete the pertaining discovery logs
        this.discoveryLogRepository.deleteByDynamicDeploymentId(dynamicDeploymentId);
    }

    /**
     * Checks whether a given ID is valid and belongs to an existing {@link DynamicDeployment}.
     *
     * @param dynamicDeploymentId The dynamic deployment ID to validate
     */
    private void validateDynamicDeploymentID(String dynamicDeploymentId) {
        //Sanity check
        if ((dynamicDeploymentId == null) || dynamicDeploymentId.isEmpty()) {
            throw new IllegalArgumentException("The dynamic deployment ID must not be null or empty");
        }

        //Check if dynamic deployment exists
        if (!dynamicDeploymentRepository.existsById(dynamicDeploymentId)) {
            throw new IllegalArgumentException("A dynamic deployment with this ID does not exist.");
        }
    }
}
