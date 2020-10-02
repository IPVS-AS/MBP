package org.citopt.connde.web.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.access_control.ACAccessRequest;
import org.citopt.connde.domain.access_control.ACAccessType;
import org.citopt.connde.domain.device.Device;
import org.citopt.connde.error.EntityNotFoundException;
import org.citopt.connde.repository.DeviceRepository;
import org.citopt.connde.service.UserEntityService;
import org.citopt.connde.service.deploy.DeviceState;
import org.citopt.connde.service.deploy.SSHDeployer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * REST Controller for requests related to the availability of devices.
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH + "/devices")
@Api(tags = { "Device state" })
public class RestDeviceStateController {

	@Autowired
	private UserEntityService userEntityService;

	@Autowired
	private DeviceRepository deviceRepository;

	@Autowired
	private SSHDeployer sshDeployer;


	@GetMapping("/state")
	@ApiOperation(value = "Retrieves the availability state for all devices the requesting user is authorized for.", produces = "application/hal+json")
	@ApiResponses({ @ApiResponse(code = 200, message = "Success!"),
			@ApiResponse(code = 404, message = "Requesting user not found!") })
	public ResponseEntity<Map<String, DeviceState>> getStatusAllDevices(@RequestHeader("X-MBP-Access-Request") String accessRequestHeader) {
		// Create result map (device id -> device state)
		Map<String, DeviceState> deviceStates = new HashMap<>();

		// Get all devices
		List<Device> devices = userEntityService.getAllWithPolicyCheck(deviceRepository, ACAccessType.READ, ACAccessRequest.valueOf(accessRequestHeader));

		// Iterate over all devices and determine the device state
		for (Device device : devices) {
			DeviceState state = sshDeployer.determineDeviceState(device);
			deviceStates.put(device.getId(), state);
		}

		return ResponseEntity.ok(deviceStates);
	}

	@GetMapping(value = "/{deviceId}/state")
	@ApiOperation(value = "Retrieves the availability state for a device", produces = "application/hal+json")
	@ApiResponses({ @ApiResponse(code = 200, message = "Success!"),
			@ApiResponse(code = 401, message = "Not authorized to access the device!"),
			@ApiResponse(code = 404, message = "Device or requesting user not found!") })
	public ResponseEntity<EntityModel<DeviceState>> getDeviceStatus(
    		@RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
			@PathVariable(value = "deviceId") @ApiParam(value = "ID of the device", example = "5c97dc2583aeb6078c5ab672", required = true) String deviceId) throws EntityNotFoundException {
		// Retrieve the device from the database
		Device device = userEntityService.getForIdWithPolicyCheck(deviceRepository, deviceId, ACAccessType.READ, ACAccessRequest.valueOf(accessRequestHeader));

		// Determine device state
		DeviceState deviceState = sshDeployer.determineDeviceState(device);

		return ResponseEntity.ok(new EntityModel<DeviceState>(deviceState));
	}
}
