package de.ipvs.as.mbp.service.discovery;

import de.ipvs.as.mbp.domain.discovery.collections.DeviceDescriptionRanking;
import de.ipvs.as.mbp.domain.discovery.device.DeviceTemplate;
import de.ipvs.as.mbp.domain.discovery.topic.RequestTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
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


    /**
     * Creates and initializes the discovery service.
     */
    public DiscoveryService() {

    }

    /**
     * Retrieves device descriptions that match the requirements of a given {@link DeviceTemplate} from discovery
     * repositories that are available under a given collection of {@link RequestTopic}s. The resulting device
     * descriptions from the repositories are then aggregated, processed, scored and ranked with respect to the scoring
     * criteria of the device template. The result is subsequently returned as {@link DeviceDescriptionRanking}.
     *
     * @param deviceTemplate The device template for which the device descriptions are supposed to be retrieved
     * @param requestTopics  The collection of request topics to use for querying the discovery repositories
     * @return The resulting ranking of the device descriptions
     */
    public DeviceDescriptionRanking retrieveDeviceDescriptions(DeviceTemplate deviceTemplate, Collection<RequestTopic> requestTopics) {
        //Sanity checks
        if (deviceTemplate == null) {
            throw new IllegalArgumentException("The device template must not be null.");
        } else if ((requestTopics == null) || requestTopics.isEmpty() || (requestTopics.stream().anyMatch(Objects::isNull))) {
            throw new IllegalArgumentException("The request topics must not be null or empty.");
        }

        //Use the discovery engine to retrieve the ranking of device descriptions
        return discoveryEngine.retrieveDeviceDescriptions(deviceTemplate, requestTopics);
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
}
