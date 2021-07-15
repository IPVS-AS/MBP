package de.ipvs.as.mbp.service.discovery;

import de.ipvs.as.mbp.domain.discovery.collections.CandidateDevicesCollection;
import de.ipvs.as.mbp.domain.discovery.collections.CandidateDevicesRanking;
import de.ipvs.as.mbp.domain.discovery.collections.CandidateDevicesResultContainer;
import de.ipvs.as.mbp.domain.discovery.description.DeviceDescription;
import de.ipvs.as.mbp.domain.discovery.device.DeviceTemplate;
import de.ipvs.as.mbp.domain.discovery.peripheral.DynamicPeripheral;
import de.ipvs.as.mbp.domain.discovery.peripheral.DynamicPeripheralStatus;
import de.ipvs.as.mbp.domain.discovery.topic.RequestTopic;
import de.ipvs.as.mbp.repository.discovery.CandidateDevicesRepository;
import de.ipvs.as.mbp.repository.discovery.DynamicPeripheralRepository;
import de.ipvs.as.mbp.service.discovery.deployment.DeploymentCompletionListener;
import de.ipvs.as.mbp.service.discovery.deployment.DeploymentResult;
import de.ipvs.as.mbp.service.discovery.deployment.DiscoveryDeploymentExecutor;
import de.ipvs.as.mbp.service.discovery.gateway.CandidateDevicesSubscriber;
import de.ipvs.as.mbp.service.discovery.gateway.DiscoveryGateway;
import de.ipvs.as.mbp.service.discovery.processing.CandidateDevicesProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * This components manages the overall discovery process by orchestrating the various involved components and takes
 * care about the execution of discovery-related tasks.
 */
@Component
public class DiscoveryEngine implements CandidateDevicesSubscriber, DeploymentCompletionListener {

    /*
    Auto-wired components
     */
    @Autowired
    private DiscoveryGateway discoveryGateway;

    @Autowired
    private CandidateDevicesProcessor candidateDevicesProcessor;

    @Autowired
    private DiscoveryDeploymentExecutor discoveryDeploymentExecutor;

    @Autowired
    private DynamicPeripheralRepository dynamicPeripheralRepository;

    @Autowired
    private CandidateDevicesRepository candidateDevicesRepository;

    //Map (dynamic peripheral ID --> completable future) of currently ongoing deployment tasks
    private Map<String, CompletableFuture<DeploymentResult>> deploymentTasks;


    /**
     * Creates the discovery engine.
     */
    public DiscoveryEngine() {
        //Initialize data structures
        this.deploymentTasks = new HashMap<>();
    }

    /**
     * Initializes the discovery engine.
     */
    @PostConstruct
    public void initialize() {
        //Step 0: Iterate through all device templates
        //Step 1: Check if there active peripherals for the current device template
        //Step 2: If not: Skip the device template and continue
        //Step 3: Read and store raw device candidate data from the repository for this template and tell the repository in this message to remove all subscriptions
        //Step 4: Request new candidate devices and update the data in the repository
        //Step 5: Iterate over all peripherals that use the device template and check their state
        //Step 5.1: If DISABLED: Do nothing
        //Step 5.2: If DEPLOYING: Let the deployer deploy the peripheral with the current ranking
        //Step 5.3: If NO_CANDIDATE/ALL_FAILED: Same
        //Step 5.4: If RUNNING: Read last MAC, get SSH details from it and check if operator is running.
        //Step 5.4.1: If yes: Do the usual check whether the current ranking shows a better device then the former one
        //Step 5.4.2: If not: Let the deployer deploy the peripheral with the current ranking

    }

    public synchronized void enableDynamicPeripheral(DynamicPeripheral dynamicPeripheral) {
        /* Steps to perform:
        Step 1: Check if already enabled and if yes, ignore
        Step 2: Check if there is already data for the device template
                --> If not: Retrieve the data (blocking), store it and create subscription
        Step 3: Set status of peripheral to deploying
        Step 4: Calculate ranking from the data and pass everything to the deployer to take care (async task!)
        Step 5: Return */

        //Null check
        if (dynamicPeripheral == null) {
            throw new IllegalArgumentException("The dynamic peripheral must not be null.");
        }

        //Check if dynamic peripheral is already enabled by the user
        if (dynamicPeripheral.isEnabled()) {
            //return; TODO
        }

        //Set dynamic peripheral to enabled in order to avoid duplicated executions
        setDynamicPeripheralEnabledState(dynamicPeripheral, true);

        //Get candidate devices and update them in the database if necessary
        CandidateDevicesResultContainer candidateDevices = retrieveAndUpdateCandidateDevices(dynamicPeripheral, false);

        //Calculate ranking from the candidate devices
        CandidateDevicesRanking ranking = candidateDevicesProcessor.process(candidateDevices, dynamicPeripheral.getDeviceTemplate());

        //Deploy, using the ranking as reference
        CompletableFuture<DeploymentResult> deploymentTask =
                this.discoveryDeploymentExecutor.deployByRanking(dynamicPeripheral, ranking, this);

        //Place the deployment task in the map
        this.deploymentTasks.put(dynamicPeripheral.getId(), deploymentTask);

        //Update status of dynamic peripheral
        dynamicPeripheral.setStatus(DynamicPeripheralStatus.DEPLOYING);
    }

    public void disableDynamicPeripheral(DynamicPeripheral dynamicPeripheral) {
        //Step 1: Check if already disabled and if yes, ignore
        //Step 2: Use the deployer to undeploy asynchronously, if needed
        //Step 3: Set status to disabled
        //Step 4: Check if dynamic peripherals remain for the corresponding device template
        //Step 5: If no peripherals remain, unsubscribe from the device template by sending the unsubscription message
    }

    /**
     * Called in case a notification was received from a repository as result of a subscription,
     * indicating that the collection of suitable candidate devices, which could be determined on behalf of a
     * certain {@link DeviceTemplate}, changed over time.
     *
     * @param deviceTemplate          The device template for which the candidate devices are retrieved
     * @param repositoryName          The name of the repository that issued the notification
     * @param updatedCandidateDevices The updated collection of candidate devices as {@link CandidateDevicesCollection}
     */
    @Override
    public void onDeviceTemplateResultChanged(DeviceTemplate deviceTemplate, String repositoryName, CandidateDevicesCollection updatedCandidateDevices) {
        //Step 0: Fetch previous candidate device results for the given device template
        //Step 1: Create copy of the previous raw results and integrate the updatedCandidateDevices by replacing by repoName
        //       Remark: Create own class for List<DeviceDescriptionCollection> that holds the template ID and offers methods for replacing parts
        //Step 2: Calculate ranking from the new device candidates
        //Step 3: Fetch all peripherals that currently use the given device template
        //Step 4: Iterate through all these peripherals and check their states
        //Step 4.1: If DISABLED: skip
        //Step 4.3: If DEPLOYING: Abort task of deployer and restart with new ranking ("search" means the deployment process here), even for empty ranking
        //Step 4.4: If NO_CANDIDATE/ALL_FAILED: Start deployer task with new ranking, even for empty ranking (deployer will handle and return immediately)
        //Step 4.5: If RUNNING:
        //Step 4.5.1 If ranking is empty: Undeploy, on callback of deployer task set the state to NO_CANDIDATE
        //Step 4.5.2 Locate old device in the new ranking
        //Step 4.5.3 If old device is in the ranking and has still the highest score (or equal to highest): Do nothing and continue with next peripheral
        //Step 4.5.4 Pass new ranking to the deployer and instruct it to deploy to the device with the highest possible score. If success, the deployer should undeploy the old device using the SSH data from the old candidate device data
    }

    /**
     * Called in case a certain deployment task, which was scheduled at the {@link DiscoveryDeploymentExecutor},
     * completed.
     *
     * @param dynamicPeripheral The dynamic peripheral that was supposed to be deployed
     * @param result            The result of the deployment
     */
    @Override
    public void onDeploymentCompleted(DynamicPeripheral dynamicPeripheral, DeploymentResult result) {
        //Delete deployment task from map
        this.deploymentTasks.remove(dynamicPeripheral.getId());

        //Check the deployment result
        switch (result) {
            case DEPLOYED:
                setDynamicPeripheralStatus(dynamicPeripheral, DynamicPeripheralStatus.DEPLOYED);
                break;
            case ALL_FAILED:
                setDynamicPeripheralStatus(dynamicPeripheral, DynamicPeripheralStatus.ALL_FAILED);
                break;
            case EMPTY_RANKING:
                setDynamicPeripheralStatus(dynamicPeripheral, DynamicPeripheralStatus.NO_CANDIDATE);
                break;
        }
    }

    /**
     * Requests {@link DeviceDescription}s of suitable candidate devices which match a given {@link DeviceTemplate}
     * from the discovery repositories that are available under a given collection of {@link RequestTopic}s.
     * The {@link DeviceDescription}s of the candidate devices that are received from the discovery repositories
     * in response are processed, scored with respect to to the {@link DeviceTemplate} and transformed to a ranking,
     * which is subsequently returned as {@link CandidateDevicesRanking}.
     *
     * @param deviceTemplate The device template to find suitable candidate devices for
     * @param requestTopics  The collection of {@link RequestTopic}s to use for sending the request to the repositories
     * @return The resulting {@link CandidateDevicesRanking}
     */
    public CandidateDevicesRanking getRankedDeviceCandidates(DeviceTemplate deviceTemplate, Collection<RequestTopic> requestTopics) {
        //Sanity checks
        if (deviceTemplate == null) {
            throw new IllegalArgumentException("The device template must not be null.");
        } else if ((requestTopics == null) || requestTopics.isEmpty() || (requestTopics.stream().anyMatch(Objects::isNull))) {
            throw new IllegalArgumentException("The request topics must not be null or empty.");
        }

        //Use the gateway to find all candidate devices that match the device template
        CandidateDevicesResultContainer candidateDevices = this.discoveryGateway.getDeviceCandidates(deviceTemplate, requestTopics);

        //Use the processor to filter, aggregate, score and rank the candidate devices
        return candidateDevicesProcessor.process(candidateDevices, deviceTemplate);
    }

    private CandidateDevicesResultContainer retrieveAndUpdateCandidateDevices(DynamicPeripheral dynamicPeripheral, boolean force) {
        DeviceTemplate deviceTemplate = dynamicPeripheral.getDeviceTemplate();

        if ((!force) && candidateDevicesRepository.existsById(deviceTemplate.getId())) {
            return candidateDevicesRepository.findById(deviceTemplate.getId()).orElse(null);
        }

        CandidateDevicesResultContainer candidateDevices = this.discoveryGateway.getDeviceCandidatesWithSubscription(deviceTemplate, dynamicPeripheral.getRequestTopics(), this);
        candidateDevicesRepository.save(candidateDevices);

        return candidateDevices;
    }

    private void setDynamicPeripheralStatus(DynamicPeripheral dynamicPeripheral, DynamicPeripheralStatus status) {
        //Update status
        dynamicPeripheral.setStatus(status);

        //Update enable state as well if necessary
        if (status.equals(DynamicPeripheralStatus.DISABLED)) {
            dynamicPeripheral.setEnabled(false);
        }

        //Update in repository
        this.dynamicPeripheralRepository.save(dynamicPeripheral);
    }

    private void setDynamicPeripheralEnabledState(DynamicPeripheral dynamicPeripheral, boolean enabled) {
        //Update enable state
        dynamicPeripheral.setEnabled(enabled);

        //Update in repository
        dynamicPeripheralRepository.save(dynamicPeripheral);
    }
}
