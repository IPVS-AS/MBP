package de.ipvs.as.mbp.service.discovery;

import de.ipvs.as.mbp.domain.discovery.collections.DeviceDescriptionRanking;
import de.ipvs.as.mbp.domain.discovery.collections.DeviceDescriptionSet;
import de.ipvs.as.mbp.domain.discovery.device.DeviceTemplate;
import de.ipvs.as.mbp.domain.discovery.topic.RequestTopic;
import de.ipvs.as.mbp.service.discovery.processing.DeviceDescriptionProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * This components manages the overall discovery process by orchestrating the various involved components and takes
 * care about the execution of discovery-related tasks.
 */
@Component
public class DiscoveryEngine {

    /*
    Auto-wired components
     */
    @Autowired
    private DeviceDescriptionProcessor deviceDescriptionProcessor;

    @Autowired
    private DiscoveryGateway discoveryGateway;

    /**
     * Creates the discovery engine.
     */
    public DiscoveryEngine() {

    }

    /**
     * Initializes the discovery engine.
     */
    @PostConstruct
    public void initialize() {

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
    public DeviceDescriptionRanking findDeviceDescriptions(DeviceTemplate deviceTemplate, Collection<RequestTopic> requestTopics) {
        //Sanity checks
        if (deviceTemplate == null) {
            throw new IllegalArgumentException("The device template must not be null.");
        } else if ((requestTopics == null) || requestTopics.isEmpty() || (requestTopics.stream().anyMatch(Objects::isNull))) {
            throw new IllegalArgumentException("The request topics must not be null or empty.");
        }

        //Use the gateway to find all device descriptions that match the device template
        List<DeviceDescriptionSet> deviceDescriptionSets = this.discoveryGateway.getDevicesForTemplate(deviceTemplate, requestTopics);

        //Use the processor to filter, aggregate, score and rank the descriptions
        return deviceDescriptionProcessor.process(deviceDescriptionSets, deviceTemplate);
    }
}
