package de.ipvs.as.mbp.service.discovery.log;

import de.ipvs.as.mbp.domain.discovery.deployment.DynamicDeployment;
import de.ipvs.as.mbp.domain.discovery.deployment.log.DynamicDeploymentLog;
import de.ipvs.as.mbp.domain.discovery.deployment.log.DynamicDeploymentLogEntry;
import de.ipvs.as.mbp.repository.discovery.DynamicDeploymentLogRepository;
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
 * Offers services for reading and writing {@link DynamicDeploymentLog}s of {@link DynamicDeployment}s. It especially
 * takes care of synchronizing write requests for {@link DynamicDeploymentLogEntry}s, such that the occurrence of issues
 * like lost updates can be avoided.
 */
@Service
public class DynamicDeploymentLogService {

    @Autowired
    private DynamicDeploymentRepository dynamicDeploymentRepository;

    @Autowired
    private DynamicDeploymentLogRepository dynamicDeploymentLogRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * Creates the {@link DynamicDeploymentLogService}.
     */
    public DynamicDeploymentLogService() {

    }

    /**
     * Initializes the {@link DynamicDeploymentLogService}.
     */
    @PostConstruct
    public void initialize() {

    }

    /**
     * Retrieves and returns the {@link DynamicDeploymentLogEntry}s for a certain {@link DynamicDeployment}, given
     * by its ID.
     *
     * @param dynamicDeploymentId The ID of the dynamic deployment
     * @return The resulting list of {@link DynamicDeploymentLogEntry}s.
     */
    public List<DynamicDeploymentLogEntry> readLogEntries(String dynamicDeploymentId) {
        //Validate the dynamic deployment ID
        validateDynamicDeployment(dynamicDeploymentId);

        //Retrieve associated log from the repository and return its entries
        return dynamicDeploymentLogRepository.findByDynamicDeploymentId(dynamicDeploymentId).orElse(new DynamicDeploymentLog()).getEntries();
    }

    public synchronized void addLogEntry(String dynamicDeploymentId, DynamicDeploymentLogEntry entry) {
        //Validate the dynamic deployment ID
        validateDynamicDeployment(dynamicDeploymentId);

        //Null check
        if (entry == null) throw new IllegalArgumentException("The log entry to write must not be null.");

        //TODO remove if not needed
        //Check if a log for this dynamic deployment does already exist
        if (!dynamicDeploymentLogRepository.existsByDynamicDeploymentId(dynamicDeploymentId)) {
            //Does not exist, thus create it
            //dynamicDeploymentLogRepository.insert(new DynamicDeploymentLog(dynamicDeploymentId));
        }

        //Create query and criteria clauses for inserting the entry safely
        Update update = new Update().addToSet("entries", entry);
        Criteria criteria = Criteria.where("dynamicDeploymentId").is(dynamicDeploymentId);

        //Execute the update
        mongoTemplate.upsert(Query.query(criteria), update, DynamicDeploymentLog.class);
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
