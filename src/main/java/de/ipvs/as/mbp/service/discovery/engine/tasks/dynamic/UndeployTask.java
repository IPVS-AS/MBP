package de.ipvs.as.mbp.service.discovery.engine.tasks.dynamic;

import de.ipvs.as.mbp.DynamicBeanProvider;
import de.ipvs.as.mbp.domain.discovery.collections.CandidateDevicesRanking;
import de.ipvs.as.mbp.domain.discovery.collections.CandidateDevicesResult;
import de.ipvs.as.mbp.domain.discovery.collections.ScoredCandidateDevice;
import de.ipvs.as.mbp.domain.discovery.device.DeviceTemplate;
import de.ipvs.as.mbp.domain.discovery.peripheral.DynamicPeripheral;
import de.ipvs.as.mbp.domain.discovery.peripheral.DynamicPeripheralDeviceDetails;
import de.ipvs.as.mbp.domain.discovery.peripheral.DynamicPeripheralState;
import de.ipvs.as.mbp.domain.operator.Operator;
import de.ipvs.as.mbp.repository.discovery.CandidateDevicesRepository;
import de.ipvs.as.mbp.repository.discovery.DynamicPeripheralRepository;
import de.ipvs.as.mbp.service.discovery.deployment.DiscoveryDeploymentService;
import de.ipvs.as.mbp.service.discovery.processing.CandidateDevicesProcessor;

import java.util.Optional;

/**
 * The purpose of this task is to undeploy the {@link Operator} of a given {@link DynamicPeripheral}
 * from the device that is referenced as {@link DynamicPeripheralDeviceDetails} in the {@link DynamicPeripheral},
 * in case the {@link DynamicPeripheral} is deployed on it.
 */
public class UndeployTask implements DynamicPeripheralTask {

    //The original version of the dynamic peripheral that is supposed to be deployed
    private DynamicPeripheral originalDynamicPeripheral;

    /*
    Injected fields
     */
    private final DiscoveryDeploymentService discoveryDeploymentService;
    private final DynamicPeripheralRepository dynamicPeripheralRepository;

    /**
     * Creates a new {@link UndeployTask} from a given {@link DynamicPeripheral}.
     *
     * @param dynamicPeripheral The dynamic peripheral to use
     */
    public UndeployTask(DynamicPeripheral dynamicPeripheral) {
        //Set fields
        setDynamicPeripheral(dynamicPeripheral);

        //Inject components
        this.discoveryDeploymentService = DynamicBeanProvider.get(DiscoveryDeploymentService.class);
        this.dynamicPeripheralRepository = DynamicBeanProvider.get(DynamicPeripheralRepository.class);
    }

    /**
     * Implements the actual operations of the task. It is recommended to check for Thread interruptions in order to
     * gracefully deal with cancellations of the task.
     */
    @Override
    public void run() {
        //Read dynamic peripheral from the repository
        Optional<DynamicPeripheral> dynamicPeripheralOptional = dynamicPeripheralRepository.findById(this.originalDynamicPeripheral.getId());

        //Sanity checks
        if (!dynamicPeripheralOptional.isPresent()) {
            //Task ends because data is not available
            return;
        }

        //Get peripheral from optional
        DynamicPeripheral dynamicPeripheral = dynamicPeripheralOptional.get();

        //Check intention for dynamic peripheral
        if (dynamicPeripheral.isActiveIntended()) {
            //De-activate is not intended, so no need to undeploy
            return;
        }

        //Update the state of the dynamic peripheral
        this.updateDynamicPeripheral(dynamicPeripheral.setState(DynamicPeripheralState.IN_PROGRESS));

        //Check whether the dynamic peripheral is currently deployed
        if(!((dynamicPeripheral.getLastDeviceDetails() != null) &&
                this.discoveryDeploymentService.isDeployed(dynamicPeripheral))){
            //Dynamic peripheral is not deployed, so just update the state
            updateDynamicPeripheral(dynamicPeripheral.setLastDeviceDetails(null)
                    .setState(DynamicPeripheralState.DISABLED));
            return;
        }

        //Dynamic peripheral is deployed, so undeploy it
        this.discoveryDeploymentService.undeploy(dynamicPeripheral);

        //Update state of dynamic peripheral accordingly
        updateDynamicPeripheral(dynamicPeripheral.setLastDeviceDetails(null)
                .setState(DynamicPeripheralState.DISABLED));
    }

    /**
     * Writes a given {@link DynamicPeripheral} to the repository, thus updating its fields in the database.
     *
     * @param dynamicPeripheral The dynamic peripheral to update
     */
    private void updateDynamicPeripheral(DynamicPeripheral dynamicPeripheral) {
        //Write state to repository
        this.dynamicPeripheralRepository.save(dynamicPeripheral);
    }

    /**
     * Returns the {@link DynamicPeripheral} object that has been originally passed to this task and is
     * supposed to be (re-)deployed.
     *
     * @return The dynamic peripheral
     */
    public DynamicPeripheral getDynamicPeripheral() {
        return this.originalDynamicPeripheral;
    }

    /**
     * Sets the {@link DynamicPeripheral} that is supposed to be (re-)deployed within this task.
     *
     * @param dynamicPeripheral The dynamic peripheral to set
     */
    private void setDynamicPeripheral(DynamicPeripheral dynamicPeripheral) {
        //Null check
        if (dynamicPeripheral == null) {
            throw new IllegalArgumentException("The dynamic peripheral must not be null.");
        }

        this.originalDynamicPeripheral = dynamicPeripheral;
    }

    /**
     * Returns the ID of the {@link DynamicPeripheral} on which this task operates.
     *
     * @return The dynamic peripheral ID
     */
    @Override
    public String getDynamicPeripheralId() {
        return this.originalDynamicPeripheral.getId();
    }


    /**
     * Returns the ID of the device template that is used by the {@link DynamicPeripheral} on which this task operates.
     *
     * @return The device template ID
     */
    @Override
    public String getDeviceTemplateId() {
        return this.originalDynamicPeripheral.getDeviceTemplate().getId();
    }
}
