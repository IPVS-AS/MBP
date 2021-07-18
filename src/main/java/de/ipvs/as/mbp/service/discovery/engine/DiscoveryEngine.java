package de.ipvs.as.mbp.service.discovery.engine;

import de.ipvs.as.mbp.domain.discovery.collections.CandidateDevicesCollection;
import de.ipvs.as.mbp.domain.discovery.collections.CandidateDevicesRanking;
import de.ipvs.as.mbp.domain.discovery.collections.CandidateDevicesResult;
import de.ipvs.as.mbp.domain.discovery.description.DeviceDescription;
import de.ipvs.as.mbp.domain.discovery.device.DeviceTemplate;
import de.ipvs.as.mbp.domain.discovery.peripheral.DynamicPeripheral;
import de.ipvs.as.mbp.domain.discovery.topic.RequestTopic;
import de.ipvs.as.mbp.repository.discovery.CandidateDevicesRepository;
import de.ipvs.as.mbp.repository.discovery.DynamicPeripheralRepository;
import de.ipvs.as.mbp.repository.discovery.RequestTopicRepository;
import de.ipvs.as.mbp.service.discovery.deployment.DiscoveryDeploymentService;
import de.ipvs.as.mbp.service.discovery.engine.tasks.DiscoveryTask;
import de.ipvs.as.mbp.service.discovery.engine.tasks.TaskWrapper;
import de.ipvs.as.mbp.service.discovery.engine.tasks.dynamic.DeployByRankingTask;
import de.ipvs.as.mbp.service.discovery.engine.tasks.dynamic.DynamicPeripheralTask;
import de.ipvs.as.mbp.service.discovery.engine.tasks.dynamic.UndeployTask;
import de.ipvs.as.mbp.service.discovery.engine.tasks.template.DeleteCandidateDevicesTask;
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
public class DiscoveryEngine implements CandidateDevicesSubscriber {

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
    private DiscoveryDeploymentService discoveryDeploymentService;

    @Autowired
    private RequestTopicRepository requestTopicRepository;

    @Autowired
    private DynamicPeripheralRepository dynamicPeripheralRepository;

    @Autowired
    private CandidateDevicesRepository candidateDevicesRepository;

    //Map (device template ID --> Queue) of task queues for device templates
    private final Map<String, Queue<TaskWrapper<DeviceTemplateTask>>> deviceTemplateTasks;

    //Map (dynamic peripheral ID --> Queue) of task queues for dynamic peripherals
    private final Map<String, Queue<TaskWrapper<DynamicPeripheralTask>>> dynamicPeripheralTasks;

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

    public void activateDynamicPeripheral(String dynamicPeripheralId) {
        //Get dynamic peripheral exclusively for activating
        DynamicPeripheral dynamicPeripheral = requestDynamicPeripheralExclusively(dynamicPeripheralId, true);

        //Null check
        if (dynamicPeripheral == null) return;

        //Get all request topics of the user
        List<RequestTopic> requestTopics = requestTopicRepository.findByOwner(dynamicPeripheral.getOwner().getId(), null);

        //Submit task for retrieving candidate devices (will abort if not needed due to force=false)
        submitTask(new UpdateCandidateDevicesTask(dynamicPeripheral.getDeviceTemplate(), requestTopics, false));

        //Submit task for deploying the dynamic peripheral
        submitTask(new DeployByRankingTask(dynamicPeripheral));
    }

    public void deactivateDynamicPeripheral(String dynamicPeripheralId) {
        //Get dynamic peripheral exclusively for deactivating
        DynamicPeripheral dynamicPeripheral = requestDynamicPeripheralExclusively(dynamicPeripheralId, false);

        //Null check
        if (dynamicPeripheral == null) return;

        //Submit task for potentially deleting candidate devices data and cancel subscriptions
        submitTask(new DeleteCandidateDevicesTask(dynamicPeripheral.getDeviceTemplate()));

        //Submit task for undeploying the dynamic peripheral
        submitTask(new UndeployTask(dynamicPeripheral));
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
        //TODO with the new task-based system, this can be simplified: Just create and submit two tasks: One DT task that
        //TODO Updates the candidate devices in the repo and then the DeployByRanking task that deals with the (re-)deployment by using the new data
    }

    /**
     * Submits a given {@link DeviceTemplateTask} so that it can be added to the corresponding task queue
     * and be scheduled for asynchronous execution.
     *
     * @param task The task to submit
     */
    private void submitTask(DeviceTemplateTask task) {
        //Delegate call
        this.addTaskToQueueMap(task, this.deviceTemplateTasks, task.getDeviceTemplateId());
    }

    /**
     * Submits a given {@link DynamicPeripheralTask} so that it can be added to the corresponding task queue
     * and be scheduled for asynchronous execution.
     *
     * @param task The task to submit
     */
    private void submitTask(DynamicPeripheralTask task) {
        //Delegate call
        this.addTaskToQueueMap(task, this.dynamicPeripheralTasks, task.getDynamicPeripheralId());
    }

    /**
     * Adds a given {@link DiscoveryTask} to a given {@link Map} (queue ID --> queue) of {@link Queue}s. In order to
     * identify the queue that matches the given task, a queue ID is provided. If the queue does not already exist
     * in the queue map, it will be created and added.
     *
     * @param task     The task to add
     * @param queueMap The queue map (queue ID --> queue) to which the task is supposed to be added
     * @param queueId  The ID of the queue that matches the task
     * @param <T>      The type of the task
     */
    private synchronized <T extends DiscoveryTask> void addTaskToQueueMap(T task, Map<String, Queue<TaskWrapper<T>>> queueMap, String queueId) {
        //Null check
        if (task == null) {
            throw new IllegalArgumentException("The task must not be null.");
        }

        //Ignore task if ID is invalid
        if ((queueId == null) || (queueId.isEmpty())) return;

        //Check if there is already a queue with this ID
        if (queueMap.containsKey(queueId)) {
            //Add task to dedicated queue
            queueMap.get(queueId).add(new TaskWrapper<>(task));
        } else {
            //Create new queue and add the task
            LinkedList<TaskWrapper<T>> newQueue = new LinkedList<>();
            newQueue.add(new TaskWrapper<>(task));

            //Add queue to queue map
            queueMap.put(queueId, newQueue);
        }

        //Trigger the execution of tasks
        executeTasks();
    }

    //@Scheduled(fixedDelay = 1000)
    private synchronized void executeTasks() {
        /* Rules:
        - Only first task in each queue is executed and remains in queue during its execution
        - After the execution of a task concluded, the task is removed from the queue
        - No DP task is started as long as there is a task in the queue for the corresponding device template
        - No DT task is started as long as there is a currently running DP task for a DP that uses the template
        - DT tasks are checked before DP tasks

        Result: When a new DT task and a DP task are added, old DP tasks are first executed, then the new DT
        task, then the new DP task.
         */

        /*
        Device template tasks
         */
        //Iterate through all available device template queues
        for (Queue<TaskWrapper<DeviceTemplateTask>> queue : this.deviceTemplateTasks.values()) {
            //Peek first task
            TaskWrapper<DeviceTemplateTask> firstTask = queue.peek();

            //Null check
            if (firstTask == null) continue;

            //Check if task has already been started
            if (firstTask.isStarted()) continue;

            //Get device template ID of the first task
            String deviceTemplateId = firstTask.getTask().getDeviceTemplateId();

            //Check if there is a currently running task for a dynamic peripheral that uses the same device template
            if ((deviceTemplateId == null) || this.dynamicPeripheralTasks.values().stream()
                    .flatMap(Collection::stream).filter(TaskWrapper::isStarted)
                    .anyMatch(x -> deviceTemplateId.equals(x.getTask().getDeviceTemplateId()))) {
                continue;
            }

            //Task passed all checks, so execute it asynchronously
            executeTaskAsynchronously(firstTask);
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
            String deviceTemplateId = firstTask.getTask().getDeviceTemplateId();
            if ((this.deviceTemplateTasks.containsKey(deviceTemplateId)) && (!this.deviceTemplateTasks.get(deviceTemplateId).isEmpty())) {
                continue;
            }

            //Task passed all checks, so execute it asynchronously
            executeTaskAsynchronously(firstTask);
        }
    }

    private void executeTaskAsynchronously(TaskWrapper<? extends DiscoveryTask> task) {
        //Sanity check
        if (task == null) {
            return;
        }

        //Mark task as started
        task.setStarted();

        //Run the task using a completable future and add a completion handler
        CompletableFuture.runAsync(task, executorService)
                .thenAccept(unused -> handleCompletedTask(task));
    }

    private synchronized void handleCompletedTask(TaskWrapper<? extends DiscoveryTask> completedTask) {
        //Check if task really completed
        if (!completedTask.isDone()) return;

        //Retrieve actual task from wrapper
        DiscoveryTask task = completedTask.getTask();

        //Check type of the task
        if (task instanceof DeviceTemplateTask) {
            //Get ID of device template
            String deviceTemplateId = ((DeviceTemplateTask) task).getDeviceTemplateId();
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
            String dynamicPeripheralId = ((DynamicPeripheralTask) task).getDynamicPeripheralId();
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
        CandidateDevicesResult candidateDevices = this.discoveryGateway.getDeviceCandidates(deviceTemplate, requestTopics);

        //Use the processor to filter, aggregate, score and rank the candidate devices
        return candidateDevicesProcessor.process(candidateDevices, deviceTemplate);
    }

    /**
     * Retrieves and returns a {@link DynamicPeripheral} of a given ID from its repository and updates its user
     * intention to a given target value. Thereby, it is checked whether the intention of the {@link DynamicPeripheral}
     * has already been updated previously to the target value. If this is the case, null will be returned instead of
     * the object. This way, it is ensured that the thread that wants to update the user intention of the
     * {@link DynamicPeripheral} and succeeds in doing so receives exclusive access to the {@link DynamicPeripheral}
     * for the scope of its operation, thus avoiding the duplicated execution of operations with the same intention.
     *
     * @param dynamicPeripheralId The ID of the dynamic peripheral to retrieve
     * @param enablingIntention   The target intention to set where true means active and false inactive
     * @return The {@link DynamicPeripheral} or null if access could not be granted
     */
    private synchronized DynamicPeripheral requestDynamicPeripheralExclusively(String dynamicPeripheralId, boolean enablingIntention) {
        //Read dynamic peripheral from repository
        Optional<DynamicPeripheral> dynamicPeripheral = this.dynamicPeripheralRepository.findById(dynamicPeripheralId);

        //Check if dynamic peripheral was found
        if (!dynamicPeripheral.isPresent()) {
            throw new IllegalArgumentException("The dynamic peripheral with the given ID does not exist.");
        }

        //Check if target status is already present
        if (dynamicPeripheral.get().isActiveIntended() == enablingIntention) {
            //Target status is already present, so do not continue
            return null;
        }

        //Set the target status
        dynamicPeripheral.get().setEnablingIntended(enablingIntention);

        //Write updated peripheral to repository and return it
        return this.dynamicPeripheralRepository.save(dynamicPeripheral.get());
    }
}
