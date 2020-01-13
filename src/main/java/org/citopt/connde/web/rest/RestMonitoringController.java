package org.citopt.connde.web.rest;

import io.swagger.annotations.*;
import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.adapter.parameters.ParameterInstance;
import org.citopt.connde.domain.component.Component;
import org.citopt.connde.domain.device.Device;
import org.citopt.connde.domain.monitoring.MonitoringAdapter;
import org.citopt.connde.domain.monitoring.MonitoringComponent;
import org.citopt.connde.domain.monitoring.MonitoringComponentDTO;
import org.citopt.connde.domain.user_entity.UserEntity;
import org.citopt.connde.repository.DeviceRepository;
import org.citopt.connde.repository.MonitoringAdapterRepository;
import org.citopt.connde.repository.projection.MonitoringAdapterExcerpt;
import org.citopt.connde.service.UserEntityService;
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
import java.util.stream.Collectors;

/**
 * REST Controller that exposes methods for the purpose of device monitoring.
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH)
@Api(tags = {"Monitoring"}, description = "Monitoring of devices")
public class RestMonitoringController {

    @Autowired
    private UserEntityService userEntityService;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private MonitoringAdapterRepository monitoringAdapterRepository;

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
    @ApiOperation(value = "Checks whether monitoring is active for a given device and monitoring adapter", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 403, message = "Not authorized to monitor the device"), @ApiResponse(code = 404, message = "Device or monitoring adapter not found or not authorized to access them"), @ApiResponse(code = 500, message = "Check failed due to an unexpected I/O error")})
    public ResponseEntity<Boolean> isMonitoringActive(@PathVariable(value = "deviceId") @ApiParam(value = "ID of the device", example = "5c97dc2583aeb6078c5ab672", required = true) String deviceId,
                                                      @RequestParam("adapter") @ApiParam(value = "ID of the monitoring adapter", example = "5c97dc2583aeb6078c5ab672", required = true) String monitoringAdapterId) {
        //Create new monitoring component from parameters
        MonitoringComponent monitoringComponent =
                monitoringHelper.createMonitoringComponent(deviceId, monitoringAdapterId);

        //Validity check
        if (monitoringComponent == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        //Check for monitoring permission
        if (!userEntityService.isUserPermitted(monitoringComponent.getDevice(), "monitor")) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
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
    @ApiOperation(value = "Enables monitoring for a given device and monitoring adapter with optional parameters", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 201, message = "Success"), @ApiResponse(code = 400, message = "Invalid parameters provided"), @ApiResponse(code = 403, message = "Not authorized to monitor the device"), @ApiResponse(code = 404, message = "Device or monitoring adapter not found or not authorized to access them"), @ApiResponse(code = 500, message = "Enabling failed due to an unexpected I/O error")})
    public ResponseEntity<ActionResponse> enableMonitoring(@PathVariable(value = "deviceId") @ApiParam(value = "ID of the device", example = "5c97dc2583aeb6078c5ab672", required = true) String deviceId,
                                                           @RequestParam("adapter") @ApiParam(value = "ID of the monitoring adapter", example = "5c97dc2583aeb6078c5ab672", required = true) String monitoringAdapterId,
                                                           @RequestBody @ApiParam(value = "List of monitoring parameter instances to use") List<ParameterInstance> parameters) {

        //Create new monitoring component
        MonitoringComponent monitoringComponent =
                monitoringHelper.createMonitoringComponent(deviceId, monitoringAdapterId);

        //Validity check
        if (monitoringComponent == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        //Check for monitoring permission
        if (!userEntityService.isUserPermitted(monitoringComponent.getDevice(), "monitor")) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
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
    @ApiOperation(value = "Disables monitoring for a given device and monitoring adapter", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 403, message = "Not authorized to monitor the device"), @ApiResponse(code = 404, message = "Device or monitoring adapter not found or not authorized to access them"), @ApiResponse(code = 500, message = "Disabling failed due to an unexpected I/O error")})
    public ResponseEntity<ActionResponse> disableMonitoring(@PathVariable(value = "deviceId") @ApiParam(value = "ID of the device", example = "5c97dc2583aeb6078c5ab672", required = true) String deviceId,
                                                            @RequestParam("adapter") @ApiParam(value = "ID of the monitoring adapter", example = "5c97dc2583aeb6078c5ab672", required = true) String monitoringAdapterId) {
        //Create new monitoring component
        MonitoringComponent monitoringComponent =
                monitoringHelper.createMonitoringComponent(deviceId, monitoringAdapterId);

        //Validity check
        if (monitoringComponent == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        //Check for monitoring permission
        if (!userEntityService.isUserPermitted(monitoringComponent.getDevice(), "monitor")) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
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
    @ApiOperation(value = "Retrieves monitoring adapters and their current monitoring state that are available for a given device and the current user", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 403, message = "Not authorized to monitor the device"), @ApiResponse(code = 404, message = "Device not found")})
    public ResponseEntity<Map<String, ComponentState>> getDeviceMonitoringState(
            @PathVariable(value = "deviceId") String deviceId) {
        //Fetch device object
        Device device = deviceRepository.get(deviceId);

        //Check if device could be found
        if (device == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        //Check for monitoring permission
        if (!userEntityService.isUserPermitted(device, "monitor")) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        //Retrieve compatible adapters
        List<MonitoringAdapter> compatibleAdapterList = monitoringHelper.getCompatibleAdapters(device);

        //For each adapter create a new monitoring component and store it in a list
        List<Component> monitoringComponentList = new ArrayList<>();
        for (MonitoringAdapter monitoringAdapter : compatibleAdapterList) {
            //Check user permission for the monitoring adapter
            if (!monitoringAdapter.isReadable()) {
                continue;
            }

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
    @GetMapping(value = "/monitoring/state/{deviceId}", params = {"adapter"})
    @ApiOperation(value = "Retrieves the monitoring state for a given device and monitoring adapter", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 403, message = "Not authorized to monitor the device"), @ApiResponse(code = 404, message = "Device or monitoring adapter not found or not authorized to access them")})
    public ResponseEntity<Resource<ComponentState>> getMonitoringState(@PathVariable(value = "deviceId") @ApiParam(value = "ID of the device", example = "5c97dc2583aeb6078c5ab672", required = true) String deviceId,
                                                                       @RequestParam("adapter") @ApiParam(value = "ID of the monitoring adapter", example = "5c97dc2583aeb6078c5ab672", required = true) String monitoringAdapterId) {
        //Create new monitoring component
        MonitoringComponent monitoringComponent = monitoringHelper.createMonitoringComponent(deviceId, monitoringAdapterId);

        //Validity check
        if (monitoringComponent == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        //Check for monitoring permission
        if (!userEntityService.isUserPermitted(monitoringComponent.getDevice(), "monitor")) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
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
    @ApiOperation(value = "Retrieves all monitoring adapters that are available for a given device and the current user", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 403, message = "Not authorized to monitor the device"), @ApiResponse(code = 404, message = "Device not found or not authorized to access it")})
    public ResponseEntity<List<MonitoringAdapterExcerpt>> getCompatibleMonitoringAdaptersForDevice(@PathVariable(value = "id") String deviceId) {
        //Validity check
        if ((deviceId == null) || deviceId.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        //Fetch device object
        Device device = (Device) userEntityService.getUserEntityFromRepository(deviceRepository, deviceId);

        //Check if device could be found
        if (device == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        //Check for monitoring permission
        if (!userEntityService.isUserPermitted(device, "monitor")) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        //Get excerpts of available compatible adapters for the device
        List<MonitoringAdapterExcerpt> compatibleAdapters = monitoringHelper.getCompatibleAdapters(device)
                .stream()
                .filter(UserEntity::isReadable) //Filter for adapters which can be accessed by the current user
                .map(adapter -> monitoringAdapterRepository.findById(adapter.getId())) //Convert to projection
                .collect(Collectors.toList());

        return new ResponseEntity<>(compatibleAdapters, HttpStatus.OK);
    }

    /**
     * Returns a list of all monitoring components that are available. Each monitoring component consists out of
     * a device and a compatible monitoring adapter and is returned as a DTO.
     *
     * @return A list of all available monitoring components
     */
    @GetMapping("/monitoring")
    @ApiOperation(value = "Retrieves all monitoring components that are available for the current user, each consisting out of a device and a monitoring adapter", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success")})
    public ResponseEntity<List<MonitoringComponentDTO>> getAllMonitoringComponents() {
        //Create result list for all found monitoring components
        List<MonitoringComponentDTO> monitoringComponents = new ArrayList<>();

        //Get all devices the current user has access to
        List<UserEntity> deviceList = userEntityService.getUserEntitiesFromRepository(deviceRepository);

        //Iterate over all devices
        for (UserEntity userEntity : deviceList) {
            //Skip device if user has no monitoring permission for it
            if (!userEntityService.isUserPermitted(userEntity, "monitor")) {
                continue;
            }

            //Cast user entity to device
            Device device = (Device) userEntity;

            //Get all compatible adapters for this device
            List<MonitoringAdapter> compatibleAdapters = monitoringHelper.getCompatibleAdapters(device);

            //Iterate over these adapters
            for (MonitoringAdapter monitoringAdapter : compatibleAdapters) {
                //Skip monitoring adapters the user has no access to
                if (!monitoringAdapter.isReadable()) {
                    continue;
                }

                //Create monitoring component from device and adapter
                MonitoringComponent monitoringComponent = new MonitoringComponent(monitoringAdapter, device);

                //Add component as DTO to result list
                monitoringComponents.add(new MonitoringComponentDTO(monitoringComponent));
            }
        }

        return new ResponseEntity<>(monitoringComponents, HttpStatus.OK);
    }
}
