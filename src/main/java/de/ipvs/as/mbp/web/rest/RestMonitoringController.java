package de.ipvs.as.mbp.web.rest;

import de.ipvs.as.mbp.RestConfiguration;
import de.ipvs.as.mbp.domain.access_control.ACAccessRequest;
import de.ipvs.as.mbp.domain.access_control.ACAccessType;
import de.ipvs.as.mbp.domain.component.Component;
import de.ipvs.as.mbp.domain.device.Device;
import de.ipvs.as.mbp.domain.monitoring.MonitoringComponent;
import de.ipvs.as.mbp.domain.monitoring.MonitoringComponentDTO;
import de.ipvs.as.mbp.domain.operator.Operator;
import de.ipvs.as.mbp.domain.operator.parameters.ParameterInstance;
import de.ipvs.as.mbp.domain.user.User;
import de.ipvs.as.mbp.error.EntityNotFoundException;
import de.ipvs.as.mbp.error.MissingPermissionException;
import de.ipvs.as.mbp.repository.DeviceRepository;
import de.ipvs.as.mbp.repository.projection.MonitoringOperatorExcerpt;
import de.ipvs.as.mbp.service.user.UserEntityService;
import de.ipvs.as.mbp.service.user.UserService;
import de.ipvs.as.mbp.service.deployment.ComponentState;
import de.ipvs.as.mbp.web.rest.helper.DeploymentWrapper;
import de.ipvs.as.mbp.web.rest.helper.MonitoringHelper;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * REST Controller that exposes methods for the purpose of device monitoring.
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH)
@Api(tags = {"Monitoring"})
public class RestMonitoringController {

    @Autowired
    private UserEntityService userEntityService;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private DeploymentWrapper deploymentWrapper;

    @Autowired
    private MonitoringHelper monitoringHelper;

    @Autowired
    private UserService userService;


    /**
     * Indicates whether monitoring is currently active for a certain device
     * and monitoring operator.
     *
     * @param deviceId             the id of the {@link Device}.
     * @param monitoringOperatorId the id of the {@link Operator}.
     * @return {@code true} if the monitoring is active; {@code false} otherwise (embedded in response body).
     * @throws EntityNotFoundException
     * @throws MissingPermissionException
     */
    @GetMapping(value = "/monitoring/{deviceId}")
    @ApiOperation(value = "Indicates whether monitoring is active for a given device and monitoring operator.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"),
            @ApiResponse(code = 401, message = "Not authorized to access the device!"),
            @ApiResponse(code = 404, message = "Device, monitoring operator or requesting user not found!")})
    public ResponseEntity<Boolean> isMonitoringActive(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @PathVariable(value = "deviceId") @ApiParam(value = "ID of the device", example = "5c97dc2583aeb6078c5ab672", required = true) String deviceId,
            @RequestParam("monitoringOperatorId") @ApiParam(value = "ID of the monitoring operator", example = "5c97dc2583aeb6078c5ab672", required = true) String monitoringOperatorId,
            @RequestBody @ApiParam(value = "The list of monitoring parameters to use") List<ParameterInstance> parameters) throws EntityNotFoundException, MissingPermissionException {
        // Parse the access-request information
        ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);

        // Create new monitoring component from parameters
        MonitoringComponent monitoringComponent = monitoringHelper.createMonitoringComponent(deviceId, monitoringOperatorId);

        // Check permission
        userEntityService.requirePermission(monitoringComponent.getDevice(), ACAccessType.READ, accessRequest);

        // Do check
        return ResponseEntity.ok(deploymentWrapper.isComponentRunning(monitoringComponent));
    }

    @PostMapping(value = "/monitoring/{deviceId}")
    @ApiOperation(value = "Enables monitoring for a given device and monitoring operator with optional parameters", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 201, message = "Success"),
            @ApiResponse(code = 400, message = "Invalid parameters provided!"),
            @ApiResponse(code = 401, message = "Not authorized to monitor the device!"),
            @ApiResponse(code = 404, message = "Device, monitoring operator or requesting user not found!"),
            @ApiResponse(code = 500, message = "Monitoring failed due to an unexpected error!")})
    public ResponseEntity<Void> startMonitoring(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @PathVariable(value = "deviceId") @ApiParam(value = "ID of the device", example = "5c97dc2583aeb6078c5ab672", required = true) String deviceId,
            @RequestParam("monitoringOperatorId") @ApiParam(value = "ID of the monitoring operator", example = "5c97dc2583aeb6078c5ab672", required = true) String monitoringOperatorId,
            @RequestBody @ApiParam(value = "The list of monitoring parameters to use") List<ParameterInstance> parameters) throws EntityNotFoundException, MissingPermissionException {
        // Parse the access-request information
        ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);

        // Retrieve the currently logged in user from the database
        User user = userService.getLoggedInUser();

        // Create new monitoring component
        MonitoringComponent monitoringComponent = monitoringHelper.createMonitoringComponent(deviceId, monitoringOperatorId);

        // Check for monitoring permission
        requireMonitoringPermission(monitoringComponent.getDevice(), user, accessRequest);

        // Deploy monitoring component
        deploymentWrapper.deployComponent(monitoringComponent);

        // Start monitoring component
        deploymentWrapper.startComponent(monitoringComponent, parameters);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    @DeleteMapping(value = "/monitoring/{deviceId}")
    @ApiOperation(value = "Disables monitoring for a given device and monitoring operator", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"),
            @ApiResponse(code = 401, message = "Not authorized to monitor the device!"),
            @ApiResponse(code = 404, message = "Device, monitoring operator or requesting user not found!"),
            @ApiResponse(code = 500, message = "Stopping monitoring failed due to an unexpected error!")})
    public ResponseEntity<Void> disableMonitoring(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @PathVariable(value = "deviceId") @ApiParam(value = "ID of the device", example = "5c97dc2583aeb6078c5ab672", required = true) String deviceId,
            @RequestParam("monitoringOperatorId") @ApiParam(value = "ID of the monitoring operator", example = "5c97dc2583aeb6078c5ab672", required = true) String monitoringOperatorId) throws EntityNotFoundException, MissingPermissionException {
        // Parse the access-request information
        ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);

        // Retrieve the currently logged in user from the database
        User user = userService.getLoggedInUser();

        // Create new monitoring component
        MonitoringComponent monitoringComponent = monitoringHelper.createMonitoringComponent(deviceId, monitoringOperatorId);

        // Check for monitoring permission
        requireMonitoringPermission(monitoringComponent.getDevice(), user, accessRequest);

        // Stop monitoring (undeploy component)
        deploymentWrapper.undeployComponent(monitoringComponent);
        return ResponseEntity.ok().build();
    }


    @GetMapping(value = "/monitoring/state/{deviceId}")
    @ApiOperation(value = "Retrieves monitoring operators and their current monitoring state that are available for a given device and the requesting user", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"),
            @ApiResponse(code = 401, message = "Not authorized to access/monitor the device!"),
            @ApiResponse(code = 404, message = "Device, monitoring operator or requesting user not found!")})
    public ResponseEntity<Map<String, ComponentState>> getDeviceMonitoringState(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @PathVariable(value = "deviceId") String deviceId) throws EntityNotFoundException, MissingPermissionException {
        // Parse the access-request information
        ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);

        // Retrieve the currently logged in user from the database
        User user = userService.getLoggedInUser();

        // Retrieve the device from the database
        Device device = userEntityService.getForIdWithAccessControlCheck(deviceRepository, deviceId, ACAccessType.READ, accessRequest);

        // Check for monitoring permission
        requireMonitoringPermission(device, user, accessRequest);

        // Retrieve compatible operators (includes only entities the requesting user has been granted access for)
        // and create a monitoring component for each one
        List<Component> monitoringComponents = monitoringHelper.getCompatibleOperators(device, accessRequest)
                .stream().map(a -> new MonitoringComponent(a, device)).collect(Collectors.toList());

        // Retrieve states for all monitoring components
        return ResponseEntity.ok(deploymentWrapper.getStatesAllComponents(monitoringComponents));
    }

    @GetMapping(value = "/monitoring/state/{deviceId}", params = {"monitoringOperatorId"})
    @ApiOperation(value = "Retrieves the monitoring state for a given device and monitoring operator", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"),
            @ApiResponse(code = 401, message = "Not authorized to access/monitor the device!"),
            @ApiResponse(code = 404, message = "Device, monitoring operator or requesting user not found!")})
    public ResponseEntity<EntityModel<ComponentState>> getMonitoringState(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @PathVariable(value = "deviceId") @ApiParam(value = "ID of the device", example = "5c97dc2583aeb6078c5ab672", required = true) String deviceId,
            @RequestParam("monitoringOperatorId") @ApiParam(value = "ID of the monitoring operator", example = "5c97dc2583aeb6078c5ab672", required = true) String monitoringOperatorId) throws EntityNotFoundException, MissingPermissionException {
        // Parse the access-request information
        ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);

        // Retrieve the currently logged in user from the database
        User user = userService.getLoggedInUser();

        // Create new monitoring component
        MonitoringComponent monitoringComponent = monitoringHelper.createMonitoringComponent(deviceId, monitoringOperatorId);

        // Check for monitoring permission
        requireMonitoringPermission(monitoringComponent.getDevice(), user, accessRequest);

        // Determine component state
        return ResponseEntity.ok(new EntityModel<ComponentState>(deploymentWrapper.getComponentState(monitoringComponent)));
    }

    @GetMapping("/monitoring-operators/by-device/{id}")
    @ApiOperation(value = "Retrieves all monitoring operators that are available for a given device and the requesting user", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 401, message = "Not authorized to access/monitor the device!"),
            @ApiResponse(code = 404, message = "Device, monitoring operator or requesting user not found!")})
    public ResponseEntity<List<MonitoringOperatorExcerpt>> getCompatibleMonitoringOperatorsForDevice(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @PathVariable(value = "id", required = true) @NotEmpty String deviceId) throws EntityNotFoundException, MissingPermissionException {
        // Parse the access-request information
        ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);

        // Retrieve the currently logged in user from the database
        User user = userService.getLoggedInUser();

        // Retrieve the device from the database
        Device device = userEntityService.getForIdWithAccessControlCheck(deviceRepository, deviceId, ACAccessType.READ, accessRequest);

        // Check for monitoring permission
        requireMonitoringPermission(device, user, accessRequest);

        // Get excerpt for each compatible operator
        List<MonitoringOperatorExcerpt> compatibleOperators = monitoringHelper.getCompatibleOperators(device, accessRequest)
                .stream()
                .map(monitoringHelper::operatorToExcerpt)
                .flatMap(o -> o.map(Stream::of).orElseGet(Stream::empty))
                .collect(Collectors.toList());

        return ResponseEntity.ok(compatibleOperators);
    }

    /**
     * Returns a list of all monitoring components that are available. Each
     * monitoring component consists out of a device and a compatible monitoring
     * operator and is returned as a DTO.
     *
     * @return A list of all available monitoring components
     */
    @GetMapping("/monitoring")
    @ApiOperation(value = "Retrieves all monitoring components that are available for the requesting user, each consisting out of a device and a monitoring operator", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success")})
    public ResponseEntity<List<MonitoringComponentDTO>> getAllMonitoringComponents(@RequestHeader("X-MBP-Access-Request") String accessRequestHeader) {
        // Parse the access-request information
        ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);

        // Retrieve all devices available for the requesting user
        List<MonitoringComponentDTO> monitoringComponents = userEntityService.getAllWithAccessControlCheck(deviceRepository, ACAccessType.READ, accessRequest)
                .stream()
                // Filter devices based on policies
                .filter(d -> userEntityService.checkPermission(d, ACAccessType.MONITOR, accessRequest))
                // Get compatible monitoring operator
                .map(d -> monitoringHelper.getCompatibleOperators(d, accessRequest)
                        .stream()
                        // Map to monitoring components
                        .map(a -> new MonitoringComponent(a, d))
                        .collect(Collectors.toList()))
                .flatMap(Collection::stream)
                // Map to DTO
                .map(MonitoringComponentDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(monitoringComponents);
    }

    private void requireMonitoringPermission(Device device, User requestingUser, ACAccessRequest accessRequest) throws MissingPermissionException {
        userEntityService.requirePermission(device, ACAccessType.MONITOR, accessRequest);
    }

//	private boolean checkMonitoringPermission(Device device, User requestingUser, ACAccessRequest accessRequest) {
//		List<ACPolicy> policies = userEntityService.getPoliciesForEntity(device);
//		for (ACPolicy policy : policies) {
//			if (!policyEvaluationService.evaluate(policy, new ACAccess(ACAccessType.MONITOR, requestingUser, device), accessRequest)) {
//				return false;
//			}
//		}
//		return true;
//	}
}
