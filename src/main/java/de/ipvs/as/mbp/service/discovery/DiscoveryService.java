package de.ipvs.as.mbp.service.discovery;

import de.ipvs.as.mbp.domain.discovery.collections.CandidateDevicesRanking;
import de.ipvs.as.mbp.domain.discovery.collections.CandidateDevicesContainer;
import de.ipvs.as.mbp.domain.discovery.deployment.DynamicDeployment;
import de.ipvs.as.mbp.domain.discovery.description.DeviceDescription;
import de.ipvs.as.mbp.domain.discovery.device.DeviceTemplate;
import de.ipvs.as.mbp.domain.discovery.topic.RequestTopic;
import de.ipvs.as.mbp.error.MBPException;
import de.ipvs.as.mbp.service.discovery.engine.DiscoveryEngine;
import de.ipvs.as.mbp.service.discovery.gateway.DiscoveryGateway;
import de.ipvs.as.mbp.service.discovery.processing.CandidateDevicesProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * This service provides various discovery-related service functions that may be offered to the user via the REST API.
 * It outsources all messaging-related logic and behaviour to the {@link DiscoveryGateway}.
 */
@Service
public class DiscoveryService {

    /*
    Auto-wired components
     */
    @Autowired
    private DiscoveryEngine discoveryEngine;

    @Autowired
    private DiscoveryGateway discoveryGateway;

    @Autowired
    private CandidateDevicesProcessor candidateDevicesProcessor;

    /**
     * Creates and initializes the discovery service.
     */
    public DiscoveryService() {

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
        CandidateDevicesContainer candidateDevices = this.discoveryGateway.getDeviceCandidates(Collections.singletonList(deviceTemplate), requestTopics).get(deviceTemplate.getId());

        //Use the processor to filter, aggregate, score and rank the candidate devices
        return candidateDevicesProcessor.process(candidateDevices, deviceTemplate);
    }

    /**
     * Checks the availability of discovery repositories for a given {@link RequestTopic} and returns
     * a map (repository name --> device descriptions count) containing the unique names of the repositories
     * that replied to the request as well as the number of device descriptions they contain.
     *
     * @param requestTopic The request topic for which the repository availability is supposed to be tested
     * @return The resulting map (repository ID --> device descriptions count)
     */
    public Map<String, Integer> getAvailableRepositories(RequestTopic requestTopic) {
        //Sanity check
        if (requestTopic == null) {
            throw new IllegalArgumentException("Request topic must not be null.");
        }

        //Call the corresponding gateway method
        return discoveryGateway.getAvailableRepositories(requestTopic);
    }

    /**
     * Activates a given {@link DynamicDeployment}. If this is not possible, e.g. because the deployment is already
     * activated, an exception is thrown.
     *
     * @param dynamicDeployment The dynamic deployment to activate
     */
    public void activateDynamicDeployment(DynamicDeployment dynamicDeployment) {
        //Null check
        if (dynamicDeployment == null) {
            throw new IllegalArgumentException("The dynamic deployment must not be null.");
        }

        //Use the discovery engine for activating the deployment
        boolean success = this.discoveryEngine.activateDynamicDeployment(dynamicDeployment.getId());

        //Check for success
        if (!success) {
            throw new MBPException(HttpStatus.PRECONDITION_FAILED, "The dynamic deployment is already activated.");
        }
    }

    /**
     * Deactivates a given {@link DynamicDeployment}. If this is not possible, e.g. because the deployment is already
     * deactivated, an exception is thrown.
     *
     * @param dynamicDeployment The dynamic deployment to deactivate
     */
    public void deactivateDynamicDeployment(DynamicDeployment dynamicDeployment) {
        //Null check
        if (dynamicDeployment == null) {
            throw new IllegalArgumentException("The dynamic deployment must not be null.");
        }

        //Use the discovery engine for deactivating the deployment
        boolean success = this.discoveryEngine.deactivateDynamicDeployment(dynamicDeployment.getId());

        //Check for success
        if (!success) {
            throw new MBPException(HttpStatus.PRECONDITION_FAILED, "The dynamic deployment is already deactivated.");
        }
    }

    /**
     * Deletes a given {@link DynamicDeployment} safely. This includes the check of pre-conditions as well as
     * the undeployment of possibly deployed operators.
     *
     * @param dynamicDeployment The dynamic deployment to delete
     */
    public void deleteDynamicDeployment(DynamicDeployment dynamicDeployment) {
        //Null check
        if (dynamicDeployment == null) {
            throw new IllegalArgumentException("The dynamic deployment must not be null.");
        }

        //Let the discovery engine perform the deletion and catch possible errors
        try {
            this.discoveryEngine.deleteDynamicDeployment(dynamicDeployment.getId());
        } catch (Exception e) {
            throw new MBPException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    /**
     * Deletes a certain {@link RequestTopic}, given by its ID, safely. For this, it is ensured that no
     * update of candidate devices is currently in progress that could accidently  re-create the subscriptions
     * at the discovery repositories.
     *
     * @param requestTopic The request topic that is supposed to be deleted
     */
    public void deleteRequestTopic(RequestTopic requestTopic) {
        //Null check
        if (requestTopic == null) {
            throw new IllegalArgumentException("The request topic must not be null.");
        }

        //Let the discovery engine perform the deletion and catch possible errors
        try {
            this.discoveryEngine.deleteRequestTopic(requestTopic.getId());
        } catch (Exception e) {
            throw new MBPException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    /**
     * Updates the candidate devices and the corresponding subscriptions at the discovery repositories for a gien
     * {@link DeviceTemplate}.
     *
     * @param deviceTemplate The pertaining device template
     */
    public void refreshCandidateDevicesAndSubscriptions(DeviceTemplate deviceTemplate) {
        //Null check
        if (deviceTemplate == null) throw new IllegalArgumentException("The device template must not be null.");

        //Let the discovery engine update the candidate devices
        this.discoveryEngine.refreshCandidateDevicesAndSubscriptions(deviceTemplate);
    }
}
