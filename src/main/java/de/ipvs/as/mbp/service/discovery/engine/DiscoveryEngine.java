package de.ipvs.as.mbp.service.discovery.engine;

import de.ipvs.as.mbp.domain.discovery.collections.CandidateDevicesCollection;
import de.ipvs.as.mbp.domain.discovery.collections.CandidateDevicesRanking;
import de.ipvs.as.mbp.domain.discovery.collections.CandidateDevicesResult;
import de.ipvs.as.mbp.domain.discovery.description.DeviceDescription;
import de.ipvs.as.mbp.domain.discovery.device.DeviceTemplate;
import de.ipvs.as.mbp.domain.discovery.peripheral.DynamicPeripheral;
import de.ipvs.as.mbp.domain.discovery.topic.RequestTopic;
import de.ipvs.as.mbp.error.MBPException;
import de.ipvs.as.mbp.repository.discovery.DeviceTemplateRepository;
import de.ipvs.as.mbp.repository.discovery.DynamicPeripheralRepository;
import de.ipvs.as.mbp.repository.discovery.RequestTopicRepository;
import de.ipvs.as.mbp.service.discovery.engine.tasks.DiscoveryTask;
import de.ipvs.as.mbp.service.discovery.engine.tasks.TaskWrapper;
import de.ipvs.as.mbp.service.discovery.engine.tasks.dynamic.DeployByRankingTask;
import de.ipvs.as.mbp.service.discovery.engine.tasks.dynamic.DynamicPeripheralTask;
import de.ipvs.as.mbp.service.discovery.engine.tasks.dynamic.UndeployTask;
import de.ipvs.as.mbp.service.discovery.engine.tasks.template.DeleteCandidateDevicesTask;
import de.ipvs.as.mbp.service.discovery.engine.tasks.template.DeviceTemplateTask;
import de.ipvs.as.mbp.service.discovery.engine.tasks.template.MergeCandidateDevicesTask;
import de.ipvs.as.mbp.service.discovery.engine.tasks.template.UpdateCandidateDevicesTask;
import de.ipvs.as.mbp.service.discovery.gateway.CandidateDevicesSubscriber;
import de.ipvs.as.mbp.service.discovery.gateway.DiscoveryGateway;
import de.ipvs.as.mbp.service.discovery.processing.CandidateDevicesProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

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
    private RequestTopicRepository requestTopicRepository;

    @Autowired
    private DynamicPeripheralRepository dynamicPeripheralRepository;

    @Autowired
    private DeviceTemplateRepository deviceTemplateRepository;

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
    public synchronized void initialize() {
        //Iterate over all device templates
        for (DeviceTemplate deviceTemplate : this.deviceTemplateRepository.findAll()) {
            //Find all dynamic peripherals that use the current device template
            List<DynamicPeripheral> dynamicPeripherals = this.dynamicPeripheralRepository.findByDeviceTemplate_Id(deviceTemplate.getId());

            /*
            Device template tasks
             */
            //Check if any of the found dynamic peripherals are intended to be activated
            if (dynamicPeripherals.stream().anyMatch(DynamicPeripheral::isActivatingIntended)) {
                //Such dynamic peripherals exist, so get request topics, update the candidate devices and subscribe
                List<RequestTopic> requestTopics = requestTopicRepository.findByOwner(deviceTemplate.getOwner().getId(), null);
                submitTask(new UpdateCandidateDevicesTask(deviceTemplate, requestTopics, this, true));
            } else {
                //No such dynamic peripherals exist, so deletion of candidate devices and unsubscription is safe
                submitTask(new DeleteCandidateDevicesTask(deviceTemplate, true));
            }

            /*
            Dynamic peripherals tasks
             */
            //Stream through all dynamic peripherals of this device template
            dynamicPeripherals.forEach(dynamicPeripheral -> {
                //Check whether activating or de-activating is intended
                if (dynamicPeripheral.isActivatingIntended()) {
                    //Dynamic peripheral is intended to be activated, so submit corresponding deployment task
                    submitTask(new DeployByRankingTask(dynamicPeripheral));
                } else {
                    //Dynamic peripheral is intended to be deactivated, so submit corresponding un-deployment task
                    submitTask(new UndeployTask(dynamicPeripheral));
                }
            });
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
        CandidateDevicesResult candidateDevices = this.discoveryGateway.getDeviceCandidates(deviceTemplate, requestTopics);

        //Use the processor to filter, aggregate, score and rank the candidate devices
        return candidateDevicesProcessor.process(candidateDevices, deviceTemplate);
    }

    /**
     * Activates the deployment of a {@link DynamicPeripheral}, given by its ID, by placing corresponding tasks
     * in the task queues. For the deployment, the device that appears most appropriate with respect to the
     * {@link DeviceTemplate} underlying the {@link DynamicPeripheral} is used. In case the deployment fails, it is
     * tried again for the next most appropriate appearing devices.
     *
     * @param dynamicPeripheralId The ID of the dynamic peripheral to deploy
     */
    public synchronized void activateDynamicPeripheral(String dynamicPeripheralId) {
        //Get dynamic peripheral exclusively for activating
        DynamicPeripheral dynamicPeripheral = requestDynamicPeripheralExclusively(dynamicPeripheralId, true);

        //Null check
        if (dynamicPeripheral == null) {
            throw new MBPException(HttpStatus.BAD_REQUEST, "The dynamic peripheral is already activated.");
        }

        //Get all request topics of the user
        List<RequestTopic> requestTopics = requestTopicRepository.findByOwner(dynamicPeripheral.getOwner().getId(), null);

        //Submit task for retrieving candidate devices (will abort if not needed due to force=false)
        submitTask(new UpdateCandidateDevicesTask(dynamicPeripheral.getDeviceTemplate(), requestTopics, false));

        //Submit task for deploying the dynamic peripheral
        submitTask(new DeployByRankingTask(dynamicPeripheral));
    }

    /**
     * Deactivates the deployment of a {@link DynamicPeripheral}, given by its ID, by placing corresponding tasks
     * in the task queues. Furthermore, the activation of the {@link DynamicPeripheral} is directly updated to false.
     *
     * @param dynamicPeripheralId The ID of the dynamic peripheral to undeploy
     */
    public synchronized void deactivateDynamicPeripheral(String dynamicPeripheralId) {
        //Get dynamic peripheral exclusively for deactivating
        DynamicPeripheral dynamicPeripheral = requestDynamicPeripheralExclusively(dynamicPeripheralId, false);

        //Null check
        if (dynamicPeripheral == null) {
            throw new MBPException(HttpStatus.BAD_REQUEST, "The dynamic peripheral is already deactivated.");
        }

        //Submit task for potentially deleting candidate devices data and cancel subscriptions
        submitTask(new DeleteCandidateDevicesTask(dynamicPeripheral.getDeviceTemplate()));

        //Submit task for undeploying the dynamic peripheral
        submitTask(new UndeployTask(dynamicPeripheral));
    }

    /**
     * Called in case a notification was received from a repository as result of a subscription,
     * indicating that the collection of suitable candidate devices, which can be determined on behalf of a
     * certain {@link DeviceTemplate}, changed over time.
     *
     * @param deviceTemplate          The device template whose candidate devices are affected
     * @param repositoryName          The name of the repository that issued the notification
     * @param updatedCandidateDevices The updated collection of candidate devices as {@link CandidateDevicesCollection}
     */
    @Override
    public synchronized void onDeviceTemplateResultChanged(DeviceTemplate deviceTemplate, String repositoryName, CandidateDevicesCollection updatedCandidateDevices) {
        //Sanity checks
        if ((deviceTemplate == null) || (repositoryName == null) || (repositoryName.isEmpty()) || (updatedCandidateDevices == null)) {
            return;
        }

        //Create task for merging the updated candidate devices with the existing ones
        submitTask(new MergeCandidateDevicesTask(deviceTemplate, repositoryName, updatedCandidateDevices));

        //Iterate over all dynamic peripherals that use the affected device template
        this.dynamicPeripheralRepository.findByDeviceTemplate_Id(deviceTemplate.getId())
                .forEach(d -> submitTask(new DeployByRankingTask(d))); //Submit re-deployment task for each
    }

    /**
     * Submits a given {@link DeviceTemplateTask} so that it can be added to the corresponding task queue
     * and be scheduled for asynchronous execution.
     *
     * @param task The task to submit
     */
    private synchronized void submitTask(DeviceTemplateTask task) {
        //Delegate call
        this.addTaskToQueueMap(task, this.deviceTemplateTasks, task.getDeviceTemplateId());
    }

    /**
     * Submits a given {@link DynamicPeripheralTask} so that it can be added to the corresponding task queue
     * and be scheduled for asynchronous execution.
     *
     * @param task The task to submit
     */
    private synchronized void submitTask(DynamicPeripheralTask task) {
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

        //TODO print current queues
        printQueues();

        //Trigger the execution of tasks
        executeTasks();
    }

    private synchronized void executeTasks() {
        /* Rules:
        - Only first task in each queue is executed and remains in queue during its execution
        - After the execution of a task concluded, the task is removed from the queue
        - No dynamic peripheral task is started as long as there is a task in the queue for the corresponding device template
        - No device template task is started as long as there is a currently running dynamic peripheral task for a dynamic peripheral that uses the template
        - Device template tasks are checked before dynamic peripheral tasks

        Result: When a new device template task and a dynamic peripheral task are added, old dynamic peripheral tasks
        are executed first, then the new device template task, then the new dynamic peripheral task.
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

    /**
     * Prepares and starts the asynchronous execution of a given {@link DiscoveryTask}, wrapped in a
     * {@link TaskWrapper} object, as {@link CompletableFuture}. After the task concluded, it is automatically
     * removed from its corresponding task queue.
     *
     * @param task The task to execute
     */
    private synchronized void executeTaskAsynchronously(TaskWrapper<? extends DiscoveryTask> task) {
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

    /**
     * Handles a given task, wrapped in a {@link TaskWrapper} object, whose execution completed by removing
     * it from the corresponding task queue.
     *
     * @param completedTask The completed task to handle
     */
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

        //TODO
        System.out.println("Some task execution finished");

        //Execute next tasks (if available)
        executeTasks();
    }

    /**
     * Retrieves and returns a {@link DynamicPeripheral} of a given ID from its repository and updates its activation
     * intention to a given target value. Thereby, it is checked whether the intention of the {@link DynamicPeripheral}
     * has already been updated previously to the target value. If this is the case, null will be returned instead of
     * the object. This way, it is ensured that the thread that wants to update the activation intention of the
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
        if (dynamicPeripheral.get().isActivatingIntended() == enablingIntention) {
            //Target status is already present, so do not continue
            return null;
        }

        //Set the target status
        dynamicPeripheral.get().setActivatingIntended(enablingIntention);

        //Write updated peripheral to repository and return it
        return this.dynamicPeripheralRepository.save(dynamicPeripheral.get());
    }

    /**
     * Prints the contents of the {@link DeviceTemplate} task queues and the {@link DynamicPeripheral}s task queues
     * to the standard output for debugging purposes.
     */
    private synchronized void printQueues() {
        System.out.println("------------------------------------");
        /*
        Device template queues
         */
        System.out.println("Device templates: ");

        //Stream through the device template queues
        this.deviceTemplateTasks.forEach((s, queue) -> {
            //Print device template ID
            System.out.printf("%s: ", s);

            //Stream through the queue elements, get their descriptions and join them
            System.out.println(queue.stream().map(t -> t.getTask().toHumanReadableString()).collect(Collectors.joining(" --> ")));
        });

        /*
        Dynamic peripheral queues
         */
        System.out.println("\nDynamic peripherals: ");

        //Stream through the dynamic peripheral queues
        this.dynamicPeripheralTasks.forEach((s, queue) -> {
            //Print device template ID
            System.out.printf("%s: ", s);

            //Stream through the queue elements, get their descriptions and join them
            System.out.println(queue.stream().map(t -> t.getTask().toHumanReadableString()).collect(Collectors.joining(" --> ")));
        });

        System.out.println("------------------------------------");
    }
}
