package de.ipvs.as.mbp.service.discovery.engine;

import de.ipvs.as.mbp.domain.discovery.collections.CandidateDevicesCollection;
import de.ipvs.as.mbp.domain.discovery.collections.CandidateDevicesRanking;
import de.ipvs.as.mbp.domain.discovery.collections.CandidateDevicesResultContainer;
import de.ipvs.as.mbp.domain.discovery.description.DeviceDescription;
import de.ipvs.as.mbp.domain.discovery.device.DeviceTemplate;
import de.ipvs.as.mbp.domain.discovery.peripheral.DynamicPeripheral;
import de.ipvs.as.mbp.domain.discovery.topic.RequestTopic;
import de.ipvs.as.mbp.repository.discovery.CandidateDevicesRepository;
import de.ipvs.as.mbp.repository.discovery.DynamicPeripheralRepository;
import de.ipvs.as.mbp.repository.discovery.RequestTopicRepository;
import de.ipvs.as.mbp.service.discovery.deployment.DeploymentCompletionListener;
import de.ipvs.as.mbp.service.discovery.deployment.DeploymentResult;
import de.ipvs.as.mbp.service.discovery.deployment.DiscoveryDeploymentExecutor;
import de.ipvs.as.mbp.service.discovery.engine.tasks.DiscoveryTask;
import de.ipvs.as.mbp.service.discovery.engine.tasks.ReplacingTaskQueue;
import de.ipvs.as.mbp.service.discovery.engine.tasks.TaskWrapper;
import de.ipvs.as.mbp.service.discovery.engine.tasks.dynamic.DynamicPeripheralTask;
import de.ipvs.as.mbp.service.discovery.engine.tasks.template.DeviceTemplateTask;
import de.ipvs.as.mbp.service.discovery.engine.tasks.template.UpdateCandidateDevicesTask;
import de.ipvs.as.mbp.service.discovery.gateway.CandidateDevicesSubscriber;
import de.ipvs.as.mbp.service.discovery.gateway.DiscoveryGateway;
import de.ipvs.as.mbp.service.discovery.processing.CandidateDevicesProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This components manages the overall discovery process by orchestrating the various involved components and takes
 * care about the execution of discovery-related tasks.
 */
@Component
public class DiscoveryEngine implements CandidateDevicesSubscriber, DeploymentCompletionListener {

    //Number of threads to use in the thread pool that executes the tasks
    private static final int THREAD_POOL_SIZE = 5;

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
    private RequestTopicRepository requestTopicRepository;

    @Autowired
    private DynamicPeripheralRepository dynamicPeripheralRepository;

    @Autowired
    private CandidateDevicesRepository candidateDevicesRepository;

    //Map (device template ID --> Queue) of task queues for device templates
    private final Map<String, LinkedList<TaskWrapper<DeviceTemplateTask>>> deviceTemplateTasks;

    //Map (dynamic peripheral ID --> Queue) of task queues for dynamic peripherals
    private final Map<String, ReplacingTaskQueue<TaskWrapper<DynamicPeripheralTask>>> dynamicPeripheralTasks;

    //Executor service for executing tasks
    private final ExecutorService executorService;

    /**
     * Creates the discovery engine.
     */
    public DiscoveryEngine() {
        //Initialize data structures
        this.dynamicPeripheralTasks = new HashMap<>();
        this.deviceTemplateTasks = new HashMap<>();

        //Initialize executor service
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
        //Step 5.3: If NO_CANDIDATE/ALL_FAILED: Same
        //Step 5.4: If RUNNING: Read last MAC, get SSH details from it and check if operator is running.
        //Step 5.4.1: If yes: Do the usual check whether the current ranking shows a better device then the former one
        //Step 5.4.2: If not: Let the deployer deploy the peripheral with the current ranking

    }

    public void enableDynamicPeripheral(String dynamicPeripheralId) {
        /* Steps to perform:
        Step 1: Check if already enabled and if yes, ignore
        Step 2: Check if there is already data for the device template
                --> If not: Retrieve the data (blocking), store it and create subscription
        Step 3: Set status of peripheral to deploying
        Step 4: Calculate ranking from the data and pass everything to the deployer to take care (async task!)
        Step 5: Return */

        //Get dynamic peripheral exclusively for enabling
        DynamicPeripheral dynamicPeripheral = requestDynamicPeripheralExclusively(dynamicPeripheralId, true);

        //Null check
        if (dynamicPeripheral == null) return;

        //Get all request topics of the user
        List<RequestTopic> requestTopics = requestTopicRepository.findByOwner(dynamicPeripheral.getOwner().getId(), null);

        //Submit task for retrieving candidate devices (will abort if not needed due to force=false)
        submitTask(new UpdateCandidateDevicesTask(dynamicPeripheral.getDeviceTemplate(), requestTopics, false));

        //TODO submit task for deployment
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
     * Called as soon as a certain deployment task, which was scheduled at the {@link DiscoveryDeploymentExecutor},
     * completed.
     *
     * @param dynamicPeripheral The dynamic peripheral that was supposed to be deployed
     * @param result            The result of the deployment
     */
    @Override
    public void onDeploymentCompleted(DynamicPeripheral dynamicPeripheral, DeploymentResult result) {
        /*
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
        }*/
    }

    private synchronized void submitTask(DeviceTemplateTask task) {
        //Get device template ID
        String deviceTemplateId = task.getDeviceTemplate().getId();

        //Ignore task if ID is invalid
        if ((deviceTemplateId == null) || (deviceTemplateId.isEmpty())) return;

        //Check if there is a task queue for this device template
        if (this.deviceTemplateTasks.containsKey(deviceTemplateId)) {
            //Add task to dedicated queue
            this.deviceTemplateTasks.get(deviceTemplateId).add(new TaskWrapper<>(task));
        }

        //Create new queue and add the task
        LinkedList<TaskWrapper<DeviceTemplateTask>> deviceTemplateQueue = new LinkedList<>();
        deviceTemplateQueue.add(new TaskWrapper<>(task));

        //Add queue to tasks map
        this.deviceTemplateTasks.put(deviceTemplateId, deviceTemplateQueue);

        //Trigger the execution of tasks
        executeTasks();
    }

    //@Scheduled(fixedDelay = 1000)
    private synchronized void executeTasks() {
        /* Rules:
        - Only first task in each queue is executed
        - After the execution of a task concluded, the task is removed from the queue
        - No DP task is started as long as there is a task in the queue for the corresponding device template
        - No DT task is started as long as there is a currently running DP task for a DP that uses the template TODO
        - DT tasks are checked before DP tasks
         */

        /*
        Device template tasks
         */
        //Iterate through all available device template queues
        for (Queue<TaskWrapper<DeviceTemplateTask>> queue : this.deviceTemplateTasks.values()) {
            //Peek first task
            TaskWrapper<? extends DiscoveryTask> firstTask = queue.peek();

            //Null check
            if (firstTask == null) continue;

            //Check if task has already been started
            if (firstTask.isStarted()) continue;

            //Mark task as started
            firstTask.setStarted();

            //Run the task using a completable future and add a completion handler
            CompletableFuture.runAsync(firstTask, executorService)
                    .thenAccept(unused -> handleCompletedTask(firstTask));
        }

        /*
        Dynamic peripheral tasks
         */
        //Iterate through all available dynamic peripheral queues
        for (Queue<TaskWrapper<DynamicPeripheralTask>> queue : this.dynamicPeripheralTasks.values()) {
            //Peek first task
            TaskWrapper<DynamicPeripheralTask> firstTask = queue.peek();

            //Null check
            if (firstTask == null) continue;

            //Check if task has already been started
            if (firstTask.isStarted()) continue;

            //Check if task is blocked due to a device template task with same device template ID
            String deviceTemplateId = firstTask.getTask().getDynamicPeripheral().getDeviceTemplate().getId();
            if ((this.deviceTemplateTasks.containsKey(deviceTemplateId)) && (!this.deviceTemplateTasks.get(deviceTemplateId).isEmpty())) {
                continue;
            }

            //Mark task as started
            firstTask.setStarted();

            //Run the task using a completable future and add a completion handler
            CompletableFuture.runAsync(firstTask, executorService)
                    .thenAccept(unused -> handleCompletedTask(firstTask));
        }
    }

    private synchronized void handleCompletedTask(TaskWrapper<? extends DiscoveryTask> completedTask) {
        //Check if task really completed
        if (!completedTask.isDone()) return;

        //Retrieve actual task from wrapper
        DiscoveryTask task = completedTask.getTask();

        //Check type of the task
        if (task instanceof DeviceTemplateTask) {
            //Get ID of device template
            String deviceTemplateId = ((DeviceTemplateTask) task).getDeviceTemplate().getId();
            //Get corresponding device template queue
            Queue<TaskWrapper<DeviceTemplateTask>> queue = this.deviceTemplateTasks.get(deviceTemplateId);
            //Remove task from the queue
            queue.remove(completedTask);
            //If empty, remove queue from map
            if (queue.isEmpty()) {
                this.deviceTemplateTasks.remove(deviceTemplateId);
            }
        } else if (task instanceof DynamicPeripheralTask) {
            //Get ID of dynamic peripheral
            String dynamicPeripheralId = ((DynamicPeripheralTask) task).getDynamicPeripheral().getId();
            //Get corresponding device template queue
            Queue<TaskWrapper<DynamicPeripheralTask>> queue = this.dynamicPeripheralTasks.get(dynamicPeripheralId);
            //Remove task from the queue
            queue.remove(completedTask);
            //If empty, remove queue from map
            if (queue.isEmpty()) {
                this.dynamicPeripheralTasks.remove(dynamicPeripheralId);
            }
        }

        //Execute next tasks (if available)
        executeTasks();
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

    /**
     * Retrieves and returns a {@link DynamicPeripheral} of a given ID from its repository and updates it enabled
     * status to a given target value. Thereby, it is checked whether the status of the {@link DynamicPeripheral}
     * has already been updated previously to the target value. If this is the case, null will be returned instead of
     * the object. This way, it is ensured that the thread that wants to update the status of the
     * {@link DynamicPeripheral} and succeeds in doing so receives exclusive access to the {@link DynamicPeripheral}
     * for the scope of its enabling/disabling operation, thus avoiding the duplicated execution of operations
     * with the same intention.
     *
     * @param dynamicPeripheralId The ID of the dynamic peripheral to retrieve
     * @param targetEnabledStatus The target status (true for enabled, false for disabled) to set
     * @return The {@link DynamicPeripheral} or null if access could not be granted
     */
    private synchronized DynamicPeripheral requestDynamicPeripheralExclusively(String dynamicPeripheralId, boolean targetEnabledStatus) {
        //Read dynamic peripheral from repository
        Optional<DynamicPeripheral> dynamicPeripheral = this.dynamicPeripheralRepository.findById(dynamicPeripheralId);

        //Check if dynamic peripheral was found
        if (!dynamicPeripheral.isPresent()) {
            throw new IllegalArgumentException("The dynamic peripheral with the given ID does not exist.");
        }

        //Check if target status is already present
        if (dynamicPeripheral.get().isEnabled() == targetEnabledStatus) {
            //Target status is already present, so do not continue
            return null;
        }

        //Set the target status
        dynamicPeripheral.get().setEnabled(targetEnabledStatus);

        //Write updated peripheral to repository and return it
        return this.dynamicPeripheralRepository.save(dynamicPeripheral.get());
    }
}
