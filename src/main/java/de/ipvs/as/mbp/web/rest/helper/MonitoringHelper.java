package de.ipvs.as.mbp.web.rest.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import de.ipvs.as.mbp.domain.access_control.ACAccessRequest;
import de.ipvs.as.mbp.domain.access_control.ACAccessType;
import de.ipvs.as.mbp.domain.device.Device;
import de.ipvs.as.mbp.domain.monitoring.MonitoringComponent;
import de.ipvs.as.mbp.error.EntityNotFoundException;
import de.ipvs.as.mbp.repository.DeviceRepository;
import de.ipvs.as.mbp.repository.MonitoringOperatorRepository;
import de.ipvs.as.mbp.repository.projection.MonitoringOperatorExcerpt;
import de.ipvs.as.mbp.service.UserEntityService;
import de.ipvs.as.mbp.domain.monitoring.MonitoringOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Helper component that provides a bundle of methods for working with monitoring components.
 */
@Component
public class MonitoringHelper {

    @Autowired
    private UserEntityService userEntityService;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private MonitoringOperatorRepository monitoringOperatorRepository;

    
    /**
     * Creates a deployable monitoring component which wraps a device and a monitoring operator. For this purpose,
     * the objects for the provided ids are looked up in the dedicated repository.
     *
     * @param deviceId            The id of the device to wrap
     * @param monitoringOperatorId The id of the monitoring operator to wrap
     * @return Null, if either the device or the monitoring operator could not be found
     * in their repositories or the user is not authorized for them; otherwise the deployable monitoring component
     * @throws EntityNotFoundException
     */
    public MonitoringComponent createMonitoringComponent(String deviceId, String monitoringOperatorId) throws EntityNotFoundException {
        // Retrieve corresponding device and operator from the database
    	Device device = userEntityService.getForId(deviceRepository, deviceId);
    	MonitoringOperator monitoringOperator = userEntityService.getForId(monitoringOperatorRepository, monitoringOperatorId);

        // Create new monitoring component (wrapper)
        MonitoringComponent monitoringComponent = new MonitoringComponent(monitoringOperator, device);

        // Set owner accordingly
        monitoringComponent.setOwner(device.getOwner());

        return monitoringComponent;
    }

	/**
	 * Retrieves all monitoring operators compatible with a given device.
	 *
	 * @param device the {@link Device}.
	 * @return All monitoring operators compatible with a given device.
	 */
	public List<MonitoringOperator> getCompatibleOperators(Device device) {
		if (device == null) {
			throw new IllegalArgumentException("Device must not be null.");
		}

		// Get all compatible monitoring operators
		return monitoringOperatorRepository.findAll()
				.stream().filter(a -> a.isCompatibleWith(device.getComponentType())).collect(Collectors.toList());
	}

	/**
	 * Retrieves all monitoring operators compatible with a given device
	 * and available for the requesting user.
	 *
	 * @param device the {@link Device}.
	 * @param accessRequest the {@link ACAccessRequest} required for policy evaluation.
	 * @return all monitoring operators compatible with a given device
	 * 		   and available for the requesting user.
	 */
	public List<MonitoringOperator> getCompatibleOperators(Device device, ACAccessRequest accessRequest) {
		if (device == null) {
			throw new IllegalArgumentException("Device must not be null.");
		}

		// Get all compatible monitoring operators available for the requesting user
		return userEntityService.getAllWithAccessControlCheck(monitoringOperatorRepository, ACAccessType.READ, accessRequest)
				.stream().filter(a -> a.isCompatibleWith(device.getComponentType())).collect(Collectors.toList());
	}

	/**
	 * Retrieves all devices compatible with a given monitoring operator.
	 *
	 * @param operator the {@link MonitoringOperator}.
	 * @return all devices compatible with a given monitoring operator
	 */
	public List<Device> getCompatibleDevices(MonitoringOperator operator) {
		if (operator == null) {
			throw new IllegalArgumentException("Operator must not be null.");
		}
		
		// Get all compatible devices
		return deviceRepository.findAll()
				.stream().filter(d -> operator.isCompatibleWith(d.getComponentType())).collect(Collectors.toList());
	}

	/**
	 * Retrieves all devices compatible with a given monitoring operator
	 * and available for the requesting user.
	 *
	 * @param operator the {@link MonitoringOperator}.
	 * @param accessRequest the {@link ACAccessRequest} required for policy evaluation.
	 * @return all devices compatible with a given monitoring operator
	 * 		   and available for the requesting user.
	 */
	public List<Device> getCompatibleDevices(MonitoringOperator operator, ACAccessRequest accessRequest) {
		if (operator == null) {
			throw new IllegalArgumentException("Operator must not be null.");
		}
		
		// Get all compatible devices available for the requesting user
		return userEntityService.getAllWithAccessControlCheck(deviceRepository, ACAccessType.READ, accessRequest)
				.stream().filter(d -> operator.isCompatibleWith(d.getComponentType())).collect(Collectors.toList());
	}

	public List<MonitoringOperatorExcerpt> convertToListProjections(Iterable<MonitoringOperator> monitoringOperators) {
		if (monitoringOperators == null) {
			throw new IllegalArgumentException("The list of monitoring operators must not be null.");
		}
		
		// Extract excerpt from each operator
		List<MonitoringOperatorExcerpt> operatorExcerpts = new ArrayList<>();
		monitoringOperators.forEach(a -> monitoringOperatorRepository.findExcerptById(a.getId()).ifPresent(operatorExcerpts::add));
		return operatorExcerpts;
	}

	public Optional<MonitoringOperatorExcerpt> operatorToExcerpt(MonitoringOperator monitoringOperator) {
		return monitoringOperatorRepository.findExcerptById(monitoringOperator.getId());
	}
}
