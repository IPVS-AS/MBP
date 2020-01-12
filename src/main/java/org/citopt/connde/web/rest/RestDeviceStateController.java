package org.citopt.connde.web.rest;

import io.swagger.annotations.*;
import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.device.Device;
import org.citopt.connde.repository.DeviceRepository;
import org.citopt.connde.security.RestSecurityGuard;
import org.citopt.connde.service.UserEntityService;
import org.citopt.connde.service.deploy.DeviceState;
import org.citopt.connde.service.deploy.SSHDeployer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST Controller for requests related to the availability of devices.
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH)
@Api(tags = {"Device state"}, description = "Retrieval of device states")
public class RestDeviceStateController {

    @Autowired
    private RestSecurityGuard securityGuard;

    @Autowired
    private UserEntityService userEntityService;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private SSHDeployer sshDeployer;

    /**
     * Responds with the availability state for all devices in the device repository as a map.
     *
     * @return A map (device id -> device state) that contains the state of each device
     */
    @GetMapping("/devices/state")
    @ApiOperation(value = "Retrieves the availability state of all devices for which the user is authorized", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success")})
    public ResponseEntity<Map<String, DeviceState>> getStatusAllDevices() {
        //Create result map (device id -> device state)
        Map<String, DeviceState> resultMap = new HashMap<>();

        //Get all devices
        List<Device> deviceList = userEntityService.getUserEntitiesFromRepository(deviceRepository)
                .stream().map(entity -> (Device) entity).collect(Collectors.toList());

        //Iterate over all devices and determine the device state
        for (Device device : deviceList) {
            DeviceState state = sshDeployer.determineDeviceState(device);
            resultMap.put(device.getId(), state);
        }

        return new ResponseEntity<>(resultMap, HttpStatus.OK);
    }

    /**
     * Responds with the availability state for a certain device.
     *
     * @param deviceId The id of the device which state is supposed to be retrieved
     * @return The availability state of the device as plain string
     */
    @GetMapping(value = "/devices/state/{id}")
    @ApiOperation(value = "Retrieves the availability state for a device", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 403, message = "Not authorized to access the device"), @ApiResponse(code = 404, message = "Device not found")})
    public ResponseEntity<Resource<DeviceState>> getDeviceStatus(@PathVariable(value = "id") @ApiParam(value = "ID of the device", example = "5c97dc2583aeb6078c5ab672", required = true) String deviceId) {
        //Retrieve device from repository
        Device device = (Device) userEntityService.getUserEntityFromRepository(deviceRepository, deviceId);

        //Check if device could be found
        if (device == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        //Security check
        if (!securityGuard.checkPermission(device, "read")) {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }

        //Determine device state
        DeviceState deviceState = sshDeployer.determineDeviceState(device);

        //Wrap device state into resource
        Resource<DeviceState> stateResource = new Resource<>(deviceState);

        return new ResponseEntity<>(stateResource, HttpStatus.OK);
    }
}
