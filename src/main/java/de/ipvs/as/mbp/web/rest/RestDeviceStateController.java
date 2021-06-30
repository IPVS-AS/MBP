package de.ipvs.as.mbp.web.rest;

import de.ipvs.as.mbp.RestConfiguration;
import de.ipvs.as.mbp.domain.access_control.ACAccessRequest;
import de.ipvs.as.mbp.domain.access_control.ACAccessType;
import de.ipvs.as.mbp.domain.device.Device;
import de.ipvs.as.mbp.error.EntityNotFoundException;
import de.ipvs.as.mbp.error.MissingPermissionException;
import de.ipvs.as.mbp.repository.DeviceRepository;
import de.ipvs.as.mbp.service.user.UserEntityService;
import de.ipvs.as.mbp.service.deployment.DeployerDispatcher;
import de.ipvs.as.mbp.service.deployment.DeviceState;
import de.ipvs.as.mbp.service.deployment.IDeployer;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	private DeployerDispatcher deployerDispatcher;


	@GetMapping("/state")
	@ApiOperation(value = "Retrieves the availability state for all devices the requesting user is authorized for.", produces = "application/hal+json")
	@ApiResponses({ @ApiResponse(code = 200, message = "Success!"),
			@ApiResponse(code = 404, message = "Requesting user not found!") })
	public ResponseEntity<Map<String, DeviceState>> getStatusAllDevices(@RequestHeader("X-MBP-Access-Request") String accessRequestHeader) {
		// Create result map (device id -> device state)
		Map<String, DeviceState> deviceStates = new HashMap<>();

		// Get all devices
		List<Device> devices = userEntityService.getAllWithAccessControlCheck(deviceRepository, ACAccessType.READ, ACAccessRequest.valueOf(accessRequestHeader));

		//Find suitable deployer component
		IDeployer deployer = deployerDispatcher.getDeployer();

		// Iterate over all devices and determine the device state
		for (Device device : devices) {
			DeviceState state = deployer.retrieveDeviceState(device);
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
			@PathVariable(value = "deviceId") @ApiParam(value = "ID of the device", example = "5c97dc2583aeb6078c5ab672", required = true) String deviceId) throws EntityNotFoundException, MissingPermissionException {
		// Retrieve the device from the database
		Device device = userEntityService.getForIdWithAccessControlCheck(deviceRepository, deviceId, ACAccessType.READ, ACAccessRequest.valueOf(accessRequestHeader));

		//Find suitable deployer component
		IDeployer deployer = deployerDispatcher.getDeployer();

		// Determine device state
		DeviceState deviceState = deployer.retrieveDeviceState(device);

		return ResponseEntity.ok(new EntityModel<DeviceState>(deviceState));
	}
}
