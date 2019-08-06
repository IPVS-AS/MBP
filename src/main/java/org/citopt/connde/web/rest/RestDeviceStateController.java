package org.citopt.connde.web.rest;

import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.device.Device;
import org.citopt.connde.repository.DeviceRepository;
import org.citopt.connde.service.UserService;
import org.citopt.connde.service.deploy.DeviceState;
import org.citopt.connde.service.deploy.SSHDeployer;
import org.springframework.beans.factory.annotation.Autowired;
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
 *
 * @author Jan
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH)
public class RestDeviceStateController {

    @Autowired
    private UserService userService;

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
    public ResponseEntity<Map<String, DeviceState>> getStatusAllDevices() {
        //Create result map (device id -> device state)
        Map<String, DeviceState> resultMap = new HashMap<>();

        //Get all devices
        List<Device> deviceList = userService.getUserEntitiesFromRepository(deviceRepository)
                .stream().map(entity -> (Device) entity).collect(Collectors.toList());

        //Iterate over all devices and determine the devie state
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
    @GetMapping("/devices/state/{id}")
    public ResponseEntity<DeviceState> getDeviceStatus(@PathVariable(value = "id") String deviceId) {
        //Retrieve device from repository
        Device device = (Device) userService.getUserEntityFromRepository(deviceRepository, deviceId);

        //Check if device could be found
        if (device == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        //Determine device state
        DeviceState deviceState = sshDeployer.determineDeviceState(device);

        return new ResponseEntity<DeviceState>(deviceState, HttpStatus.OK);
    }
}
