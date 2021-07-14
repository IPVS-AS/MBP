package de.ipvs.as.mbp.service.discovery;

import de.ipvs.as.mbp.domain.discovery.collections.DeviceDescriptionCollection;
import de.ipvs.as.mbp.domain.discovery.collections.DeviceDescriptionRanking;
import de.ipvs.as.mbp.domain.discovery.description.DeviceDescription;
import de.ipvs.as.mbp.domain.discovery.device.DeviceTemplate;
import de.ipvs.as.mbp.domain.discovery.peripheral.DynamicPeripheral;
import de.ipvs.as.mbp.domain.discovery.topic.RequestTopic;
import de.ipvs.as.mbp.service.discovery.gateway.CandidateDevicesSubscriber;
import de.ipvs.as.mbp.service.discovery.gateway.DiscoveryGateway;
import de.ipvs.as.mbp.service.discovery.processing.DeviceDescriptionProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This components manages the overall discovery process by orchestrating the various involved components and takes
 * care about the execution of discovery-related tasks.
 */
@Component
public class DiscoveryEngine implements CandidateDevicesSubscriber {

    //Number of threads to use in the thread pool that executes deployment tasks
    private static final int THREAD_POOL_SIZE = 5;

    /*
    Auto-wired components
     */
    @Autowired
    private DeviceDescriptionProcessor deviceDescriptionProcessor;

    @Autowired
    private DiscoveryGateway discoveryGateway;

    //Executor service for executing deployment tasks asynchronously
    private ExecutorService executorService;

    /**
     * Creates the discovery engine.
     */
    public DiscoveryEngine() {
        //Create executor service
        this.executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
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
        //Step 5.3: If NO_CANDIDATE: Same
        //Step 5.4: If RUNNING: Read last MAC, get SSH details from it and check if operator is running.
        //Step 5.4.1: If yes: Do the usual check whether the current ranking shows a better device then the former one
        //Step 5.4.2: If not: Let the deployer deploy the peripheral with the current ranking

    }

    public void enableDynamicPeripheral(DynamicPeripheral dynamicPeripheral) {
        //Step 1: Check if already enabled and if yes, ignore
        //Step 2: Check if there is already data for the device template
        //Step 2.1: If not: Retrieve the data synchronously NOW and create subscription
        //Step 2.2 Store the data
        //Step 3: Set status of peripheral to deploying
        //Step 4: Calculate ranking from the data and pass everything to the deployer to take care (async task!)
        //Step 5: Return
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
     * @param updatedCandidateDevices The updated collection of candidate devices as {@link DeviceDescriptionCollection}
     */
    @Override
    public void onDeviceTemplateResultChanged(DeviceTemplate deviceTemplate, String repositoryName, DeviceDescriptionCollection updatedCandidateDevices) {
        //Step 0: Fetch previous candidate device results for the given device template
        //Step 1: Create copy of the previous raw results and integrate the updatedCandidateDevices by replacing by repoName
        //       Remark: Create own class for List<DeviceDescriptionCollection> that holds the template ID and offers methods for replacing parts
        //Step 2: Calculate ranking from the new device candidates
        //Step 3: Fetch all peripherals that currently use the given device template
        //Step 4: Iterate through all these peripherals and check their states
        //Step 4.1: If DISABLED: skip
        //Step 4.3: If DEPLOYING: Abort task of deployer and restart with new ranking ("search" means the deployment process here), even for empty ranking
        //Step 4.4: If NO_CANDIDATE: Start deployer task with new ranking, even for empty ranking (deployer will handle and return immediately)
        //Step 4.5: If RUNNING:
        //Step 4.5.1 If ranking is empty: Undeploy, on callback of deployer task set the state to NO_CANDIDATE
        //Step 4.5.2 Locate old device in the new ranking
        //Step 4.5.3 If old device is in the ranking and has still the highest score (or equal to highest): Do nothing and continue with next peripheral
        //Step 4.5.4 Pass new ranking to the deployer and instruct it to deploy to the device with the highest possible score. If success, the deployer should undeploy the old device using the SSH data from the old candidate device data
    }

    /**
     * Requests {@link DeviceDescription}s of suitable candidate devices which match a given {@link DeviceTemplate}
     * from the discovery repositories that are available under a given collection of {@link RequestTopic}s.
     * The {@link DeviceDescription}s of the candidate devices that are received from the discovery repositories
     * in response are returned as list of {@link DeviceDescriptionCollection}s, containing one collection
     * per repository. No subscription is created at the repositories as part of this request.
     *
     * @param deviceTemplate The device template to find suitable candidate devices for
     * @param requestTopics  The collection of {@link RequestTopic}s to use for sending the request to the repositories
     * @return The resulting list of {@link DeviceDescriptionCollection}s
     */
    public DeviceDescriptionRanking getDeviceCandidates(DeviceTemplate deviceTemplate, Collection<RequestTopic> requestTopics) {
        //Sanity checks
        if (deviceTemplate == null) {
            throw new IllegalArgumentException("The device template must not be null.");
        } else if ((requestTopics == null) || requestTopics.isEmpty() || (requestTopics.stream().anyMatch(Objects::isNull))) {
            throw new IllegalArgumentException("The request topics must not be null or empty.");
        }

        //Use the gateway to find all device descriptions that match the device template
        List<DeviceDescriptionCollection> deviceDescriptionCollections = this.discoveryGateway.getDeviceCandidates(deviceTemplate, requestTopics);

        //Use the processor to filter, aggregate, score and rank the descriptions
        return deviceDescriptionProcessor.process(deviceDescriptionCollections, deviceTemplate);
    }
}
