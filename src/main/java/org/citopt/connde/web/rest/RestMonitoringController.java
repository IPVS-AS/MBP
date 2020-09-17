package org.citopt.connde.web.rest;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.access_control.ACAccess;
import org.citopt.connde.domain.access_control.ACAccessRequest;
import org.citopt.connde.domain.access_control.ACAccessType;
import org.citopt.connde.domain.access_control.ACPolicy;
import org.citopt.connde.domain.adapter.Adapter;
import org.citopt.connde.domain.adapter.parameters.ParameterInstance;
import org.citopt.connde.domain.component.Component;
import org.citopt.connde.domain.device.Device;
import org.citopt.connde.domain.monitoring.MonitoringComponent;
import org.citopt.connde.domain.monitoring.MonitoringComponentDTO;
import org.citopt.connde.domain.user.User;
import org.citopt.connde.repository.DeviceRepository;
import org.citopt.connde.repository.projection.MonitoringAdapterExcerpt;
import org.citopt.connde.service.UserEntityService;
import org.citopt.connde.service.UserService;
import org.citopt.connde.service.access_control.ACPolicyEvaluationService;
import org.citopt.connde.service.deploy.ComponentState;
import org.citopt.connde.web.rest.helper.DeploymentWrapper;
import org.citopt.connde.web.rest.helper.MonitoringHelper;
import org.citopt.connde.web.rest.response.ActionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * REST Controller that exposes methods for the purpose of device monitoring.
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH)
@Api(tags = { "Monitoring" })
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

	@Autowired
	private ACPolicyEvaluationService policyEvaluationService;

	
	/**
	 * Indicates whether monitoring is currently active for a certain device
	 * and monitoring adapter.
	 *
	 * @param deviceId the id of the {@link Device}.
	 * @param monitoringAdapterId the id of the {@link Adapter}.
	 * @return {@code true} if the monitoring is active; {@code false} otherwise (embedded in response body).
	 */
	@GetMapping(value = "/monitoring/{deviceId}")
	@ApiOperation(value = "Indicates whether monitoring is active for a given device and monitoring adapter.", produces = "application/hal+json")
	@ApiResponses({ @ApiResponse(code = 200, message = "Success!"),
			@ApiResponse(code = 401, message = "Not authorized to access the device!"),
			@ApiResponse(code = 404, message = "Device, monitoring adapter or requesting user not found!") })
	public ResponseEntity<Boolean> isMonitoringActive(
			@PathVariable(value = "deviceId") @ApiParam(value = "ID of the device", example = "5c97dc2583aeb6078c5ab672", required = true) String deviceId,
			@RequestParam("adapterId") @ApiParam(value = "ID of the monitoring adapter", example = "5c97dc2583aeb6078c5ab672", required = true) String adapterId,
			@Valid @RequestBody ACAccessRequest<Void> accessRequest) {
		// Retrieve the currently logged in user from the database
		User user = userService.getLoggedInUser();

		// Create new monitoring component from parameters
		MonitoringComponent monitoringComponent = monitoringHelper.createMonitoringComponent(deviceId, adapterId);

		// Retrieve the policies from the database (we use the policies from the device and check for reading access permission)
		List<ACPolicy> policies = userEntityService.getPoliciesForEntity(monitoringComponent.getDevice());

		// Check for monitoring permission
		for (ACPolicy policy : policies) {
			if (!policyEvaluationService.evaluate(policy, new ACAccess(ACAccessType.READ, user, monitoringComponent.getDevice()), accessRequest)) {
				throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User '" + user.getUsername() + "' is not allowed to access device with id '" + deviceId + "'!");
			}
		}

		// Do check
		return ResponseEntity.ok(deploymentWrapper.isComponentRunning(monitoringComponent));
	}

	/**
	 * Starts monitoring for a certain device and monitoring adapter.
	 *
	 * @param deviceId the id of the {@link Device}.
	 * @param monitoringAdapterId the id of the {@link Adapter}.
	 * @param accessRequest the {@link ACAccessRequest} holding the context information of the requesting user
	 * 		  as well as the list of parameters.
	 * @return the {@link ActionResponse}.
	 */
	@PostMapping(value = "/monitoring/{deviceId}")
	@ApiOperation(value = "Enables monitoring for a given device and monitoring adapter with optional parameters", produces = "application/hal+json")
	@ApiResponses({ @ApiResponse(code = 201, message = "Success"),
			@ApiResponse(code = 400, message = "Invalid parameters provided!"),
			@ApiResponse(code = 401, message = "Not authorized to monitor the device!"),
			@ApiResponse(code = 404, message = "Device, monitoring adapter or requesting user not found!"),
			@ApiResponse(code = 500, message = "Monitoring failed due to an unexpected error!") })
	public ResponseEntity<ActionResponse> startMonitoring(
			@PathVariable(value = "deviceId") @ApiParam(value = "ID of the device", example = "5c97dc2583aeb6078c5ab672", required = true) String deviceId,
			@RequestParam("adapterId") @ApiParam(value = "ID of the monitoring adapter", example = "5c97dc2583aeb6078c5ab672", required = true) String monitoringAdapterId,
			@Valid @RequestBody @ApiParam(value = "Contains the context information for access-control and the list of monitoring parameters to use") ACAccessRequest<List<ParameterInstance>> accessRequest) {
		// Retrieve the currently logged in user from the database
		User user = userService.getLoggedInUser();
		
		// Create new monitoring component
		MonitoringComponent monitoringComponent = monitoringHelper.createMonitoringComponent(deviceId, monitoringAdapterId);
		
		// Check for monitoring permission
		requireMonitoringPermission(monitoringComponent.getDevice(), user, accessRequest);

		// Deploy monitoring component and check whether deployment succeeded
		if (!deploymentWrapper.deployComponent(monitoringComponent)) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred wihle deploying the component.");
		}

		// Start monitoring component
		ActionResponse response = deploymentWrapper.startComponent(monitoringComponent, accessRequest.getRequestBody());
		if (response.isSuccess()) {
			return ResponseEntity.status(HttpStatus.CREATED).build();
		} else {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(deploymentWrapper.startComponent(monitoringComponent, accessRequest.getRequestBody()));
		}
	}

	/**
	 * Stops monitoring for a certain device and monitoring adapter.
	 *
	 * @param deviceId the id of the {@link Device}.
	 * @param monitoringAdapterId the id of the {@link Adapter}.
	 * @param accessRequest the {@link ACAccessRequest} holding the context information of the requesting user
	 * 		  as well as the list of parameters.
	 * @return the {@link ActionResponse}.
	 */
	@DeleteMapping(value = "/monitoring/{deviceId}")
	@ApiOperation(value = "Disables monitoring for a given device and monitoring adapter", produces = "application/hal+json")
	@ApiResponses({ @ApiResponse(code = 200, message = "Success!"),
			@ApiResponse(code = 401, message = "Not authorized to monitor the device!"),
			@ApiResponse(code = 404, message = "Device, monitoring adapter or requesting user not found!"),
			@ApiResponse(code = 500, message = "Stopping monitoring failed due to an unexpected error!") })
	public ResponseEntity<ActionResponse> disableMonitoring(
			@PathVariable(value = "deviceId") @ApiParam(value = "ID of the device", example = "5c97dc2583aeb6078c5ab672", required = true) String deviceId,
			@RequestParam("adapterId") @ApiParam(value = "ID of the monitoring adapter", example = "5c97dc2583aeb6078c5ab672", required = true) String monitoringAdapterId,
			@Valid @RequestBody ACAccessRequest<Void> accessRequest) {
		// Retrieve the currently logged in user from the database
		User user = userService.getLoggedInUser();
		
		// Create new monitoring component
		MonitoringComponent monitoringComponent = monitoringHelper.createMonitoringComponent(deviceId, monitoringAdapterId);

		// Check for monitoring permission
		requireMonitoringPermission(monitoringComponent.getDevice(), user, accessRequest);

		// Stop monitoring (undeploy component)
		boolean result = deploymentWrapper.undeployComponent(monitoringComponent);
		if (result) {
			return ResponseEntity.ok().build();
		} else {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while undeploying the component.");
		}
	}

	
	@GetMapping(value = "/monitoring/state/{deviceId}")
	@ApiOperation(value = "Retrieves monitoring adapters and their current monitoring state that are available for a given device and the requesting user", produces = "application/hal+json")
	@ApiResponses({ @ApiResponse(code = 200, message = "Success!"),
			@ApiResponse(code = 401, message = "Not authorized to access/monitor the device!"),
			@ApiResponse(code = 404, message = "Device, monitoring adapter or requesting user not found!") })
	public ResponseEntity<Map<String, ComponentState>> getDeviceMonitoringState(
			@PathVariable(value = "deviceId") String deviceId,
			@Valid @RequestBody ACAccessRequest<Void> accessRequest) {
		// Retrieve the currently logged in user from the database
		User user = userService.getLoggedInUser();
		
		// Retrieve the device from the database
		Device device = userEntityService.getForIdWithPolicyCheck(deviceRepository, deviceId, ACAccessType.READ, accessRequest);
		
		// Check for monitoring permission
		requireMonitoringPermission(device, user, accessRequest);

		// Retrieve compatible adapters (includes only entities the requesting user has been granted access for)
		// and create a monitoring component for each one
		List<Component> monitoringComponents = monitoringHelper.getCompatibleAdapters(device, accessRequest)
				.stream().map(a -> new MonitoringComponent(a, device)).collect(Collectors.toList());

		// Retrieve states for all monitoring components
		return ResponseEntity.ok(deploymentWrapper.getStatesAllComponents(monitoringComponents));
	}

	@GetMapping(value = "/monitoring/state/{deviceId}", params = { "adapter" })
	@ApiOperation(value = "Retrieves the monitoring state for a given device and monitoring adapter", produces = "application/hal+json")
	@ApiResponses({ @ApiResponse(code = 200, message = "Success!"),
			@ApiResponse(code = 401, message = "Not authorized to access/monitor the device!"),
			@ApiResponse(code = 404, message = "Device, monitoring adapter or requesting user not found!") })
	public ResponseEntity<EntityModel<ComponentState>> getMonitoringState(
			@PathVariable(value = "deviceId") @ApiParam(value = "ID of the device", example = "5c97dc2583aeb6078c5ab672", required = true) String deviceId,
			@RequestParam("adapterId") @ApiParam(value = "ID of the monitoring adapter", example = "5c97dc2583aeb6078c5ab672", required = true) String monitoringAdapterId,
			@Valid @RequestBody ACAccessRequest<Void> accessRequest) {
		// Retrieve the currently logged in user from the database
		User user = userService.getLoggedInUser();
		
		// Create new monitoring component
		MonitoringComponent monitoringComponent = monitoringHelper.createMonitoringComponent(deviceId, monitoringAdapterId);

		// Check for monitoring permission
		requireMonitoringPermission(monitoringComponent.getDevice(), user, accessRequest);

		// Determine component state
		return ResponseEntity.ok(new EntityModel<ComponentState>(deploymentWrapper.getComponentState(monitoringComponent)));
	}

	@GetMapping("/monitoring-adapters/by-device/{id}")
	@ApiOperation(value = "Retrieves all monitoring adapters that are available for a given device and the requesting user", produces = "application/hal+json")
	@ApiResponses({ @ApiResponse(code = 200, message = "Success"),
			@ApiResponse(code = 401, message = "Not authorized to access/monitor the device!"),
			@ApiResponse(code = 404, message = "Device, monitoring adapter or requesting user not found!") })
	public ResponseEntity<List<MonitoringAdapterExcerpt>> getCompatibleMonitoringAdaptersForDevice(
			@PathVariable(value = "id", required = true) @NotEmpty String deviceId,
			@Valid @RequestBody ACAccessRequest<Void> accessRequest) {
		// Retrieve the currently logged in user from the database
		User user = userService.getLoggedInUser();
		
		// Retrieve the device from the database
		Device device = userEntityService.getForIdWithPolicyCheck(deviceRepository, deviceId, ACAccessType.READ, accessRequest);

		// Check for monitoring permission
		requireMonitoringPermission(device, user, accessRequest);

		// Get excerpt for each compatible adapter
		List<MonitoringAdapterExcerpt> compatibleAdapters =  monitoringHelper.getCompatibleAdapters(device, accessRequest)
				.stream()
				.map(monitoringHelper::adapterToExcerpt)
				.flatMap(o -> o.map(Stream::of).orElseGet(Stream::empty))
				.collect(Collectors.toList());

		return ResponseEntity.ok(compatibleAdapters);
	}

	/**
	 * Returns a list of all monitoring components that are available. Each
	 * monitoring component consists out of a device and a compatible monitoring
	 * adapter and is returned as a DTO.
	 *
	 * @return A list of all available monitoring components
	 */
	@GetMapping("/monitoring")
	@ApiOperation(value = "Retrieves all monitoring components that are available for the requesting user, each consisting out of a device and a monitoring adapter", produces = "application/hal+json")
	@ApiResponses({ @ApiResponse(code = 200, message = "Success") })
	public ResponseEntity<List<MonitoringComponentDTO>> getAllMonitoringComponents(@Valid @RequestBody ACAccessRequest<Void> accessRequest) {
		// Retrieve the currently logged in user from the database
		User user = userService.getLoggedInUser();
		
		// Retrieve all devices available for the requesting user
		List<MonitoringComponentDTO> monitoringComponents = userEntityService.getAllWithPolicyCheck(deviceRepository, ACAccessType.READ, accessRequest)
				.stream()
				// Filter devices based on policies
				.filter(d -> checkMonitoringPermission(d, user, accessRequest))
				// Get compatible monitoring adapter
				.map(d -> monitoringHelper.getCompatibleAdapters(d, accessRequest)
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
	
	private void requireMonitoringPermission(Device device, User requestingUser, ACAccessRequest<?> accessRequest) {
		if (!checkMonitoringPermission(device, requestingUser, accessRequest)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User '" + requestingUser.getUsername() + "' is not allowed to monior device with id '" + device.getId() + "'!");
		}
	}
	
	private boolean checkMonitoringPermission(Device device, User requestingUser, ACAccessRequest<?> accessRequest) {
		List<ACPolicy> policies = userEntityService.getPoliciesForEntity(device);
		for (ACPolicy policy : policies) {
			if (!policyEvaluationService.evaluate(policy, new ACAccess(ACAccessType.MONITOR, requestingUser, device), accessRequest)) {
				return false;
			}
		}
		return true;
	}
}
