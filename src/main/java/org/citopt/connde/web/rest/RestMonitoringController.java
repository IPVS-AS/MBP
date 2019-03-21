package org.citopt.connde.web.rest;

import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.adapter.parameters.ParameterInstance;
import org.citopt.connde.domain.component.Component;
import org.citopt.connde.domain.device.Device;
import org.citopt.connde.domain.monitoring.MonitoringAdapter;
import org.citopt.connde.domain.monitoring.MonitoringComponent;
import org.citopt.connde.repository.DeviceRepository;
import org.citopt.connde.repository.MonitoringAdapterRepository;
import org.citopt.connde.repository.projection.MonitoringAdapterListProjection;
import org.citopt.connde.service.deploy.ComponentState;
import org.citopt.connde.web.rest.response.ActionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * REST Controller that exposes methods for the purpose of device monitoring.
 *
 * @author Jan
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH)
public class RestMonitoringController {

    @Autowired
    ComponentDeploymentWrapper deploymentWrapper;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private MonitoringAdapterRepository monitoringAdapterRepository;

    @GetMapping(value = "/monitoring/{deviceId}")
    public ResponseEntity<Boolean> isMonitoringActive(@PathVariable(value = "deviceId") String deviceId,
                                                      @RequestParam("adapter") String monitoringAdapterId) {
        //Validity check
        if ((deviceId == null) || deviceId.isEmpty() || (monitoringAdapterId == null) || monitoringAdapterId.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        //Create new monitoring component
        MonitoringComponent monitoringComponent = createComponent(deviceId, monitoringAdapterId);

        //Validity check
        if (monitoringComponent == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        //Do check
        return deploymentWrapper.isRunningComponent(monitoringComponent);
    }

    @PostMapping(value = "/monitoring/{deviceId}")
    public ResponseEntity<ActionResponse> enableMonitoring(@PathVariable(value = "deviceId") String deviceId,
                                                           @RequestParam("adapter") String monitoringAdapterId,
                                                           @RequestBody List<ParameterInstance> parameters) {
        //Validity check
        if ((deviceId == null) || deviceId.isEmpty() || (monitoringAdapterId == null) || monitoringAdapterId.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        //Create new monitoring component
        MonitoringComponent monitoringComponent = createComponent(deviceId, monitoringAdapterId);

        //Validity check
        if (monitoringComponent == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        //Deploy
        return deploymentWrapper.deployComponent(monitoringComponent, parameters);
    }

    @DeleteMapping(value = "/monitoring/{deviceId}")
    public ResponseEntity<ActionResponse> disableMonitoring(@PathVariable(value = "deviceId") String deviceId,
                                                            @RequestParam("adapter") String monitoringAdapterId) {
        //Validity check
        if ((deviceId == null) || deviceId.isEmpty() || (monitoringAdapterId == null) || monitoringAdapterId.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        //Create new monitoring component
        MonitoringComponent monitoringComponent = createComponent(deviceId, monitoringAdapterId);

        //Validity check
        if (monitoringComponent == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        //Undeploy
        return deploymentWrapper.undeployComponent(monitoringComponent);
    }

    @RequestMapping(value = "/monitoring/state/{deviceId}", method = RequestMethod.GET)
    public ResponseEntity<Map<String, ComponentState>> getDeviceMonitoringState(@PathVariable(value = "deviceId") String deviceId) {
        //Validity check
        if ((deviceId == null) || deviceId.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        //Fetch device object
        Device device = deviceRepository.findOne(deviceId);

        //Check if device could be found
        if (device == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        //Retrieve compatible adapters
        List<MonitoringAdapter> compatibleAdapterList = getCompatibleAdapters(device);

        //For each adapter create a new monitoring component and store it in a list
        List<Component> monitoringComponentList = new ArrayList<>();
        for (MonitoringAdapter monitoringAdapter : compatibleAdapterList) {
            MonitoringComponent monitoringComponent = new MonitoringComponent(monitoringAdapter, device);
            monitoringComponentList.add(monitoringComponent);
        }

        //Retrieve states for all monitoring components
        return deploymentWrapper.getStatesAllComponents(monitoringComponentList);
    }


    @RequestMapping(value = "/monitoring/state/{deviceId}", params = {"adapter"}, method = RequestMethod.GET)
    public ResponseEntity<ComponentState> getMonitoringState(@PathVariable(value = "deviceId") String deviceId,
                                                             @RequestParam("adapter") String monitoringAdapterId) {
        //Create new monitoring component
        MonitoringComponent monitoringComponent = createComponent(deviceId, monitoringAdapterId);

        //Validity check
        if (monitoringComponent == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return deploymentWrapper.getComponentState(monitoringComponent);
    }

    /**
     * Creates a deployable component object which wraps a device and a monitoring adapter. For this purpose,
     * the objects for the provided ids are looked up in the dedicated repository.
     *
     * @param deviceId            The id of the device to wrap
     * @param monitoringAdapterId The id of the monitoring adapter to wrap
     * @return The deployable component object
     */
    private MonitoringComponent createComponent(String deviceId, String monitoringAdapterId) {
        //Retrieve corresponding device and adapter from their repositories
        Device device = deviceRepository.findOne(deviceId);
        MonitoringAdapter monitoringAdapter = monitoringAdapterRepository.findOne(monitoringAdapterId);

        //Check if both objects were found
        if ((device == null) || (monitoringAdapter == null)) {
            return null;
        }

        //Create new monitoring component (wrapper)
        MonitoringComponent monitoringComponent = new MonitoringComponent(monitoringAdapter, device);

        return monitoringComponent;
    }

    /**
     * Returns a list of monitoring adapters that are compatible with a certain device.
     *
     * @param deviceId The id of the device for which the adapters are supposed to be retrieved
     * @return A list of all monitoring adapters that are compatible with the device
     */
    @GetMapping("/monitoring-adapters/by-device/{id}")
    public ResponseEntity<List<MonitoringAdapterListProjection>> getCompatibleMonitoringAdaptersForDevice(@PathVariable(value = "id") String deviceId) {
        //Validity check
        if ((deviceId == null) || deviceId.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        //Fetch device object
        Device device = deviceRepository.findOne(deviceId);

        //Check if device could be found
        if (device == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        //Get list of compatible adapters for this device
        List<MonitoringAdapter> compatibleAdapters = getCompatibleAdapters(device);

        //Create a list for the corresponding adapter projections
        List<MonitoringAdapterListProjection> adapterProjectionList = new ArrayList<>();

        //Get projection for each compatible adapter and add it to the list
        for (MonitoringAdapter adapter : compatibleAdapters) {
            MonitoringAdapterListProjection projection = monitoringAdapterRepository.findById(adapter.getId());
            adapterProjectionList.add(projection);
        }

        return new ResponseEntity<>(adapterProjectionList, HttpStatus.OK);
    }

    /**
     * Retrieves a list of all monitoring adapters in the corresponding repository
     * that are compatible with a certain device.
     *
     * @param device The device for which the compatible monitoring adapters should be determined
     * @return A list of monitoring adapters that are compatible with the device
     */
    private List<MonitoringAdapter> getCompatibleAdapters(Device device) {
        //Validity check
        if (device == null) {
            throw new IllegalArgumentException("Device must not be null.");
        }

        //Get device type
        String deviceTyp = device.getComponentType();

        //Get all monitoring adapters
        List<MonitoringAdapter> allAdapters = monitoringAdapterRepository.findAll();

        //Create a list for all compatible adapters
        List<MonitoringAdapter> compatibleAdapterList = new ArrayList<>();

        //Iterate over all adapters and check for compatibility
        for (MonitoringAdapter adapter : allAdapters) {
            if (adapter.isCompatibleWith(deviceTyp)) {
                //Add compatible adapter
                compatibleAdapterList.add(adapter);
            }
        }

        return compatibleAdapterList;
    }
}
