package org.citopt.connde.web.rest.helper;

import org.citopt.connde.domain.device.Device;
import org.citopt.connde.domain.entity_type.DeviceType;
import org.citopt.connde.domain.monitoring.MonitoringAdapter;
import org.citopt.connde.domain.monitoring.MonitoringComponent;
import org.citopt.connde.repository.DeviceRepository;
import org.citopt.connde.repository.MonitoringAdapterRepository;
import org.citopt.connde.repository.projection.MonitoringAdapterExcerpt;
import org.citopt.connde.service.UserEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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
     */
    public MonitoringComponent createMonitoringComponent(String deviceId, String monitoringAdapterId) {
        //Retrieve corresponding device and adapter from their repositories
        Device device = (Device) userEntityService.getUserEntityFromRepository(deviceRepository, deviceId);
        MonitoringAdapter monitoringAdapter = (MonitoringAdapter) userEntityService.getUserEntityFromRepository(monitoringAdapterRepository, monitoringAdapterId);

        //Check if both objects were found
        if ((device == null) || (monitoringAdapter == null)) {
            return null;
        }

        //Create new monitoring component (wrapper)
        MonitoringComponent monitoringComponent = new MonitoringComponent(monitoringAdapter, device);

        //Copy owner and approved users from device to void failing generic security checks
        monitoringComponent.setOwner(device.getOwner());
        monitoringComponent.getApprovedUsers().addAll(device.getApprovedUsers());

        return monitoringComponent;
    }

    /**
     * Retrieves a list of all monitoring adapters in the corresponding repository
     * that are compatible with a certain device.
     *
     * @param device The device for which the compatible monitoring adapters should be retrieved
     * @return A list of monitoring adapters that are compatible with the device
     */
    public List<MonitoringAdapter> getCompatibleAdapters(Device device) {
        //Sanity check
        if (device == null) {
            throw new IllegalArgumentException("Device must not be null.");
        }

        //Get device type
        String deviceTyp = device.getComponentType();

        //Get all monitoring adapters
        List<MonitoringAdapter> allAdapters = monitoringAdapterRepository.findAll();

        //Create a list for all compatible adapters
        List<MonitoringAdapter> compatibleAdapterList = new ArrayList<>();

        //Iterate over all adapters, check for compatibility and add compatible adapters to list
        for (MonitoringAdapter adapter : allAdapters) {
            if (adapter.isCompatibleWith(deviceTyp)) {
                compatibleAdapterList.add(adapter);
            }
        }

        return compatibleAdapterList;
    }

    /**
     * Retrieves a list of all devices in the corresponding repository that are compatible with a certain
     * monitoring adapter.
     *
     * @param adapter The monitoring adapter for which the compatible devices should be retrieved
     * @return A list of devices that are compatible with the monitoring adapter
     */
    public List<Device> getCompatibleDevices(MonitoringAdapter adapter) {
        //Sanity check
        if (adapter == null) {
            throw new IllegalArgumentException("Device must not be null.");
        }

        //Get device types of the adapter
        List<DeviceType> adapterDeviceTypes = adapter.getDeviceTypes();

        //Get all devices
        List<Device> allDevices = deviceRepository.findAll();

        //Create a list for all compatible devices
        List<Device> compatibleDevicesList = new ArrayList<>();

        //Iterate over all devices, check for compatibility and add compatible devices to list
        for (Device device : allDevices) {
            //Get component type of current device
            String deviceComponentType = device.getComponentType();

            //Check for compatibility
            if (adapter.isCompatibleWith(deviceComponentType)) {
                compatibleDevicesList.add(device);
            }
        }

        return compatibleDevicesList;
    }

    /**
     * Converts an Iterable of monitoring adapters into a list of monitoring adapter projections.
     *
     * @param monitoringAdapters The Iterable of monitoring adapters to convert
     * @return The converted list of monitoring adapter projections
     */
    public List<MonitoringAdapterExcerpt> convertToListProjections(
            Iterable<MonitoringAdapter> monitoringAdapters) {
        //Sanity check
        if (monitoringAdapters == null) {
            throw new IllegalArgumentException("The iterable of monitoring adapters must not be null.");
        }

        //Create a list for the resulting adapter projections
        List<MonitoringAdapterExcerpt> adapterProjectionList = new ArrayList<>();

        //Get projection for each monitoring adapter of the iterable
        for (MonitoringAdapter adapter : monitoringAdapters) {
            MonitoringAdapterExcerpt projection = monitoringAdapterRepository.findById(adapter.getId());
            adapterProjectionList.add(projection);
        }

        return adapterProjectionList;
    }
}
