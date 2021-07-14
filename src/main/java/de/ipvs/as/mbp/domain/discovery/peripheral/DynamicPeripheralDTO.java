package de.ipvs.as.mbp.domain.discovery.peripheral;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * DTOs for {@link DynamicPeripheral}s.
 */
public class DynamicPeripheralDTO {

    //Name of the dynamic peripheral
    private String name;

    //Id of the operator to use
    @JsonProperty("operator")
    private String operatorId;

    //Id of the device template to use
    @JsonProperty("deviceTemplate")
    private String deviceTemplateId;

    //Ids of the request topics to use
    @JsonProperty("requestTopics")
    private List<String> requestTopicIds = new ArrayList<>();

    /**
     * Creates a new, empty DTO for a {@link DynamicPeripheral}.
     */
    public DynamicPeripheralDTO() {

    }

    /**
     * Returns the name of the dynamic peripheral.
     *
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the ID of the operator to use in the dynamic peripheral.
     *
     * @return The operator ID
     */
    public String getOperatorId() {
        return operatorId;
    }

    /**
     * Returns the ID of the device template to use in the dynamic peripheral.
     *
     * @return The device template ID
     */
    public String getDeviceTemplateId() {
        return deviceTemplateId;
    }

    /**
     * Returns the IDs of the request topics that are supposed to be used for retrieving suitable candidate devices
     * for the dynamic peripheral.
     *
     * @return The list of request topic IDs
     */
    public List<String> getRequestTopicIds() {
        return requestTopicIds;
    }
}