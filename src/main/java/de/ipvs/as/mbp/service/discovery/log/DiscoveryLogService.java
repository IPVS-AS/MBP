package de.ipvs.as.mbp.service.discovery.log;

import de.ipvs.as.mbp.domain.discovery.deployment.DynamicDeployment;
import de.ipvs.as.mbp.domain.discovery.deployment.log.DiscoveryLog;
import de.ipvs.as.mbp.domain.discovery.deployment.log.DiscoveryLogEntry;
import de.ipvs.as.mbp.repository.discovery.DiscoveryLogRepository;
import de.ipvs.as.mbp.repository.discovery.DynamicDeploymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * Offers services for reading and writing {@link DiscoveryLog}s of {@link DynamicDeployment}s. It especially
 * takes care of synchronizing write requests for {@link DiscoveryLogEntry}s, such that typical issues like lost
 * updates are avoided.
 */
@Service
public class DiscoveryLogService {

    @Autowired
    private DynamicDeploymentRepository dynamicDeploymentRepository;

    @Autowired
    private DiscoveryLogRepository discoveryLogRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

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
     * Retrieves and returns the {@link DiscoveryLogEntry}s for a certain {@link DynamicDeployment}, given
     * by its ID.
     *
     * @param dynamicDeploymentId The ID of the {@link DynamicDeployment}
     * @return The resulting list of {@link DiscoveryLogEntry}s.
     */
    public List<DiscoveryLogEntry> readLogEntries(String dynamicDeploymentId) {
        //Validate the dynamic deployment ID
        validateDynamicDeployment(dynamicDeploymentId);

        //Retrieve associated log from the repository and return its entries
        return discoveryLogRepository.findByDynamicDeploymentId(dynamicDeploymentId).orElse(new DiscoveryLog()).getEntries();
    }


    /**
     * Adds a given {@link DiscoveryLogEntry} to the {@link DiscoveryLog} of a certain {@link DynamicDeployment}, given
     * by its ID.
     *
     * @param dynamicDeploymentId To ID of the {@link DynamicDeployment}
     * @param entry               The {@link DiscoveryLogEntry} to add
     */
    public void addLogEntry(String dynamicDeploymentId, DiscoveryLogEntry entry) {
        //Null check
        if (entry == null) throw new IllegalArgumentException("The log entry must not be null.");

        //Validate the dynamic deployment ID
        validateDynamicDeployment(dynamicDeploymentId);

        //Create query and criteria clauses for inserting the entry in an atomic way
        Update update = new Update().addToSet("entries", entry);
        Criteria criteria = Criteria.where("dynamicDeploymentId").is(dynamicDeploymentId);

        //Execute the update as upsert
        mongoTemplate.upsert(Query.query(criteria), update, DiscoveryLog.class);
    }

    /**
     * Checks whether a given ID is valid and belongs to an existing {@link DynamicDeployment}.
     *
     * @param dynamicDeploymentId The dynamic deployment ID to validate
     */
    private void validateDynamicDeployment(String dynamicDeploymentId) {
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
