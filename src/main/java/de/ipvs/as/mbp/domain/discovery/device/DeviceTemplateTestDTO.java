package de.ipvs.as.mbp.domain.discovery.device;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.ipvs.as.mbp.domain.discovery.topic.RequestTopic;

import java.util.Set;

/**
 * DTO for test requests of {@link DeviceTemplate}s, wrapping the {@link DeviceTemplate} to test and a set
 * of IDs describing the {@link RequestTopic}s that are supposed to be used for the test.
 */
public class DeviceTemplateTestDTO {

    //The device template to test
    private DeviceTemplate deviceTemplate;

    //IDs of the request topic to use for the test
    @JsonProperty("requestTopics")
    private Set<String> requestTopicIds;

    /**
     * Creates a new device template test DTO.
     */
    public DeviceTemplateTestDTO() {

    }

    /**
     * Returns the {@link DeviceTemplate} that is supposed to be tested.
     *
     * @return The device template
     */
    public DeviceTemplate getDeviceTemplate() {
        return deviceTemplate;
    }

    /**
     * Returns the set of IDs describing the {@link RequestTopic} to use for the test.
     *
     * @return The set of request topic IDs
     */
    public Set<String> getRequestTopicIds() {
        return requestTopicIds;
    }
}
