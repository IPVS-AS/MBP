package de.ipvs.as.mbp.service.discovery.deployment;

import de.ipvs.as.mbp.domain.component.Component;
import de.ipvs.as.mbp.domain.device.Device;
import de.ipvs.as.mbp.domain.discovery.description.DeviceDescription;
import de.ipvs.as.mbp.domain.discovery.peripheral.DynamicPeripheral;
import de.ipvs.as.mbp.domain.discovery.peripheral.DynamicPeripheralDeviceDetails;
import de.ipvs.as.mbp.domain.key_pair.KeyPair;
import de.ipvs.as.mbp.service.deployment.IDeployer;

/**
 * Objects of this class wrap a {@link DynamicPeripheral} that is supposed to be deployed and a corresponding
 * {@link DeviceDescription} describing the target device into a {@link Component}s that can actually be deployed
 * with the common {@link IDeployer}s.
 */
public class DynamicDeployableComponent extends Component {
    //Type name of the component
    private static final String TYPE_NAME = "dynamic";

    /**
     * Creates a new dynamic deployable component from a given {@link DynamicPeripheral} that is supposed to be
     * deployed and the corresponding {@link DeviceDescription} describing the target device of the deployment.
     *
     * @param dynamicPeripheral The dynamic peripheral to use
     * @param deviceDescription THe device description to use
     */
    public DynamicDeployableComponent(DynamicPeripheral dynamicPeripheral, DeviceDescription deviceDescription) {
        //Call overloaded constructor
        this(dynamicPeripheral, new DynamicPeripheralDeviceDetails(deviceDescription));
    }

    /**
     * Creates a new dynamic deployable component from a given {@link DynamicPeripheral} and uses the
     * {@link DynamicPeripheralDeviceDetails} that are referenced in it to retrieve the target device information.
     *
     * @param dynamicPeripheral The dynamic peripheral to use
     */
    public DynamicDeployableComponent(DynamicPeripheral dynamicPeripheral) {
        //Call overloaded constructor
        this(dynamicPeripheral, dynamicPeripheral.getLastDeviceDetails());
    }

    /**
     * Creates a new dynamic deployable component from a given {@link DynamicPeripheral} and given
     * {@link DynamicPeripheralDeviceDetails} that contain the relevant information about the target device.
     *
     * @param dynamicPeripheral The dynamic peripheral to use
     * @param deviceDetails     The device details to use
     */
    public DynamicDeployableComponent(DynamicPeripheral dynamicPeripheral, DynamicPeripheralDeviceDetails deviceDetails) {
        //Call constructor of super class
        super();

        //Null check
        if (dynamicPeripheral == null) {
            throw new IllegalArgumentException("The dynamic peripheral must not be null.");
        } else if (deviceDetails == null) {
            throw new IllegalArgumentException("The device details must not be null.");
        }

        //Create device
        Device device = new Device().setId("").setName("")
                .setIpAddress(deviceDetails.getIpAddress())
                .setUsername(deviceDetails.getUsername())
                .setPassword(deviceDetails.getPassword());

        //Check if a key pair is provided
        String privateKey = deviceDetails.getPrivateKey();
        if ((privateKey != null) && privateKey.isEmpty()) {
            //Add key pair
            device.setKeyPair(new KeyPair().setId("").setName("").setPublicKey("").setPrivateKey(privateKey));
        }

        //Set all fields of the component
        this.setId(dynamicPeripheral.getId());
        this.setName(dynamicPeripheral.getName());
        this.setComponentType("dynamic_deployment");
        this.setOperator(dynamicPeripheral.getOperator());
        this.setDevice(device);
        this.setOwner(dynamicPeripheral.getOwner());
    }

    @Override
    public String getComponentTypeName() {
        return TYPE_NAME;
    }
}
