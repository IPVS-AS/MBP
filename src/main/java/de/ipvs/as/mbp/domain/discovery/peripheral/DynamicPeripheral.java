package de.ipvs.as.mbp.domain.discovery.peripheral;

import de.ipvs.as.mbp.domain.device.Device;
import de.ipvs.as.mbp.domain.discovery.device.DeviceTemplate;
import de.ipvs.as.mbp.domain.discovery.device.monitoring.DeviceBlockSet;
import de.ipvs.as.mbp.domain.discovery.topic.RequestTopic;
import de.ipvs.as.mbp.domain.operator.Operator;
import de.ipvs.as.mbp.domain.user_entity.MBPEntity;
import de.ipvs.as.mbp.domain.user_entity.UserEntity;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.GeneratedValue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Objects of this class represent dynamic peripherals, i.e. actuators or sensors that do not make use of a fixed
 * {@link Device}, but use a {@link DeviceTemplate} instead in order to automatically find suitable devices
 * on which a user-defined {@link Operator} can be deployed, installed and executed.
 */
@Document
@MBPEntity(createValidator = DynamicPeripheralCreateValidator.class)
public class DynamicPeripheral extends UserEntity {

    @Id
    @GeneratedValue
    private String id;

    //Name of the dynamic peripheral
    private String name;

    //The operator to deploy on the chosen devices
    @DBRef
    private Operator operator;

    //The device template to use for finding suitable device candidates
    @DBRef
    private DeviceTemplate deviceTemplate;

    //The request topics to use for sending requests to discovery repositories
    @DBRef
    private List<RequestTopic> requestTopics;

    //The user's last intention regarding activating/deactivating the dynamic peripheral
    private boolean activatingIntended = false;

    //The current state of the dynamic peripheral
    private DynamicPeripheralState state = DynamicPeripheralState.DISABLED;

    //Details about the currently used device
    private DynamicPeripheralDeviceDetails lastDeviceDetails;

    //Set of devices to ignore as potential candidates
    private DeviceBlockSet blockSet;

    /**
     * Creates a new, empty dynamic peripheral.
     */
    public DynamicPeripheral() {
        //Initialize data structures
        this.blockSet = new DeviceBlockSet();
    }

    /**
     * Return the id of the dynamic peripheral.
     *
     * @return the id
     */
    @Override
    public String getId() {
        return this.id;
    }

    /**
     * Sets the id of the dynamic peripheral.
     *
     * @param id The id to set
     * @return The dynamic peripheral
     */
    public DynamicPeripheral setId(String id) {
        this.id = id;
        return this;
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
     * Sets the name of the dynamic peripheral.
     *
     * @param name The name to set
     * @return The dynamic peripheral
     */
    public DynamicPeripheral setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Returns the operator of the dynamic peripheral.
     *
     * @return The operator
     */
    public Operator getOperator() {
        return operator;
    }

    /**
     * Sets the operator of the dynamic peripheral.
     *
     * @param operator The operator to set
     * @return The dynamic peripheral
     */
    public DynamicPeripheral setOperator(Operator operator) {
        this.operator = operator;
        return this;
    }

    /**
     * Returns the device template of the dynamic peripheral.
     *
     * @return The device template
     */
    public DeviceTemplate getDeviceTemplate() {
        return deviceTemplate;
    }

    /**
     * Sets the device template of the dynamic peripheral.
     *
     * @param deviceTemplate The device template to set
     * @return The dynamic peripheral
     */
    public DynamicPeripheral setDeviceTemplate(DeviceTemplate deviceTemplate) {
        this.deviceTemplate = deviceTemplate;
        return this;
    }

    /**
     * Returns the list of {@link RequestTopic}s that are supposed to be used for retrieving the descriptions of
     * suitable candidate device from the discovery repositories.
     *
     * @return The list of request topics
     */
    public List<RequestTopic> getRequestTopics() {
        return requestTopics;
    }

    /**
     * Sets the collection of {@link RequestTopic}s that are supposed to be used for retrieving the descriptions of
     * suitable candidate device from the discovery repositories.
     *
     * @param requestTopics The collection of request topics
     * @return The dynamic peripheral
     */
    public DynamicPeripheral setRequestTopics(Collection<RequestTopic> requestTopics) {
        this.requestTopics = new ArrayList<>(requestTopics);
        return this;
    }


    /**
     * Returns whether the dynamic peripheral is currently intended to be active by the user.
     *
     * @return True, if active; false otherwise
     */
    public boolean isActivatingIntended() {
        return activatingIntended;
    }

    /**
     * Sets whether the dynamic peripheral is currently intended to be active by the user.
     *
     * @param activatingIntended True, if active; false otherwise
     * @return The dynamic peripheral
     */
    public DynamicPeripheral setActivatingIntended(boolean activatingIntended) {
        this.activatingIntended = activatingIntended;
        return this;
    }


    /**
     * Returns the state of the dynamic peripheral.
     *
     * @return The state
     */
    public DynamicPeripheralState getState() {
        return state;
    }

    /**
     * Sets the state of the dynamic peripheral.
     *
     * @param state The state to set
     * @return The dynamic peripheral
     */
    public DynamicPeripheral setState(DynamicPeripheralState state) {
        this.state = state;
        return this;
    }

    /**
     * Returns {@link DynamicPeripheralDeviceDetails} about the device that was most recently used for deploying
     * the operator of the dynamic peripheral.
     *
     * @return The device details
     */
    public DynamicPeripheralDeviceDetails getLastDeviceDetails() {
        return lastDeviceDetails;
    }

    /**
     * Sets the {@link DynamicPeripheralDeviceDetails} about the device that was most recently used for deploying
     * the operator of the dynamic peripheral.
     *
     * @param lastDeviceDetails The device details to set
     * @return The dynamic peripheral
     */
    public DynamicPeripheral setLastDeviceDetails(DynamicPeripheralDeviceDetails lastDeviceDetails) {
        this.lastDeviceDetails = lastDeviceDetails;
        return this;
    }

    /**
     * Returns the set of devices that are not considered as potential candidate devices for the dynamic peripheral.
     *
     * @return The block list
     */
    public DeviceBlockSet getBlockSet() {
        return blockSet;
    }

    /**
     * Sets the set of devices that are not considered as potential candidate devices for the dynamic peripheral.
     *
     * @return The block list
     */
    public DynamicPeripheral setBlockSet(DeviceBlockSet blockSet) {
        this.blockSet = blockSet;
        return this;
    }
}
