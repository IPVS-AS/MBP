package de.ipvs.as.mbp.service.discovery;

import de.ipvs.as.mbp.domain.discovery.topic.RequestTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * This service provides various discovery-related service functions that may be offered to the user via the REST API.
 * It outsources all messaging-related logic and behaviour to the {@link DiscoveryGateway}.
 */
@Service
public class DiscoveryService {

    @Autowired
    private DiscoveryGateway discoveryGateway;


    /**
     * Creates and initializes the discovery service.
     */
    public DiscoveryService() {

    }


    /**
     * Checks the availability of discovery repositories for a given {@link RequestTopic} and returns
     * a map (repository ID --> device descriptions count) containing the identifiers of the repositories
     * that replied to the request as well as the number of device descriptions they contain.
     *
     * @param requestTopic The request topic for which the repository availability is supposed to be tested
     * @return The resulting map (repository ID --> device descriptions count)
     */
    public Map<String, Integer> checkAvailableRepositories(RequestTopic requestTopic) {
        //Sanity check
        if (requestTopic == null) {
            throw new IllegalArgumentException("Request topic must not be null.");
        }

        //Call the corresponding gateway method
        return checkAvailableRepositories(requestTopic);
    }
}
