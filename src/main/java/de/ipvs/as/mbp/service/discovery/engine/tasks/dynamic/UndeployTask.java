package de.ipvs.as.mbp.service.discovery.engine.tasks.dynamic;

import de.ipvs.as.mbp.DynamicBeanProvider;
import de.ipvs.as.mbp.domain.discovery.deployment.DynamicDeployment;
import de.ipvs.as.mbp.domain.discovery.deployment.DynamicDeploymentDeviceDetails;
import de.ipvs.as.mbp.domain.discovery.deployment.DynamicDeploymentState;
import de.ipvs.as.mbp.domain.discovery.deployment.log.DiscoveryLog;
import de.ipvs.as.mbp.domain.discovery.deployment.log.DiscoveryLogMessage;
import de.ipvs.as.mbp.domain.discovery.deployment.log.DiscoveryLogMessageType;
import de.ipvs.as.mbp.domain.discovery.device.DeviceTemplate;
import de.ipvs.as.mbp.domain.operator.Operator;
import de.ipvs.as.mbp.repository.discovery.DynamicDeploymentRepository;
import de.ipvs.as.mbp.service.discovery.deployment.DiscoveryDeploymentService;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import static de.ipvs.as.mbp.domain.discovery.deployment.log.DiscoveryLogMessageType.INFO;
import static de.ipvs.as.mbp.domain.discovery.deployment.log.DiscoveryLogMessageType.SUCCESS;

/**
 * The purpose of this task is to undeploy the {@link Operator} of a given {@link DynamicDeployment}
 * from the device that is referenced as {@link DynamicDeploymentDeviceDetails} in the {@link DynamicDeployment},
 * in case the {@link DynamicDeployment} is deployed on it.
 */
public class UndeployTask implements DynamicDeploymentTask {

    //Whether this tasks requires access to the candidate devices
    private static final boolean DEPENDS_ON_CANDIDATE_DEVICES = false;

    //The original version of the dynamic deployment that is supposed to be deployed
    private DynamicDeployment originalDynamicDeployment;

    //The discovery log to extend for further log messages
    private DiscoveryLog discoveryLog;

    /*
    Injected fields
     */
    private final DiscoveryDeploymentService discoveryDeploymentService;
    private final DynamicDeploymentRepository dynamicDeploymentRepository;
    private final MongoTemplate mongoTemplate;

    /**
     * Creates a new {@link UndeployTask} from a given {@link DynamicDeployment} and a {@link DiscoveryLog}.
     *
     * @param dynamicDeployment The dynamic deployment to use
     * @param discoveryLog      The {@link DiscoveryLog} to use for logging within this task
     */
    public UndeployTask(DynamicDeployment dynamicDeployment, DiscoveryLog discoveryLog) {
        //Set fields
        setDynamicDeployment(dynamicDeployment);
        setDiscoveryLog(discoveryLog);

        //Inject components
        this.discoveryDeploymentService = DynamicBeanProvider.get(DiscoveryDeploymentService.class);
        this.dynamicDeploymentRepository = DynamicBeanProvider.get(DynamicDeploymentRepository.class);
        this.mongoTemplate = DynamicBeanProvider.get(MongoTemplate.class);
    }

    /**
     * Implements the actual operations of the task. It is recommended to check for Thread interruptions in order to
     * gracefully deal with cancellations of the task.
     */
    @Override
    public void run() {
        //Read dynamic deployment from the repository
        DynamicDeployment dynamicDeployment = dynamicDeploymentRepository.findById(this.originalDynamicDeployment.getId()).orElse(null);

        //Sanity checks
        if (dynamicDeployment == null) {
            //Task ends because data is not available
            return;
        }

        //Check intention for dynamic deployment
        if (dynamicDeployment.isActivatingIntended()) {
            //De-activate is not intended, so no need to undeploy
            return;
        }

        //Check whether the dynamic deployment is currently deployed
        if ((dynamicDeployment.getLastDeviceDetails() == null) ||
                (!this.discoveryDeploymentService.isDeployed(dynamicDeployment))) {
            //Dynamic deployment is not deployed, so just update the state
            updateDynamicDeployment(dynamicDeployment.getId(), null, DynamicDeploymentState.DISABLED);
            return;
        }

        //Write log
        addLogMessage("Started task.");

        //Write log
        addLogMessage(String.format("Operator is currently deployed to %s, undeploying.", dynamicDeployment.getLastDeviceDetails().getMacAddress()));

        //Dynamic deployment is deployed, so undeploy it
        this.discoveryDeploymentService.undeploy(dynamicDeployment);

        //Write log
        addLogMessage(SUCCESS, "Undeployed the operator from its former device.");

        //Update state of dynamic deployment accordingly
        updateDynamicDeployment(dynamicDeployment.getId(), null, DynamicDeploymentState.DISABLED);
    }

    /**
     * Returns the {@link DiscoveryLog} that is used within this task in order to collect
     * {@link DiscoveryLogMessage}s for logging purposes. May be null, if the task does not perform logging.
     *
     * @return The {@link DiscoveryLog} or null, if logging is not performed
     */
    @Override
    public DiscoveryLog getDiscoveryLog() {
        return this.discoveryLog;
    }

    /**
     * Sets the {@link DiscoveryLog} that is supposed to be used within this task in order to collect
     * {@link DiscoveryLogMessage}s for logging purposes. If set to null, logging is not formed.
     *
     * @param discoveryLog The {@link DiscoveryLog} or null, if no logging is supposed to be performed
     */
    private void setDiscoveryLog(DiscoveryLog discoveryLog) {
        this.discoveryLog = discoveryLog;
    }

    /**
     * Updates the last device details and last state field of a certain {@link DynamicDeployment}, given by its
     * ID, in its repository. By using this method instead of the {@link DynamicDeploymentRepository},
     * lost updates on other fields can be avoided.
     *
     * @param dynamicDeploymentId The ID of the {@link DynamicDeployment} to update
     * @param newDeviceDetails    The new device details to set
     * @param newState            The new state to set
     */
    private void updateDynamicDeployment(String dynamicDeploymentId, DynamicDeploymentDeviceDetails newDeviceDetails, DynamicDeploymentState newState) {
        //Create query and update clauses
        Query query = Query.query(Criteria.where("id").is(dynamicDeploymentId));
        Update update = new Update().set("lastDeviceDetails", newDeviceDetails).set("lastState", newState);

        //Write changes to repository
        mongoTemplate.updateFirst(query, update, DynamicDeployment.class);
    }

    /**
     * Creates a new {@link DiscoveryLogMessage} from a given message string and adds it to the
     * {@link DiscoveryLog} that collects the logs of this task.
     *
     * @param message The actual log message
     */
    private void addLogMessage(String message) {
        //Delegate call
        addLogMessage(INFO, message);
    }

    /**
     * Creates a new {@link DiscoveryLogMessage} from a given message string and a {@link DiscoveryLogMessageType}
     * and adds it to the {@link DiscoveryLog} that collects the logs of this task.
     *
     * @param type    The type of the log message
     * @param message The actual log message
     */
    private void addLogMessage(DiscoveryLogMessageType type, String message) {
        //Check if log messages are supposed to be collected
        if (this.discoveryLog == null) return;

        //Update start timestamp when this is the first log message
        if (discoveryLog.isEmpty()) discoveryLog.updateStartTimestamp();

        //Create new log message
        DiscoveryLogMessage logMessage = new DiscoveryLogMessage(type, message);

        //Add the message to the discovery log of this task
        discoveryLog.addMessage(logMessage);
    }

    /**
     * Returns the {@link DynamicDeployment} object that has been originally passed to this task and is
     * supposed to be (re-)deployed.
     *
     * @return The dynamic deployment
     */
    public DynamicDeployment getDynamicDeployment() {
        return this.originalDynamicDeployment;
    }

    /**
     * Sets the {@link DynamicDeployment} that is supposed to be (re-)deployed within this task.
     *
     * @param dynamicDeployment The dynamic deployment to set
     */
    private void setDynamicDeployment(DynamicDeployment dynamicDeployment) {
        //Null check
        if (dynamicDeployment == null) {
            throw new IllegalArgumentException("The dynamic deployment must not be null.");
        }

        this.originalDynamicDeployment = dynamicDeployment;
    }

    /**
     * Returns the ID of the {@link DynamicDeployment} on which this task operates.
     *
     * @return The dynamic deployment ID
     */
    @Override
    public String getDynamicDeploymentId() {
        return this.originalDynamicDeployment.getId();
    }


    /**
     * Returns the ID of the device template that is used by the {@link DynamicDeployment} on which this task operates.
     *
     * @return The device template ID
     */
    @Override
    public String getDeviceTemplateId() {
        return this.originalDynamicDeployment.getDeviceTemplate().getId();
    }

    /**
     * Returns whether this task requires access to the candidate devices of the {@link DeviceTemplate} that is
     * referenced in the {@link DynamicDeployment}.
     *
     * @return True, if this task depends on the candidate devices; false otherwise
     */
    @Override
    public boolean dependsOnCandidateDevices() {
        return DEPENDS_ON_CANDIDATE_DEVICES;
    }

    /**
     * Returns whether this task may replace another, previously created task in the task queue.
     *
     * @return True, if the task may replace another task; false otherwise
     */
    @Override
    public boolean mayReplace() {
        return true;
    }

    /**
     * Returns a simple, short and human-readable description of the task.
     *
     * @return The human-readable description
     */
    @Override
    public String toHumanReadableString() {
        return "[Undeploy]";
    }
}
