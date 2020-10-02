package org.citopt.connde.web.rest.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.citopt.connde.domain.access_control.ACAccessRequest;
import org.citopt.connde.domain.access_control.ACAccessType;
import org.citopt.connde.domain.device.Device;
import org.citopt.connde.domain.monitoring.MonitoringAdapter;
import org.citopt.connde.domain.monitoring.MonitoringComponent;
import org.citopt.connde.error.EntityNotFoundException;
import org.citopt.connde.repository.DeviceRepository;
import org.citopt.connde.repository.MonitoringAdapterRepository;
import org.citopt.connde.repository.projection.MonitoringAdapterExcerpt;
import org.citopt.connde.service.UserEntityService;
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
    private MonitoringAdapterRepository monitoringAdapterRepository;

    
    /**
     * Creates a deployable monitoring component which wraps a device and a monitoring adapter. For this purpose,
     * the objects for the provided ids are looked up in the dedicated repository.
     *
     * @param deviceId            The id of the device to wrap
     * @param monitoringAdapterId The id of the monitoring adapter to wrap
     * @return Null, if either the device or the monitoring adapter could not be found
     * in their repositories or the user is not authorized for them; otherwise the deployable monitoring component
     * @throws EntityNotFoundException 
     */
    public MonitoringComponent createMonitoringComponent(String deviceId, String monitoringAdapterId) throws EntityNotFoundException {
        // Retrieve corresponding device and adapter from the database
    	Device device = userEntityService.getForId(deviceRepository, deviceId);
    	MonitoringAdapter monitoringAdapter = userEntityService.getForId(monitoringAdapterRepository, monitoringAdapterId);

        // Create new monitoring component (wrapper)
        MonitoringComponent monitoringComponent = new MonitoringComponent(monitoringAdapter, device);

        // Set owner accordingly
        monitoringComponent.setOwner(device.getOwner());

        return monitoringComponent;
    }

	/**
	 * Retrieves all monitoring adapters compatible with a given device.
	 *
	 * @param device the {@link Device}.
	 * @return all monitoring adapters compatible with a given device.
	 */
	public List<MonitoringAdapter> getCompatibleAdapters(Device device) {
		if (device == null) {
			throw new IllegalArgumentException("Device must not be null.");
		}

		// Get all compatible monitoring adapters
		return monitoringAdapterRepository.findAll()
				.stream().filter(a -> a.isCompatibleWith(device.getComponentType())).collect(Collectors.toList());
	}

	/**
	 * Retrieves all monitoring adapters compatible with a given device
	 * and available for the requesting user.
	 *
	 * @param device the {@link Device}.
	 * @param accessRequest the {@link ACAccessRequest} required for policy evaluation.
	 * @return all monitoring adapters compatible with a given device
	 * 		   and available for the requesting user.
	 */
	public List<MonitoringAdapter> getCompatibleAdapters(Device device, ACAccessRequest accessRequest) {
		if (device == null) {
			throw new IllegalArgumentException("Device must not be null.");
		}

		// Get all compatible monitoring adapters available for the requesting user
		return userEntityService.getAllWithPolicyCheck(monitoringAdapterRepository, ACAccessType.READ, accessRequest)
				.stream().filter(a -> a.isCompatibleWith(device.getComponentType())).collect(Collectors.toList());
	}

	/**
	 * Retrieves all devices compatible with a given monitoring adapter.
	 *
	 * @param adapter the {@link MonitoringAdapter}.
	 * @return all devices compatible with a given monitoring adapter.
	 */
	public List<Device> getCompatibleDevices(MonitoringAdapter adapter) {
		if (adapter == null) {
			throw new IllegalArgumentException("Adapter must not be null.");
		}
		
		// Get all compatible devices
		return deviceRepository.findAll()
				.stream().filter(d -> adapter.isCompatibleWith(d.getComponentType())).collect(Collectors.toList());
	}

	/**
	 * Retrieves all devices compatible with a given monitoring adapter
	 * and available for the requesting user.
	 *
	 * @param adapter the {@link MonitoringAdapter}.
	 * @param accessRequest the {@link ACAccessRequest} required for policy evaluation.
	 * @return all devices compatible with a given monitoring adapter
	 * 		   and available for the requesting user.
	 */
	public List<Device> getCompatibleDevices(MonitoringAdapter adapter, ACAccessRequest accessRequest) {
		if (adapter == null) {
			throw new IllegalArgumentException("Adapter must not be null.");
		}
		
		// Get all compatible devices available for the requesting user
		return userEntityService.getAllWithPolicyCheck(deviceRepository, ACAccessType.READ, accessRequest)
				.stream().filter(d -> adapter.isCompatibleWith(d.getComponentType())).collect(Collectors.toList());
	}

	public List<MonitoringAdapterExcerpt> convertToListProjections(Iterable<MonitoringAdapter> monitoringAdapters) {
		if (monitoringAdapters == null) {
			throw new IllegalArgumentException("The list of monitoring adapters must not be null.");
		}
		
		// Extract excerpt from each adapter
		List<MonitoringAdapterExcerpt> adapterExcerpts = new ArrayList<>();
		monitoringAdapters.forEach(a -> monitoringAdapterRepository.findExcerptById(a.getId()).ifPresent(adapterExcerpts::add));
		return adapterExcerpts;
	}

	public Optional<MonitoringAdapterExcerpt> adapterToExcerpt(MonitoringAdapter monitoringAdapter) {
		return monitoringAdapterRepository.findExcerptById(monitoringAdapter.getId());
	}
}
