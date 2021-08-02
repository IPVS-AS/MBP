package de.ipvs.as.mbp.service.discovery.engine;

import de.ipvs.as.mbp.domain.discovery.collections.CandidateDevicesCollection;
import de.ipvs.as.mbp.domain.discovery.collections.CandidateDevicesContainer;
import de.ipvs.as.mbp.domain.discovery.collections.revision.CandidateDevicesRevision;
import de.ipvs.as.mbp.domain.discovery.collections.revision.operations.RevisionOperation;
import de.ipvs.as.mbp.domain.discovery.deployment.DynamicDeployment;
import de.ipvs.as.mbp.domain.discovery.deployment.log.DiscoveryLog;
import de.ipvs.as.mbp.domain.discovery.device.DeviceTemplate;
import de.ipvs.as.mbp.domain.discovery.topic.RequestTopic;
import de.ipvs.as.mbp.repository.discovery.DeviceTemplateRepository;
import de.ipvs.as.mbp.repository.discovery.DynamicDeploymentRepository;
import de.ipvs.as.mbp.repository.discovery.RequestTopicRepository;
import de.ipvs.as.mbp.service.discovery.engine.tasks.DiscoveryTask;
import de.ipvs.as.mbp.service.discovery.engine.tasks.TaskWrapper;
import de.ipvs.as.mbp.service.discovery.engine.tasks.dynamic.DeployByRankingTask;
import de.ipvs.as.mbp.service.discovery.engine.tasks.dynamic.DynamicDeploymentTask;
import de.ipvs.as.mbp.service.discovery.engine.tasks.dynamic.UndeployTask;
import de.ipvs.as.mbp.service.discovery.engine.tasks.template.CandidateDevicesTask;
import de.ipvs.as.mbp.service.discovery.engine.tasks.template.DeleteCandidateDevicesTask;
import de.ipvs.as.mbp.service.discovery.engine.tasks.template.ReviseCandidateDevicesTask;
import de.ipvs.as.mbp.service.discovery.engine.tasks.template.UpdateCandidateDevicesTask;
import de.ipvs.as.mbp.service.discovery.gateway.CandidateDevicesSubscriber;
import de.ipvs.as.mbp.service.discovery.gateway.DiscoveryGateway;
import de.ipvs.as.mbp.service.discovery.log.DiscoveryLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static de.ipvs.as.mbp.domain.discovery.deployment.log.DiscoveryLogTrigger.*;

/**
 * This components manages the overall discovery process by orchestrating the various involved components and takes
 * care about the execution of discovery-related tasks.
 */
@Component
public class DiscoveryEngine implements ApplicationListener<ContextRefreshedEvent>, CandidateDevicesSubscriber {

    //Number of threads to use in the thread pool that executes the tasks
    private static final int THREAD_POOL_SIZE = 5;

    /*
    Auto-wired components
     */
    @Autowired
    private DiscoveryLogService discoveryLoggingService;

    @Autowired
    private DiscoveryGateway discoveryGateway;

    @Autowired
    private DynamicDeploymentRepository dynamicDeploymentRepository;

    @Autowired
    private DeviceTemplateRepository deviceTemplateRepository;

    @Autowired
    private RequestTopicRepository requestTopicRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    //Map (device template ID --> Queue) of task queues for candidate devices tasks
    private final Map<String, Queue<TaskWrapper<CandidateDevicesTask>>> candidateDevicesTasks;

    //Map (dynamic deployment ID --> Queue) of task queues for dynamic deployment tasks
    private final Map<String, Queue<TaskWrapper<DynamicDeploymentTask>>> dynamicDeploymentTasks;

    //Executor service for executing tasks
    private final ExecutorService executorService;

    /**
     * Creates the discovery engine.
     */
    public DiscoveryEngine() {
        //Initialize data structures
        this.dynamicDeploymentTasks = new HashMap<>();
        this.candidateDevicesTasks = new HashMap<>();

        //Initialize executor service
        this.executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    }

    /**
     * Initializes the discovery engine.
     */
    @PostConstruct
    public void initialize() {
        /*
        Do not interact with the MongoDB here in different threads:
        https://github.com/spring-projects/spring-data-mongodb/issues/2452
         */
    }

    /**
     * Called as soon as the {@link ContextRefreshedEvent} is sent by the application. This method then takes care
     * of initializing the {@link DiscoveryEngine} by retrieving {@link DeviceTemplate}s and the corresponding
     * {@link DynamicDeployment}s that make use of them and checking and updating their status to the current
     * consitution of the IoT environment.
     *
     * @param event The {@link ContextRefreshedEvent} to wait for
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        //Iterate over all device templates
        for (DeviceTemplate deviceTemplate : this.deviceTemplateRepository.findAll()) {
            //Find all dynamic deployments that use the current device template
            List<DynamicDeployment> dynamicDeployments = this.dynamicDeploymentRepository.findByDeviceTemplate_Id(deviceTemplate.getId());

            /*
            Candidate devices tasks
             */
            //Check if any of the found dynamic deployments are intended to be activated
            if (dynamicDeployments.stream().anyMatch(DynamicDeployment::isActivatingIntended)) {
                //Such dynamic deployments exist, so update the candidate devices and subscribe
                submitTask(new UpdateCandidateDevicesTask(deviceTemplate, this, true, new DiscoveryLog(MBP, "Update candidate devices")));
            } else {
                //No such dynamic deployments exist, so deletion of candidate devices and unsubscription is safe
                submitTask(new DeleteCandidateDevicesTask(deviceTemplate, true, new DiscoveryLog(MBP, "Delete candidate devices")));
            }

            /*
            Dynamic deployments tasks
             */
            //Stream through all dynamic deployments of this device template
            dynamicDeployments.forEach(dynamicDeployment -> {
                //Check whether activating or de-activating is intended
                if (dynamicDeployment.isActivatingIntended()) {
                    //Dynamic deployment is intended to be activated, so submit corresponding deployment task
                    submitTask(new DeployByRankingTask(dynamicDeployment, true,
                            new DiscoveryLog(MBP, "Deploy on startup")));
                } else {
                    //Dynamic deployment is intended to be deactivated, so submit corresponding un-deployment task
                    submitTask(new UndeployTask(dynamicDeployment,
                            new DiscoveryLog(MBP, "Undeploy on startup")));
                }
            });
        }

        //Trigger the execution of tasks
        executeTasks();
    }

    /**
     * Returns whether operations for a certain {@link DynamicDeployment}, given by its ID, are currently in progress.
     * This is determined by checking whether a non-empty task queue exists for the {@link DynamicDeployment}.
     *
     * @param dynamicDeploymentId The ID of the dynamic deployment to check
     * @return True, if operations are in progress; false otherwise
     */
    public boolean isDynamicDeploymentInProgress(String dynamicDeploymentId) {
        //Sanity check
        if ((dynamicDeploymentId == null) || dynamicDeploymentId.isEmpty()) {
            return false;
        }

        //Check if a queue exists for the dynamic deployment
        if (!this.dynamicDeploymentTasks.containsKey(dynamicDeploymentId)) {
            return false;
        }

        //Get queue for the dynamic deployment
        Queue<TaskWrapper<DynamicDeploymentTask>> queue = this.dynamicDeploymentTasks.getOrDefault(dynamicDeploymentId, null);

        //Check if the queue is valid and contains tasks
        return (queue != null) && (!queue.isEmpty());
    }

    /**
     * Activates the deployment of a {@link DynamicDeployment}, given by its ID, by placing corresponding tasks
     * in the task queues. For the deployment, the device that appears most appropriate with respect to the
     * {@link DeviceTemplate} underlying the {@link DynamicDeployment} is used. In case the deployment fails, it is
     * tried again for the next most appropriate appearing devices.
     *
     * @param dynamicDeploymentId The ID of the dynamic deployment to deploy
     * @return True, if the activation succeeded; false, if the dynamic deployment was already activated
     */
    public synchronized boolean activateDynamicDeployment(String dynamicDeploymentId) {
        //Get dynamic deployment exclusively for activating
        DynamicDeployment dynamicDeployment = requestDynamicDeploymentExclusively(dynamicDeploymentId, true);

        //Null check
        if (dynamicDeployment == null) {
            return false;
        }

        //Submit task for retrieving candidate devices (will abort if not needed due to force=false)
        submitTask(new UpdateCandidateDevicesTask(dynamicDeployment.getDeviceTemplate(), this, false, new DiscoveryLog(USER, "Update candidate devices")));

        //Submit task for deploying the dynamic deployment
        submitTask(new DeployByRankingTask(dynamicDeployment, true, new DiscoveryLog(USER, "Activate deployment")));

        //Trigger the execution of tasks
        executeTasks();

        return true;
    }

    /**
     * Deactivates the deployment of a {@link DynamicDeployment}, given by its ID, by placing corresponding tasks
     * in the task queues. Furthermore, the activation of the {@link DynamicDeployment} is directly updated to false.
     *
     * @param dynamicDeploymentId The ID of the dynamic deployment to undeploy
     * @return True, if the deactivation succeeded; false, if the dynamic deployment was already deactivated
     */
    public synchronized boolean deactivateDynamicDeployment(String dynamicDeploymentId) {
        //Get dynamic deployment exclusively for deactivating
        DynamicDeployment dynamicDeployment = requestDynamicDeploymentExclusively(dynamicDeploymentId, false);

        //Null check
        if (dynamicDeployment == null) {
            return false;
        }

        //Submit task for undeploying the dynamic deployment
        submitTask(new UndeployTask(dynamicDeployment, new DiscoveryLog(USER, "Deactivate deployment")));

        //Trigger the execution of tasks
        executeTasks();

        return true;
    }

    /**
     * Deletes a certain {@link DynamicDeployment}, given by its ID, safely. This way, it is ensured that no deployment
     * remains on a former device. Before the deletion is actually executed, pre-conditions like no remaining tasks
     * and no activating intention are checked for the pertaining {@link DynamicDeployment}. If they are violated,
     * a corresponding {@link IllegalStateException} will be thrown.
     *
     * @param dynamicDeploymentId The ID of the {@link DynamicDeployment} to delete
     */
    public synchronized void deleteDynamicDeployment(String dynamicDeploymentId) {
        //Check whether operations are in progress for this dynamic deployment
        if (isDynamicDeploymentInProgress(dynamicDeploymentId))
            throw new IllegalStateException("Cannot delete, because operations for the dynamic deployment are currently in progress.");

        //Retrieve dynamic deployment from its repository
        DynamicDeployment dynamicDeployment = this.dynamicDeploymentRepository.findById(dynamicDeploymentId).orElse(null);

        //Check if dynamic deployment was found and if it is already flagged for deletion
        if (dynamicDeployment == null)
            throw new IllegalStateException("The dynamic deployment does not exist.");

        //Check activating intention
        if (dynamicDeployment.isActivatingIntended())
            throw new IllegalStateException("The dynamic deployment must be disabled before it can be deleted.");

        /*
        Due to synchronized, we know that no deployment of the operator exists anymore, since the activation intention
        is set to false and no tasks are in its queue. Therefore, a deletion at this point is safe.
         */

        //Delete the dynamic deployment from its repository
        this.dynamicDeploymentRepository.deleteById(dynamicDeploymentId);

        //Check whether dynamic deployments remain that use the same device template
        if (!this.dynamicDeploymentRepository.findByDeviceTemplate_Id(dynamicDeployment.getDeviceTemplate().getId()).isEmpty()) {
            return;
        }

        //Submit task for deleting the candidate devices and cancelling the subscriptions
        submitTask(new DeleteCandidateDevicesTask(dynamicDeployment.getDeviceTemplate(), false, new DiscoveryLog(USER, "Delete candidate devices")));

        //Trigger the execution of tasks
        executeTasks();
    }

    /**
     * Deletes a certain {@link RequestTopic}, given by its ID, safely. For this, it is ensured that no
     * {@link UpdateCandidateDevicesTask}s are currently in the task queues that could make use of
     * the {@link RequestTopic} to delete and thus re-create the subscriptions at the discovery repositories
     * by accident.
     *
     * @param requestTopicId The ID of the request topic that is supposed to be deleted
     */
    public synchronized void deleteRequestTopic(String requestTopicId) {
        //Read request topic from repository
        RequestTopic requestTopic = this.requestTopicRepository.findById(requestTopicId).orElse(null);

        //Check if request topic could be found
        if (requestTopic == null) throw new IllegalStateException("The request topic does not exist.");

        //Retrieve the device templates of the same user
        List<DeviceTemplate> affectedDeviceTemplates = this.deviceTemplateRepository.findByOwner(requestTopic.getOwner().getId(), null);

        //Ensure that no update task is in progress for any of those device templates
        if (affectedDeviceTemplates.stream().map(d -> this.candidateDevicesTasks.containsKey(d.getId()) ? this.candidateDevicesTasks.get(d.getId()).peek() : null).anyMatch(t -> (t != null) && (t.getTask() instanceof UpdateCandidateDevicesTask))) {
            throw new IllegalStateException("Cannot delete, because update operations are in progress for at least one of the device templates that use this request topic.");
        }

        /*
        At this point it is clear that no update tasks are in execution for the affected device templates. Due to
        synchronized, also no further update tasks will be queued or executed during the remainder of this method.
        For this reason, the request topic can be safely deleted from the repository, since all update tasks that will
        be executed in the future will read the request topics fresh from the repository and thus already use
        the updated set of request topics. This way, the accidential re-creation of subscriptions at the discovery
        repositories is avoided.
         */

        //Delete the request topic from the repository
        this.requestTopicRepository.deleteById(requestTopicId);

        //Cancel existing subscriptions for this request topic
        discoveryGateway.cancelSubscriptionsForRequestTopic(affectedDeviceTemplates, requestTopic);
    }


    /**
     * Submits a task for updating the candidate devices that are stored as {@link CandidateDevicesContainer} for a
     * given {@link DeviceTemplate} and also the corresponding subscriptions at the discovery repositories.
     * All modifications are forced and thus do not abort on failing pre-conditions.
     *
     * @param deviceTemplate The device template
     */
    public void refreshCandidateDevicesAndSubscriptions(DeviceTemplate deviceTemplate) {
        //Null check
        if (deviceTemplate == null) throw new IllegalArgumentException("The device template must not be null.");

        //Submit task for updating the candidate devices
        submitTask(new UpdateCandidateDevicesTask(deviceTemplate, true, new DiscoveryLog(USER, "Update candidate devices")));

        //Trigger the execution of tasks
        executeTasks();
    }

    /**
     * Called in case a notification was received from a discovery repository as result of a subscription,
     * indicating that the corresponding {@link CandidateDevicesCollection} of a certain {@link DeviceTemplate}
     * changed over time. Thereby, a {@link CandidateDevicesRevision} is provided, containing the
     * {@link RevisionOperation}s that can be executed on the {@link CandidateDevicesCollection} in order to update
     * it again.
     *
     * @param deviceTemplateId The ID of the {@link DeviceTemplate} whose candidate devices are affected
     * @param repositoryName   The name of the repository that issued the notification
     * @param revision         The {@link CandidateDevicesRevision} that allows to update the candidate devices again
     */
    @Override
    public synchronized void onCandidateDevicesChanged(String deviceTemplateId, String repositoryName, CandidateDevicesRevision revision) {
        //Sanity checks
        if ((deviceTemplateId == null) || (deviceTemplateId.isEmpty()) ||
                (repositoryName == null) || (repositoryName.isEmpty()) || (revision == null)) return;

        //Create task for revising the candidate devices
        submitTask(new ReviseCandidateDevicesTask(deviceTemplateId, repositoryName, revision, new DiscoveryLog(DISCOVERY_REPOSITORY, "Revise candidate devices")));

        //Iterate over all dynamic deployments that use the affected device template
        this.dynamicDeploymentRepository.findByDeviceTemplate_Id(deviceTemplateId)
                .forEach(d -> submitTask(new DeployByRankingTask(d, new DiscoveryLog(DISCOVERY_REPOSITORY, "Re-evaluate deployment"))));

        //Trigger the execution of tasks
        executeTasks();
    }

    /**
     * Submits a given {@link CandidateDevicesTask} so that it can be added to the corresponding task queue
     * and be scheduled for asynchronous execution.
     *
     * @param task The task to submit
     */
    private synchronized void submitTask(CandidateDevicesTask task) {
        //Null check
        if (task == null) {
            throw new IllegalArgumentException("The task must not be null.");
        }

        //Get device template ID
        String deviceTemplateId = task.getDeviceTemplateId();

        //Check if there is already a queue with this ID
        if (!candidateDevicesTasks.containsKey(deviceTemplateId)) {
            //Create new queue and add the task
            LinkedList<TaskWrapper<CandidateDevicesTask>> newQueue = new LinkedList<>();
            newQueue.add(new TaskWrapper<>(task));

            //Add queue to queue map
            candidateDevicesTasks.put(deviceTemplateId, newQueue);
            return;
        }

        //Add task to dedicated queue
        candidateDevicesTasks.get(deviceTemplateId).add(new TaskWrapper<>(task));
    }

    /**
     * Submits a given {@link DynamicDeploymentTask} so that it can be added to the corresponding task queue
     * and be scheduled for asynchronous execution.
     *
     * @param task The task to submit
     */
    private synchronized void submitTask(DynamicDeploymentTask task) {
        //Null check
        if (task == null) {
            throw new IllegalArgumentException("The task must not be null.");
        }

        //Get ID of the dynamic deployment
        String dynamicDeploymentId = task.getDynamicDeploymentId();

        //Check if there is already a queue with this ID
        if (!dynamicDeploymentTasks.containsKey(dynamicDeploymentId)) {
            //Create new queue and add the task
            LinkedList<TaskWrapper<DynamicDeploymentTask>> newQueue = new LinkedList<>();
            newQueue.add(new TaskWrapper<>(task));

            //Add queue to queue map
            dynamicDeploymentTasks.put(dynamicDeploymentId, newQueue);
            return;
        }

        //Get queue for the dynamic deployment
        Queue<TaskWrapper<DynamicDeploymentTask>> queue = dynamicDeploymentTasks.get(dynamicDeploymentId);

        /* Optimization:
        ---------------------
        The following block tries to optimize the queue by removing redundant tasks. This approach keeps the effective
        queue size at a maximum of 2, including the currently running task and the next one.
         */

        //Iterate over the queue using an iterator
        Iterator<TaskWrapper<DynamicDeploymentTask>> queueIterator = queue.iterator();
        while (queueIterator.hasNext()) {
            //Get current queue task
            TaskWrapper<DynamicDeploymentTask> taskWrapper = queueIterator.next();

            //Check if task is running
            if (taskWrapper.isStarted()) continue;

            //Check if queued task and new one are redundant
            if (task.mayReplace()) {
                //New task is created by user, so replace current one
                queueIterator.remove();
                break;
            }

            //New task is not user created and since there is a previous task (undeploy or deploy), we do not need it
            return;
        }

        //Just add the task to the queue
        queue.add(new TaskWrapper<>(task));
    }

    private synchronized void executeTasks() {
        /* Rules:
        - Only first task in each queue is executed and remains in queue during its execution
        - After the execution of a task concluded, the task is removed from the queue
        - No dynamic deployment task that depends on candidate devices is started as long as there is a task in the queue for the corresponding device template
        - No candidate devices task is started as long as there is a currently running dynamic deployment task depending on candidate devices for a dynamic deployment that uses the template
        - Candidate devices tasks are checked before dynamic deployment tasks

        Result: When a new candidate devices task and a dynamic deployment task are added, old dynamic deployment tasks
        are executed first, then the new candidate devices task, then the new dynamic deployment task.
         */

        /*
        Candidate devices tasks
         */
        //Iterate through all available device template queues
        for (Queue<TaskWrapper<CandidateDevicesTask>> queue : this.candidateDevicesTasks.values()) {
            //Peek first task
            TaskWrapper<CandidateDevicesTask> firstTask = queue.peek();

            //Null check
            if (firstTask == null) continue;

            //Check if task has already been started
            if (firstTask.isStarted()) continue;

            //Get device template ID of the first task
            String deviceTemplateId = firstTask.getTask().getDeviceTemplateId();

            //Check if there is a currently running task for a dynamic deployment that uses the same device template
            if ((deviceTemplateId == null) || this.dynamicDeploymentTasks.values().stream()
                    .flatMap(Collection::stream)
                    .filter(TaskWrapper::isStarted) //Filter for currently running tasks
                    .filter(x -> x.getTask().dependsOnCandidateDevices()) //Filter for tasks depending on candidate devices
                    .anyMatch(x -> deviceTemplateId.equals(x.getTask().getDeviceTemplateId()))) {
                continue;
            }

            //Task passed all checks, so execute it asynchronously
            executeTaskAsynchronously(firstTask);
        }

        /*
        Dynamic deployment tasks
         */
        //Iterate through all available dynamic deployment queues
        for (Queue<TaskWrapper<DynamicDeploymentTask>> queue : this.dynamicDeploymentTasks.values()) {
            //Peek first task
            TaskWrapper<DynamicDeploymentTask> firstTask = queue.peek();

            //Null check
            if (firstTask == null) continue;

            //Check if task has already been started
            if (firstTask.isStarted()) continue;

            //Get task object and device template ID
            DynamicDeploymentTask taskObject = firstTask.getTask();
            String deviceTemplateId = taskObject.getDeviceTemplateId();

            //Check if task is blocked due to a candidate devices task with same device template ID
            if ((taskObject.dependsOnCandidateDevices()) && (this.candidateDevicesTasks.containsKey(deviceTemplateId)) &&
                    (!this.candidateDevicesTasks.get(deviceTemplateId).isEmpty())) {
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

        //Check for exceptional completion
        try {
            completedTask.get();
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Task " + completedTask.getTask().getClass().getSimpleName() + " completed exceptionally.");
        }

        //Retrieve actual task from wrapper
        DiscoveryTask task = completedTask.getTask();

        //Check type of the task
        if (task instanceof CandidateDevicesTask) {
            //Get ID of device template
            String deviceTemplateId = ((CandidateDevicesTask) task).getDeviceTemplateId();
            //Get corresponding device template queue
            Queue<TaskWrapper<CandidateDevicesTask>> queue = this.candidateDevicesTasks.get(deviceTemplateId);
            //Remove task from the queue
            queue.remove(completedTask);
            //If empty, remove queue from map
            if (queue.isEmpty()) {
                this.candidateDevicesTasks.remove(deviceTemplateId);
            }
        } else if (task instanceof DynamicDeploymentTask) {
            //Get ID of dynamic deployment
            String dynamicDeploymentId = ((DynamicDeploymentTask) task).getDynamicDeploymentId();
            //Get corresponding device template queue
            Queue<TaskWrapper<DynamicDeploymentTask>> queue = this.dynamicDeploymentTasks.get(dynamicDeploymentId);
            //Remove task from the queue
            queue.remove(completedTask);
            //If empty, remove queue from map
            if (queue.isEmpty()) {
                this.dynamicDeploymentTasks.remove(dynamicDeploymentId);
            }
        }

        //Save logs of the task (if available)
        writeTaskLogs(task);

        //Execute next tasks (if available)
        executeTasks();
    }

    /**
     * Extracts the {@link DiscoveryLog} (if available) from a given {@link DiscoveryTask} and adds it to the
     * {@link DiscoveryLog}s of the pertaining {@link DynamicDeployment}s.
     *
     * @param task The task from which the {@link DiscoveryLog} is supposed to be processed
     */
    private void writeTaskLogs(DiscoveryTask task) {
        //Null check
        if (task == null) return;

        //Retrieve discovery log from the task
        DiscoveryLog discoveryLog = task.getDiscoveryLog();

        //Check if logs are available
        if ((discoveryLog == null) || (discoveryLog.isEmpty())) return;

        //Update the end timestamp of the log
        discoveryLog.updateEndTimestamp();

        //Check type of task
        if (task instanceof CandidateDevicesTask) {
            //Get ID of affected device template
            String deviceTemplateId = ((CandidateDevicesTask) task).getDeviceTemplateId();

            //Save the log entry for all dynamic deployments that use the device template
            dynamicDeploymentRepository.findByDeviceTemplate_Id(deviceTemplateId).stream()
                    .map(DynamicDeployment::getId).forEach(id -> discoveryLoggingService.writeDiscoveryLog(id, discoveryLog));
        } else if (task instanceof DynamicDeploymentTask) {
            //Get ID of affected dynamic deployment and save the log entry
            discoveryLoggingService.writeDiscoveryLog(((DynamicDeploymentTask) task).getDynamicDeploymentId(), discoveryLog);
        }
    }

    /**
     * Retrieves and returns a {@link DynamicDeployment} of a given ID from its repository and updates its activation
     * intention to a given target value. Thereby, it is checked whether the intention of the {@link DynamicDeployment}
     * has already been updated previously to the target value and whether the deletion flag is set. If one of both is
     * the case, null will be returned instead of the object. This way, it is ensured that the thread that wants to
     * update the activation intention of the {@link DynamicDeployment} and succeeds in doing so receives exclusive
     * access to the {@link DynamicDeployment} for the scope of its operation, thus avoiding the duplicated execution
     * of operations with the same intention or operations on entities that are supposed to be deleted.
     *
     * @param dynamicDeploymentId The ID of the dynamic deployment to retrieve
     * @param activatingIntention The target intention to set where true means active and false inactive
     * @return The {@link DynamicDeployment} or null if access could not be granted
     */
    private synchronized DynamicDeployment requestDynamicDeploymentExclusively(String dynamicDeploymentId, boolean activatingIntention) {
        //Retrieve dynamic deployment from repository
        DynamicDeployment dynamicDeployment = this.dynamicDeploymentRepository.findById(dynamicDeploymentId).orElse(null);

        //Check if dynamic deployment was found
        if (dynamicDeployment == null) {
            throw new IllegalArgumentException("The dynamic deployment with the given ID does not exist.");
        }

        //Check if target status is already present
        if (dynamicDeployment.isActivatingIntended() == activatingIntention) return null;

        //Write the target status
        mongoTemplate.updateFirst(Query.query(Criteria.where("id").is(dynamicDeploymentId)),
                new Update().set("activatingIntended", activatingIntention), DynamicDeployment.class);
        return dynamicDeployment.setActivatingIntended(activatingIntention);
    }

    /**
     * Prints the contents of the {@link DeviceTemplate} task queues and the {@link DynamicDeployment}s task queues
     * to the standard output for debugging purposes.
     */
    private synchronized void printQueues() {
        System.out.println("------------------------------------");
        /*
        Device template queues
         */
        System.out.println("Device templates: ");

        //Stream through the device template queues
        this.candidateDevicesTasks.forEach((s, queue) -> {
            //Print device template ID
            System.out.printf("%s: ", s);

            //Stream through the queue elements, get their descriptions and join them
            System.out.println(queue.stream().map(t -> t.getTask().toHumanReadableString() + (t.isStarted() ? " (running)" : "")).collect(Collectors.joining(" --> ")));
        });

        /*
        Dynamic deployment queues
         */
        System.out.println("\nDynamic deployments: ");

        //Stream through the dynamic deployment queues
        this.dynamicDeploymentTasks.forEach((s, queue) -> {
            //Print device template ID
            System.out.printf("%s: ", s);

            //Stream through the queue elements, get their descriptions and join them
            System.out.println(queue.stream().map(t -> t.getTask().toHumanReadableString()).collect(Collectors.joining(" --> ")));
        });

        System.out.println("------------------------------------");
    }
}
