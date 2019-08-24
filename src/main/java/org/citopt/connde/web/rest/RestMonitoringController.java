package org.citopt.connde.web.rest;

import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.adapter.parameters.ParameterInstance;
import org.citopt.connde.domain.component.Component;
import org.citopt.connde.domain.device.Device;
import org.citopt.connde.domain.monitoring.MonitoringAdapter;
import org.citopt.connde.domain.monitoring.MonitoringComponent;
import org.citopt.connde.domain.monitoring.MonitoringComponentDTO;
import org.citopt.connde.repository.DeviceRepository;
import org.citopt.connde.repository.projection.MonitoringAdapterExcerpt;
import org.citopt.connde.service.deploy.ComponentState;
import org.citopt.connde.web.rest.helper.DeploymentWrapper;
import org.citopt.connde.web.rest.helper.MonitoringHelper;
import org.citopt.connde.web.rest.response.ActionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * REST Controller that exposes methods for the purpose of device monitoring.
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH)
public class RestMonitoringController {

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    DeploymentWrapper deploymentWrapper;

    @Autowired
    private MonitoringHelper monitoringHelper;

    /**
     * Replies to the requesting client whether the monitoring is currently active for a certain device
     * and monitoring adapter.
     *
     * @param deviceId            The id of the device
     * @param monitoringAdapterId The id of the monitoring adapter
     * @return A response with true, if the monitoring is currently active and false otherwise
     */
    @GetMapping(value = "/monitoring/{deviceId}")
    public ResponseEntity<Boolean> isMonitoringActive(@PathVariable(value = "deviceId") String deviceId,
                                                      @RequestParam("adapter") String monitoringAdapterId) {
        //Validity check
        if ((deviceId == null) || deviceId.isEmpty() || (monitoringAdapterId == null) || monitoringAdapterId.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        //Create new monitoring component from parameters
        MonitoringComponent monitoringComponent =
                monitoringHelper.createMonitoringComponent(deviceId, monitoringAdapterId);

        //Validity check
        if (monitoringComponent == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        //Do check
        return deploymentWrapper.isComponentRunning(monitoringComponent);
    }

    /**
     * Tries to enable monitoring for a certain device and monitoring adapter with optional parameters
     * and replies to the requesting client whether this action was successful.
     *
     * @param deviceId            The id of the device to monitor
     * @param monitoringAdapterId The id of the monitoring adapter
     * @param parameters          Optional deployment parameters to use
     * @return A response containing the result of the monitoring enabling attempt
     */
    @PostMapping(value = "/monitoring/{deviceId}")
    public ResponseEntity<ActionResponse> enableMonitoring(@PathVariable(value = "deviceId") String deviceId,
                                                           @RequestParam("adapter") String monitoringAdapterId,
                                                           @RequestBody List<ParameterInstance> parameters) {
        //Validity check
        if ((deviceId == null) || deviceId.isEmpty() || (monitoringAdapterId == null) || monitoringAdapterId.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        //Create new monitoring component
        MonitoringComponent monitoringComponent =
                monitoringHelper.createMonitoringComponent(deviceId, monitoringAdapterId);

        //Validity check
        if (monitoringComponent == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        //Deploy monitoring component
        ResponseEntity<ActionResponse> response = deploymentWrapper.deployComponent(monitoringComponent);

        //Check if deployment was not successful
        if (!response.getBody().isSuccess()) {
            return response;
        }

        //Start monitoring component
        return deploymentWrapper.startComponent(monitoringComponent, parameters);
    }

    /**
     * Tries to disable monitoring for a certain device and monitoring adapter and replies to the requesting client
     * whether this action was successful.
     *
     * @param deviceId            The id of the device
     * @param monitoringAdapterId The id of the monitoring adapter
     * @return A response containing the result of the monitoring disabling attempt
     */
    @DeleteMapping(value = "/monitoring/{deviceId}")
    public ResponseEntity<ActionResponse> disableMonitoring(@PathVariable(value = "deviceId") String deviceId,
                                                            @RequestParam("adapter") String monitoringAdapterId) {
        //Validity check
        if ((deviceId == null) || deviceId.isEmpty() || (monitoringAdapterId == null) || monitoringAdapterId.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        //Create new monitoring component
        MonitoringComponent monitoringComponent =
                monitoringHelper.createMonitoringComponent(deviceId, monitoringAdapterId);

        //Validity check
        if (monitoringComponent == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        //Undeploy
        return deploymentWrapper.undeployComponent(monitoringComponent);
    }

    /**
     * Replies to the requesting client with a map (monitoring adapter id -> monitoring state) of monitoring states
     * for each combination of a certain device and monitoring adapters that are compatible to this device.
     *
     * @param deviceId The id of the device for which the monitoring states are supposed to be retrieved
     * @return A response containing the map (monitoring adapter id -> monitoring state) of monitoring states
     */
    @GetMapping(value = "/monitoring/state/{deviceId}")
    public ResponseEntity<Map<String, ComponentState>> getDeviceMonitoringState(
            @PathVariable(value = "deviceId") String deviceId) {
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
        List<MonitoringAdapter> compatibleAdapterList = monitoringHelper.getCompatibleAdapters(device);

        //For each adapter create a new monitoring component and store it in a list
        List<Component> monitoringComponentList = new ArrayList<>();
        for (MonitoringAdapter monitoringAdapter : compatibleAdapterList) {
            MonitoringComponent monitoringComponent = new MonitoringComponent(monitoringAdapter, device);
            monitoringComponentList.add(monitoringComponent);
        }

        //Retrieve states for all monitoring components
        return deploymentWrapper.getStatesAllComponents(monitoringComponentList);
    }

    /**
     * Replies to the requesting client with the monitoring state of a certain device and monitoring adapter.
     *
     * @param deviceId            The id of the device
     * @param monitoringAdapterId The id of the monitoring adapter
     * @return A response containing the monitoring state
     */
    @RequestMapping(value = "/monitoring/state/{deviceId}", params = {"adapter"}, method = RequestMethod.GET)
    public ResponseEntity<Resource<ComponentState>> getMonitoringState(@PathVariable(value = "deviceId") String deviceId,
                                                                       @RequestParam("adapter") String monitoringAdapterId) {
        //Create new monitoring component
        MonitoringComponent monitoringComponent = monitoringHelper.createMonitoringComponent(deviceId, monitoringAdapterId);

        //Validity check
        if (monitoringComponent == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return deploymentWrapper.getComponentState(monitoringComponent);
    }

    /**
     * Returns a list of monitoring adapters that are compatible with a certain device.
     *
     * @param deviceId The id of the device for which the adapters are supposed to be retrieved
     * @return A list of all monitoring adapters that are compatible with the device
     */
    @GetMapping("/monitoring-adapters/by-device/{id}")
    public ResponseEntity<List<MonitoringAdapterExcerpt>> getCompatibleMonitoringAdaptersForDevice(@PathVariable(value = "id") String deviceId) {
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
        List<MonitoringAdapter> compatibleAdapters = monitoringHelper.getCompatibleAdapters(device);

        //Convert list of monitoring adapters into list of monitoring adapter list projections
        List<MonitoringAdapterExcerpt> adapterProjectionList =
                monitoringHelper.convertToListProjections(compatibleAdapters);

        return new ResponseEntity<>(adapterProjectionList, HttpStatus.OK);
    }

    /**
     * Returns a list of all monitoring components that are available. Each monitoring component consists out of
     * a device and a compatible monitoring adapter and is returned as a DTO.
     *
     * @return A list of all available monitoring components
     */
    @GetMapping("/monitoring")
    public ResponseEntity<List<MonitoringComponentDTO>> getAllMonitoringComponents() {
        //Create result list for all found monitoring components
        List<MonitoringComponentDTO> monitoringComponents = new ArrayList<>();

        //Get all devices
        List<Device> devices = deviceRepository.findAll();

        //Iterate over all devices
        for (Device device : devices) {
            //Get all compatible adapters for this device
            List<MonitoringAdapter> compatibleAdapters = monitoringHelper.getCompatibleAdapters(device);

            //Iterate over these adapters
            for (MonitoringAdapter monitoringAdapter : compatibleAdapters) {
                //Create corresponding monitoring component for device and adapter
                MonitoringComponent monitoringComponent = new MonitoringComponent(monitoringAdapter, device);

                //Add component as DTO to result list
                monitoringComponents.add(new MonitoringComponentDTO(monitoringComponent));
            }
        }

        return new ResponseEntity<>(monitoringComponents, HttpStatus.OK);
    }
}
