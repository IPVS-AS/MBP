package de.ipvs.as.mbp.service.discovery.deployment;

import de.ipvs.as.mbp.domain.discovery.collections.CandidateDevicesRanking;
import de.ipvs.as.mbp.domain.discovery.peripheral.DynamicPeripheral;
import de.ipvs.as.mbp.service.deployment.DeployerDispatcher;
import de.ipvs.as.mbp.service.deployment.IDeployer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class DiscoveryDeploymentExecutor {

    //Number of threads to use in the thread pool that executes deployment tasks
    private static final int THREAD_POOL_SIZE = 5;

    /**
     * Enumeration of possible results of deployment tasks.
     */
    public enum DeploymentResult {
        DEPLOYED, EMPTY_RANKING, ALL_FAILED;
    }


    @Autowired
    private DeployerDispatcher deployerDispatcher;

    //The deployer to use, retrieved from the dispatcher
    private IDeployer deployer;

    //Executor service for executing deployment tasks asynchronously
    private ExecutorService executorService;

    /**
     * Creates the deployment executor.
     */
    public DiscoveryDeploymentExecutor() {
        //Create executor service
        this.executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    }

    /**
     * Initializes the deployment executor.
     */
    @PostConstruct
    private void init() {
        //Get deployer from the dispatcher
        this.deployer = deployerDispatcher.getDeployer();
    }

    public CompletableFuture<DeploymentResult> deployByRanking(DynamicPeripheral dynamicPeripheral, CandidateDevicesRanking ranking) {
        return deployByRanking(dynamicPeripheral, ranking, -1);
    }

    public CompletableFuture<DeploymentResult> deployByRanking(DynamicPeripheral dynamicPeripheral, CandidateDevicesRanking ranking, double minScoreExclusive) {
        //Check and remember whether the dynamic peripheral is currently deployed (based on the device details)
        //Go through the ranking and check every device for deployability
        //Thereby skip the device on which the peripheral is currently deployed
        //After one is found where the deployment succeed, kill the deployment on the previous device (if necessary)

        //This function should be universal usable, i.e. does as many checks as possible to make life easier for the engine

        //Do not do the stuff synchronously, but use a thread pool of certain size instead to execute the deployment tasks
        //Take a callback as function parameter and append it to the completable future --> Inform via callback whether
        //the deployment succeeded, resulting in the next status of the dynamic peripheral


        return null;
    }
}
