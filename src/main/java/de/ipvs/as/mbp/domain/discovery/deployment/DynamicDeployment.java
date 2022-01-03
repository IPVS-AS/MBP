package de.ipvs.as.mbp.domain.discovery.deployment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.ipvs.as.mbp.DynamicBeanProvider;
import de.ipvs.as.mbp.domain.component.Component;
import de.ipvs.as.mbp.domain.device.Device;
import de.ipvs.as.mbp.domain.discovery.device.DeviceTemplate;
import de.ipvs.as.mbp.domain.operator.Operator;
import de.ipvs.as.mbp.domain.user_entity.MBPEntity;
import de.ipvs.as.mbp.service.discovery.engine.DiscoveryEngine;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Objects of this class represent dynamic deployments, i.e. deployments for actuators or sensors that do not make use
 * of a fixed {@link Device}, but use a {@link DeviceTemplate} instead in order to automatically find the most
 * appropriate devices on which a user-defined {@link Operator} can be deployed, installed and executed.
 */
@Document
@MBPEntity(createValidator = DynamicDeploymentCreateValidator.class)
public class DynamicDeployment extends Component {

    //Name of the component type
    private static final String COMPONENT_TYPE_NAME = "dynamic_deployment";

    //The device template to use for finding suitable device candidates
    @DBRef
    private DeviceTemplate deviceTemplate;

    //The user's last intention regarding activating/deactivating the dynamic deployment
    private boolean activatingIntended = false;

    //The most recent state of the dynamic deployment
    private DynamicDeploymentState lastState = DynamicDeploymentState.DISABLED;

    //Details about the currently used device
    private DynamicDeploymentDeviceDetails lastDeviceDetails;

    /**
     * Creates a new, empty dynamic deployment.
     */
    public DynamicDeployment() {

    }

    /**
     * Returns the device template of the dynamic deployment.
     *
     * @return The device template
     */
    public DeviceTemplate getDeviceTemplate() {
        return deviceTemplate;
    }

    /**
     * Sets the device template of the dynamic deployment.
     *
     * @param deviceTemplate The device template to set
     * @return The dynamic deployment
     */
    public DynamicDeployment setDeviceTemplate(DeviceTemplate deviceTemplate) {
        this.deviceTemplate = deviceTemplate;
        return this;
    }

    /**
     * Returns whether the dynamic deployment is currently intended to be active by the user.
     *
     * @return True, if active; false otherwise
     */
    public boolean isActivatingIntended() {
        return activatingIntended;
    }

    /**
     * Sets whether the dynamic deployment is currently intended to be active by the user.
     *
     * @param activatingIntended True, if active; false otherwise
     * @return The dynamic deployment
     */
    public DynamicDeployment setActivatingIntended(boolean activatingIntended) {
        this.activatingIntended = activatingIntended;
        return this;
    }

    /**
     * Returns the last state of the dynamic deployment.
     *
     * @return The state
     */
    public DynamicDeploymentState getLastState() {
        return lastState;
    }

    /**
     * Sets the last state of the dynamic deployment.
     *
     * @param lastState The state to set
     * @return The dynamic deployment
     */
    public DynamicDeployment setLastState(DynamicDeploymentState lastState) {
        this.lastState = lastState;
        return this;
    }

    /**
     * Returns {@link DynamicDeploymentDeviceDetails} about the device that was most recently used for deploying
     * the operator of the dynamic deployment.
     *
     * @return The device details
     */
    public DynamicDeploymentDeviceDetails getLastDeviceDetails() {
        return lastDeviceDetails;
    }

    /**
     * Sets the {@link DynamicDeploymentDeviceDetails} about the device that was most recently used for deploying
     * the operator of the dynamic deployment.
     *
     * @param lastDeviceDetails The device details to set
     * @return The dynamic deployment
     */
    public DynamicDeployment setLastDeviceDetails(DynamicDeploymentDeviceDetails lastDeviceDetails) {
        this.lastDeviceDetails = lastDeviceDetails;
        return this;
    }

    @JsonProperty("inProgress")
    public boolean isInProgress() {
        //Check if ID of the dynamic deployment is available
        if ((this.getId() == null) || this.getId().isEmpty()) {
            return false;
        }

        //Get the discovery engine bean
        DiscoveryEngine engine = DynamicBeanProvider.get(DiscoveryEngine.class);

        //Check whether the dynamic deployment is in progress
        return engine.isDynamicDeploymentInProgress(this.getId());
    }

    @Override
    public String getComponentTypeName() {
        return COMPONENT_TYPE_NAME;
    }

    @JsonIgnore
    public Device getDevice() {
        throw new UnsupportedOperationException();
    }

    public Component setDevice(Device address) {
        throw new UnsupportedOperationException();
    }
}
