package de.ipvs.as.mbp.service.discovery.engine.tasks.dynamic;

import de.ipvs.as.mbp.DynamicBeanProvider;
import de.ipvs.as.mbp.domain.discovery.collections.CandidateDevicesRanking;
import de.ipvs.as.mbp.domain.discovery.collections.CandidateDevicesResult;
import de.ipvs.as.mbp.domain.discovery.collections.ScoredCandidateDevice;
import de.ipvs.as.mbp.domain.discovery.deployment.DynamicDeployment;
import de.ipvs.as.mbp.domain.discovery.deployment.DynamicDeploymentDeviceDetails;
import de.ipvs.as.mbp.domain.discovery.deployment.DynamicDeploymentState;
import de.ipvs.as.mbp.domain.discovery.deployment.log.DynamicDeploymentLog;
import de.ipvs.as.mbp.domain.discovery.deployment.log.DynamicDeploymentLogEntry;
import de.ipvs.as.mbp.domain.discovery.deployment.log.DynamicDeploymentLogEntryType;
import de.ipvs.as.mbp.domain.discovery.device.DeviceTemplate;
import de.ipvs.as.mbp.domain.operator.Operator;
import de.ipvs.as.mbp.repository.discovery.CandidateDevicesRepository;
import de.ipvs.as.mbp.repository.discovery.DynamicDeploymentRepository;
import de.ipvs.as.mbp.service.discovery.deployment.DiscoveryDeploymentService;
import de.ipvs.as.mbp.service.discovery.log.DynamicDeploymentLogService;
import de.ipvs.as.mbp.service.discovery.processing.CandidateDevicesProcessor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import static de.ipvs.as.mbp.domain.discovery.deployment.log.DynamicDeploymentLogEntryTrigger.DISCOVERY_REPOSITORY;
import static de.ipvs.as.mbp.domain.discovery.deployment.log.DynamicDeploymentLogEntryTrigger.USER;
import static de.ipvs.as.mbp.domain.discovery.deployment.log.DynamicDeploymentLogEntryType.*;

/**
 * The purpose of this task is to deploy a given {@link DynamicDeployment} with respect to the ranking which
 * can be computed from the candidate devices in the {@link CandidateDevicesResult} that is associated
 * with the {@link DeviceTemplate} underlying the {@link DynamicDeployment}. Thereby, the operator of the
 * {@link DynamicDeployment} is supposed to be deployed to the target device of the ranking that can be considered
 * as most appropriate. If necessary, this task also takes care of suspending possibly existing former deployments
 * of the {@link DynamicDeployment} by undeploying the corresponding {@link Operator} from its former target device.
 * The decision for the target device and whether a (re-)deployment is necessary is done according to the following
 * procedure:
 * <ul>
 *     <li>All devices of the device candidates ranking are considered in ascending order of their rank.</li>
 *     <li>If the dynamic deployment is currently already deployed to a device and the currently considered device
 *     has a score that is lower than or equal to the score of this former device in the new ranking, the former
 *     device is continued to be used.</li>
 *     <li>Otherwise, it is tried to deploy the operator to the current device. If the deployment fails, the next
 *     device in the ranking is considered.</li>
 * </ul>
 * <p>
 * This way, it is ensured that re-deployments of already deployed {@link DynamicDeployment} are only executed
 * in case a device is available that appears to be really better suited than the currently used device.
 */
public class DeployByRankingTask implements DynamicDeploymentTask {
    //Whether this tasks requires access to the candidate devices
    private static final boolean DEPENDS_ON_CANDIDATE_DEVICES = true;

    //The original version of the dynamic deployment that is supposed to be deployed
    private DynamicDeployment originalDynamicDeployment;

    //Indicates whether the task was created on behalf of an user
    private boolean isUserCreated = false;

    /*
    Injected fields
     */
    private final DynamicDeploymentLogService logService;
    private final CandidateDevicesProcessor candidateDevicesProcessor;
    private final DiscoveryDeploymentService discoveryDeploymentService;
    private final DynamicDeploymentRepository dynamicDeploymentRepository;
    private final CandidateDevicesRepository candidateDevicesRepository;
    private final MongoTemplate mongoTemplate;

    /**
     * Creates a new {@link DeployByRankingTask} from a given {@link DynamicDeployment}.
     *
     * @param dynamicDeployment The dynamic deployment to use
     */
    public DeployByRankingTask(DynamicDeployment dynamicDeployment) {
        //Delegate call
        this(dynamicDeployment, false);
    }

    /**
     * Creates a new {@link DeployByRankingTask} from a given {@link DynamicDeployment} and an indication
     * whether the task was created on behalf of an user.
     *
     * @param dynamicDeployment The dynamic deployment to use
     * @param isUserCreated     True, if the task was created on behalf of an user; false otherwise
     */
    public DeployByRankingTask(DynamicDeployment dynamicDeployment, boolean isUserCreated) {
        //Set fields
        setDynamicDeployment(dynamicDeployment);
        setIsUserCreated(isUserCreated);

        //Inject components
        this.logService = DynamicBeanProvider.get(DynamicDeploymentLogService.class);
        this.candidateDevicesProcessor = DynamicBeanProvider.get(CandidateDevicesProcessor.class);
        this.discoveryDeploymentService = DynamicBeanProvider.get(DiscoveryDeploymentService.class);
        this.dynamicDeploymentRepository = DynamicBeanProvider.get(DynamicDeploymentRepository.class);
        this.candidateDevicesRepository = DynamicBeanProvider.get(CandidateDevicesRepository.class);
        this.mongoTemplate = DynamicBeanProvider.get(MongoTemplate.class);
    }

    /**
     * Implements the actual operations of the task. It is recommended to check for Thread interruptions in order to
     * gracefully deal with cancellations of the task.
     */
    @Override
    public void run() {
        //Read dynamic deployment and candidate devices from their repositories
        DynamicDeployment dynamicDeployment = dynamicDeploymentRepository.findById(this.originalDynamicDeployment.getId()).orElse(null);
        CandidateDevicesResult candidateDevices = candidateDevicesRepository.findById(getDeviceTemplateId()).orElse(null);

        //Sanity checks
        if ((dynamicDeployment == null) || (candidateDevices == null)) {
            //Task ends because data is not available
            return;
        }

        //Check intention for dynamic deployment
        if (!dynamicDeployment.isActivatingIntended()) {
            //Active is not intended, so no need to deploy; potential undeployment will be done by another task
            return;
        }

        //Write log
        writeLog("Started deployment task.");

        //Determine whether the dynamic deployment is currently deployed
        boolean isDeployed = (dynamicDeployment.getLastDeviceDetails() != null) &&
                this.discoveryDeploymentService.isDeployed(dynamicDeployment);

        //Write log
        writeLog("Operator is currently " + (isDeployed ? "deployed to " + dynamicDeployment.getLastDeviceDetails().getMacAddress() : "not deployed") + ".");

        //Calculate and candidate devices ranking
        CandidateDevicesRanking ranking = this.candidateDevicesProcessor.process(candidateDevices, dynamicDeployment.getDeviceTemplate());

        //Check if ranking is valid
        if ((ranking == null) || (ranking.isEmpty())) {
            //Write log
            writeLog("Ranking is empty, " + (isDeployed ? "trying to undeploy from currently used device." : "no deployment possible."), UNDESIRABLE);

            //No suitable candidate devices; if there is still an active deployment, we need to undeploy it
            if (isDeployed) {
                this.discoveryDeploymentService.undeploy(dynamicDeployment);
            }
            //Update last device details and status
            updateDynamicDeployment(dynamicDeployment.getId(), null, DynamicDeploymentState.NO_CANDIDATE);
            return;
        }

        //Write log
        writeLog(ranking.toHumanReadableDescription());

        //Get MAC address of current deployment (if existing)
        String oldMacAddress = dynamicDeployment.getLastDeviceDetails() == null ? null : dynamicDeployment.getLastDeviceDetails().getMacAddress();

        //Determine score of old device (if existing) in the new ranking
        double minScoreExclusive = isDeployed ? ranking.getScoreByMacAddress(oldMacAddress) : -1;

        //Write log
        if (minScoreExclusive >= 0)
            writeLog(String.format("Currently used device has now a score of [%f].", minScoreExclusive));

        //Iterate through the ranking
        for (ScoredCandidateDevice candidateDevice : ranking) {
            //Check if score is still in range
            if (candidateDevice.getScore() <= minScoreExclusive) {
                //Write log
                writeLog("Currently used device is better suited than the remainder of the ranking, thus aborting.");

                //Deployment is already deployed and the device is still best suited
                updateDynamicDeployment(dynamicDeployment.getId(), dynamicDeployment.getLastDeviceDetails(), DynamicDeploymentState.DEPLOYED);
                return;
            }

            //Check if current candidate is the same as the existing deployment (probably this check is not needed)
            if (isDeployed && (candidateDevice.getIdentifiers().getMacAddress().equals(oldMacAddress)))
                continue; //Skip candidate device because it is equal to the current device

            //Write log
            writeLog(String.format("Trying to deploy operator to %s.", candidateDevice.getIdentifiers().getMacAddress()));

            //Try to deploy to candidate device and check for success
            if (this.discoveryDeploymentService.deploy(dynamicDeployment, candidateDevice)) {
                //Write log
                writeLog(String.format("Deployment to %s succeeded.", candidateDevice.getIdentifiers().getMacAddress()), SUCCESS);

                //Success; if former deployment existed, undeploy from former device
                if (isDeployed) {
                    writeLog("Undeploying from old device.");
                    this.discoveryDeploymentService.undeploy(dynamicDeployment);
                }

                //Update the dynamic deployment accordingly and set the state
                updateDynamicDeployment(dynamicDeployment.getId(), new DynamicDeploymentDeviceDetails(candidateDevice), DynamicDeploymentState.DEPLOYED);
                return;
            }

            //Write log
            writeLog(String.format("Deployment failed for %s.", candidateDevice.getIdentifiers().getMacAddress()), UNDESIRABLE);
        }

        /*
        All valid candidate devices of the ranking were checked, two cases remain:
        - Case 1: No former deployment existed and deployment failed for all candidate devices of the non-empty ranking
        - Case 2: Former deployment existed and is still most appropriate, because all other candidate devices of
                  the non-empty ranking failed
         */

        //Check if deployment exists
        if (isDeployed) {
            //Write log
            writeLog("Deployment failed for better suited candidate devices, thus preserving the current deployment.", UNDESIRABLE);

            //Former deployment is still the most appropriate deployment, update status accordingly
            updateDynamicDeployment(dynamicDeployment.getId(), dynamicDeployment.getLastDeviceDetails(), DynamicDeploymentState.DEPLOYED);
            return;
        }

        //Write log
        writeLog("Deployment failed for all candidate devices.", UNDESIRABLE);

        //No deployment exists and deployment failed for all candidate devices
        updateDynamicDeployment(dynamicDeployment.getId(), null, DynamicDeploymentState.ALL_FAILED);
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
     * Creates a new {@link DynamicDeploymentLogEntry} from a given message and writes it to the
     * {@link DynamicDeploymentLog} of the pertaining {@link DynamicDeployment}.
     *
     * @param message The message of the log entry
     */
    private void writeLog(String message) {
        //Delegate call
        writeLog(message, INFO);
    }

    /**
     * Creates a new {@link DynamicDeploymentLogEntry} from a given message and {@link DynamicDeploymentLogEntryType}
     * and writes it to the {@link DynamicDeploymentLog} of the pertaining {@link DynamicDeployment}.
     *
     * @param message The message of the log entry
     * @param type    The type of the log entry
     */
    private void writeLog(String message, DynamicDeploymentLogEntryType type) {
        //Create new log entry
        DynamicDeploymentLogEntry logEntry = new DynamicDeploymentLogEntry(type, isUserCreated ? USER : DISCOVERY_REPOSITORY, this.getClass().getSimpleName(), message);

        //Write log entry using the log service
        logService.addLogEntry(getDynamicDeploymentId(), logEntry);
    }

    /**
     * Returns whether this task was created on behalf of an user.
     *
     * @return True, if the task was created on behalf of an user; false otherwise
     */
    @Override
    public boolean isUserCreated() {
        return this.isUserCreated;
    }

    /**
     * Sets whether this task was created on behalf of an user.
     *
     * @param isUserCreated True, if the task was created on behalf of an user; false otherwise
     */
    private void setIsUserCreated(boolean isUserCreated) {
        this.isUserCreated = isUserCreated;
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
     * Returns a simple, short and human-readable description of the task.
     *
     * @return The human-readable description
     */
    @Override
    public String toHumanReadableString() {
        return "[Deploy by ranking]";
    }
}
