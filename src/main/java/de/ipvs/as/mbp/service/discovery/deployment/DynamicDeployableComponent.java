package de.ipvs.as.mbp.service.discovery.deployment;

import de.ipvs.as.mbp.domain.component.Component;
import de.ipvs.as.mbp.domain.device.Device;
import de.ipvs.as.mbp.domain.discovery.deployment.DynamicDeployment;
import de.ipvs.as.mbp.domain.discovery.deployment.DynamicDeploymentDeviceDetails;
import de.ipvs.as.mbp.domain.discovery.description.DeviceDescription;
import de.ipvs.as.mbp.domain.key_pair.KeyPair;
import de.ipvs.as.mbp.service.deployment.IDeployer;

/**
 * Objects of this class wrap a {@link DynamicDeployment} that is supposed to be deployed and a corresponding
 * {@link DeviceDescription} describing the target device into a {@link Component}s that can actually be deployed
 * with the common {@link IDeployer}s.
 */
public class DynamicDeployableComponent extends Component {
    //Type name of the component
    private static final String TYPE_NAME = "dynamic";

    /**
     * Creates a new, empty dynamic deployable component.
     */
    public DynamicDeployableComponent() {

    }

    /**
     * Creates a new dynamic deployable component from a given {@link DynamicDeployment} that is supposed to be
     * deployed and the corresponding {@link DeviceDescription} describing the target device of the deployment.
     *
     * @param dynamicDeployment The dynamic deployment to use
     * @param deviceDescription THe device description to use
     */
    public DynamicDeployableComponent(DynamicDeployment dynamicDeployment, DeviceDescription deviceDescription) {
        //Call overloaded constructor
        this(dynamicDeployment, new DynamicDeploymentDeviceDetails(deviceDescription));
    }

    /**
     * Creates a new dynamic deployable component from a given {@link DynamicDeployment} and uses the
     * {@link DynamicDeploymentDeviceDetails} that are referenced in it to retrieve the target device information.
     *
     * @param dynamicDeployment The dynamic deployment to use
     */
    public DynamicDeployableComponent(DynamicDeployment dynamicDeployment) {
        //Call overloaded constructor
        this(dynamicDeployment, dynamicDeployment.getLastDeviceDetails());
    }

    /**
     * Creates a new dynamic deployable component from a given {@link DynamicDeployment} and given
     * {@link DynamicDeploymentDeviceDetails} that contain the relevant information about the target device.
     *
     * @param dynamicDeployment The dynamic deployment to use
     * @param deviceDetails     The device details to use
     */
    public DynamicDeployableComponent(DynamicDeployment dynamicDeployment, DynamicDeploymentDeviceDetails deviceDetails) {
        //Call constructor of super class
        super();

        //Null check
        if (dynamicDeployment == null) {
            throw new IllegalArgumentException("The dynamic deployment must not be null.");
        }

        //Copy fields to the component
        this.setId(dynamicDeployment.getId());
        this.setName(dynamicDeployment.getName());
        this.setComponentType(TYPE_NAME);
        this.setOperator(dynamicDeployment.getOperator());
        this.setOwner(dynamicDeployment.getOwner());

        //Check whether device details are available
        if (deviceDetails == null) {
            return;
        }

        //Create device
        Device device = new Device().setId(deviceDetails.getMacAddress()) //Use MAC address in order to enable SSH session caching
                .setName("")
                .setIpAddress(deviceDetails.getIpAddress())
                .setUsername(deviceDetails.getUsername())
                .setPassword(deviceDetails.getPassword());

        //Check if a key pair is provided
        String privateKey = deviceDetails.getPrivateKey();
        if ((privateKey != null) && (!privateKey.isEmpty())) {
            //Add key pair
            device.setKeyPair(new KeyPair().setId("").setName("").setPublicKey("").setPrivateKey(privateKey));
        }

        //Add device to component
        this.setDevice(device);
    }

    @Override
    public String getComponentTypeName() {
        return TYPE_NAME;
    }
}
