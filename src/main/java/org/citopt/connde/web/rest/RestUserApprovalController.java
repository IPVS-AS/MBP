package org.citopt.connde.web.rest;

import io.swagger.annotations.*;
import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.component.Actuator;
import org.citopt.connde.domain.component.Sensor;
import org.citopt.connde.domain.user.User;
import org.citopt.connde.domain.user_entity.UserEntity;
import org.citopt.connde.repository.*;
import org.citopt.connde.repository.projection.ComponentExcerpt;
import org.citopt.connde.service.UserEntityService;
import org.citopt.connde.service.UserService;
import org.citopt.connde.service.deploy.SSHDeployer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * REST Controller for approval and disapproval of users for user entities.
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH)
@Api(tags = {"User entity approval"}, description = "Approval and disapproval of useres for entities")
public class RestUserApprovalController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserEntityService userEntityService;

    @Autowired
    private AdapterRepository adapterRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private ActuatorRepository actuatorRepository;

    @Autowired
    private SensorRepository sensorRepository;

    @Autowired
    private SSHDeployer sshDeployer;

    @PostMapping("/adapters/{adapterId}/approve")
    @PreAuthorize("@restSecurityGuard.checkPermission(@adapterRepository.get(#adapterId), 'approve')")
    @ApiOperation(value = "Approves an user for an adapter entity", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 400, message = "User is already approved for this adapter"), @ApiResponse(code = 404, message = "Adapter or user not found or not authorized to access this adapter")})
    public ResponseEntity<Void> approveForAdapter(@PathVariable @ApiParam(value = "ID of the adapter to approve an user for", example = "5c97dc2583aeb6078c5ab672", required = true) String adapterId, @RequestBody @ApiParam(value = "Name of the user to approve", example = "johndoe", required = true) String username) {
        return approveUserEntity(adapterId, username, adapterRepository);
    }

    @PostMapping("/adapters/{adapterId}/disapprove")
    @PreAuthorize("@restSecurityGuard.checkPermission(@adapterRepository.get(#adapterId), 'disapprove')")
    @ApiOperation(value = "Disapproves an user for an adapter entity", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 400, message = "User cannot be disapproved for this adapter"), @ApiResponse(code = 404, message = "Adapter or user not found or not authorized to access this adapter")})
    public ResponseEntity<Void> disapproveForAdapter(@PathVariable @ApiParam(value = "ID of the adapter to disapprove an user for", example = "5c97dc2583aeb6078c5ab672", required = true) String adapterId, @RequestBody @ApiParam(value = "Name of the user to disapprove", example = "johndoe", required = true) String username) throws IOException {
        //TODO clean up and extract common helper method for this and the event handler method

        //Get all possibly affected actuators
        List<ComponentExcerpt> affectedActuators = actuatorRepository.findAllByAdapterId(adapterId);

        //Check actuators
        for (ComponentExcerpt actuatorExcerpt : affectedActuators) {
            //Get actuator entity
            Actuator actuator = actuatorRepository.get(actuatorExcerpt.getId());

            //Check if actuator is owned by the affected user
            if (actuator.getOwnerName().equals(username)) {
                //Undeploy actuator if necessary
                sshDeployer.undeployIfRunning(actuator);

                //Delete actuator
                actuatorRepository.delete(actuatorExcerpt.getId());
            }
        }

        //Get all possibly affected sensors
        List<ComponentExcerpt> affectedSensors = sensorRepository.findAllByAdapterId(adapterId);

        //Check actuators
        for (ComponentExcerpt sensorExcerpt : affectedSensors) {
            //Get sensor entity
            Sensor sensor = sensorRepository.get(sensorExcerpt.getId());

            //Check if sensor is owned by the affected user
            if (sensor.getOwnerName().equals(username)) {
                //Undeploy actuator if necessary
                sshDeployer.undeployIfRunning(sensor);

                //Delete actuator
                sensorRepository.delete(sensorExcerpt.getId());
            }
        }

        return disapproveUserEntity(adapterId, username, adapterRepository);
    }

    @PostMapping("/devices/{deviceId}/approve")
    @PreAuthorize("@restSecurityGuard.checkPermission(@deviceRepository.get(#deviceId), 'approve')")
    @ApiOperation(value = "Approves an user for a device entity", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 400, message = "User is already approved for this device"), @ApiResponse(code = 404, message = "Device or user not found or not authorized to access this device")})
    public ResponseEntity<Void> approveForDevice(@PathVariable @ApiParam(value = "ID of the device to approve an user for", example = "5c97dc2583aeb6078c5ab672", required = true) String deviceId, @RequestBody @ApiParam(value = "Name of the user to approve", example = "johndoe", required = true) String username) {
        return approveUserEntity(deviceId, username, deviceRepository);
    }


    @PostMapping("/devices/{deviceId}/disapprove")
    @PreAuthorize("@restSecurityGuard.checkPermission(@deviceRepository.get(#deviceId), 'disapprove')")
    @ApiOperation(value = "Disapproves an user for a device entity", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 400, message = "User cannot be disapproved for this device"), @ApiResponse(code = 404, message = "Device or user not found or not authorized to access this device")})
    public ResponseEntity<Void> disapproveForDevice(@PathVariable @ApiParam(value = "ID of the device to disapprove an user for", example = "5c97dc2583aeb6078c5ab672", required = true) String deviceId, @RequestBody @ApiParam(value = "Name of the user to disapprove", example = "johndoe", required = true) String username) throws IOException {
        //TODO clean up and extract common helper method for this and the event handler method

        //Get all possibly affected actuators
        List<ComponentExcerpt> affectedActuators = actuatorRepository.findAllByDeviceId(deviceId);

        //Check actuators
        for (ComponentExcerpt actuatorExcerpt : affectedActuators) {
            //Get actuator entity
            Actuator actuator = actuatorRepository.get(actuatorExcerpt.getId());

            //Check if actuator is owned by the affected user
            if (actuator.getOwnerName().equals(username)) {
                //Undeploy actuator if necessary
                sshDeployer.undeployIfRunning(actuator);

                //Delete actuator
                actuatorRepository.delete(actuatorExcerpt.getId());
            }
        }

        //Get all possibly affected sensors
        List<ComponentExcerpt> affectedSensors = sensorRepository.findAllByDeviceId(deviceId);

        //Check actuators
        for (ComponentExcerpt sensorExcerpt : affectedSensors) {
            //Get sensor entity
            Sensor sensor = sensorRepository.get(sensorExcerpt.getId());

            //Check if sensor is owned by the affected user
            if (sensor.getOwnerName().equals(username)) {

                //Undeploy actuator if necessary
                sshDeployer.undeployIfRunning(sensor);

                //Delete actuator
                sensorRepository.delete(sensorExcerpt.getId());
            }
        }

        return disapproveUserEntity(deviceId, username, deviceRepository);
    }

    @PostMapping("/actuators/{actuatorId}/approve")
    @PreAuthorize("@restSecurityGuard.checkPermission(@actuatorRepository.get(#actuatorId), 'approve')")
    @ApiOperation(value = "Approves an user for an actuator entity", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 400, message = "User is already approved for this actuator"), @ApiResponse(code = 404, message = "Actuator or user not found or not authorized to access this actuator")})
    public ResponseEntity<Void> approveForActuator(@PathVariable @ApiParam(value = "ID of the actuator to approve an user for", example = "5c97dc2583aeb6078c5ab672", required = true) String actuatorId, @RequestBody @ApiParam(value = "Name of the user to approve", example = "johndoe", required = true) String username) {
        return approveUserEntity(actuatorId, username, actuatorRepository);
    }


    @PostMapping("/actuators/{actuatorId}/disapprove")
    @PreAuthorize("@restSecurityGuard.checkPermission(@actuatorRepository.get(#actuatorId), 'disapprove')")
    @ApiOperation(value = "Disapproves an user for an actuator entity", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 400, message = "User cannot be disapproved for this actuator"), @ApiResponse(code = 404, message = "Actuator or user not found or not authorized to access this actuator")})
    public ResponseEntity<Void> disapproveForActuator(@PathVariable @ApiParam(value = "ID of the actuator to disapprove an user for", example = "5c97dc2583aeb6078c5ab672", required = true) String actuatorId, @RequestBody @ApiParam(value = "Name of the user to disapprove", example = "johndoe", required = true) String username) {
        return disapproveUserEntity(actuatorId, username, actuatorRepository);
    }

    @PostMapping("/sensors/{sensorId}/approve")
    @PreAuthorize("@restSecurityGuard.checkPermission(@sensorRepository.get(#sensorId), 'approve')")
    @ApiOperation(value = "Approves an user for a sensor entity", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 400, message = "User is already approved for this sensor"), @ApiResponse(code = 404, message = "Sensor or user not found or not authorized to access this sensor")})
    public ResponseEntity<Void> approveForSensor(@PathVariable @ApiParam(value = "ID of the sensor to approve an user for", example = "5c97dc2583aeb6078c5ab672", required = true) String sensorId, @RequestBody @ApiParam(value = "Name of the user to approve", example = "johndoe", required = true) String username) {
        return approveUserEntity(sensorId, username, sensorRepository);
    }


    @PostMapping("/sensors/{sensorId}/disapprove")
    @PreAuthorize("@restSecurityGuard.checkPermission(@sensorRepository.get(#sensorId), 'disapprove')")
    @ApiOperation(value = "Disapproves an user for a sensor entity", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 400, message = "User cannot be disapproved for this sensor"), @ApiResponse(code = 404, message = "Sensor or user not found or not authorized to access this sensor")})
    public ResponseEntity<Void> disapproveForSensor(@PathVariable @ApiParam(value = "ID of the sensor to disapprove an user for", example = "5c97dc2583aeb6078c5ab672", required = true) String sensorId, @RequestBody @ApiParam(value = "Name of the sensor to disapprove", example = "johndoe", required = true) String username) {
        return disapproveUserEntity(sensorId, username, sensorRepository);
    }

    /**
     * Tries to approve an user for an user entity from a repository and generates a corresponding response for
     * answering REST requests.
     *
     * @param userEntityId         The id of the entity to approve the user for
     * @param username             THe username of the user to approve
     * @param userEntityRepository The repository where the user entity can be found
     * @return A response containing the result of the approval attempt
     */
    private ResponseEntity<Void> approveUserEntity(String userEntityId, String username, UserEntityRepository userEntityRepository) {
        //Get user entity from repository by id
        UserEntity userEntity = userEntityService.getUserEntityFromRepository(userEntityRepository, userEntityId);

        //Check if entity could be found
        if (userEntity == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        //Get user by ID
        Optional<User> userOptional = userService.getUserWithAuthoritiesByUsername(username);

        //Check if user could be found
        if (!userOptional.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        //Get user from optional
        User candidateUser = userOptional.get();

        //Only non-approved and non-admin users may be approved
        if (candidateUser.isAdmin() || userEntity.isUserApproved(candidateUser)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        //Approve user
        userEntity.approveUser(candidateUser);
        userEntityRepository.save(userEntity);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Tries to disapprove an user for an user entity from a repository and generates a corresponding response for
     * answering REST requests.
     *
     * @param userEntityId         The id of the entity to disapprove the user for
     * @param username             THe username of the user to disapprove
     * @param userEntityRepository The repository where the user entity can be found
     * @return A response containing the result of the disapproval attempt
     */
    private ResponseEntity<Void> disapproveUserEntity(String userEntityId, String username, UserEntityRepository userEntityRepository) {
        //Get user entity from repository by id
        UserEntity userEntity = userEntityService.getUserEntityFromRepository(userEntityRepository, userEntityId);

        //Check if entity could be found
        if (userEntity == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        //Get user by ID
        Optional<User> userOptional = userService.getUserWithAuthoritiesByUsername(username);

        //Check if user could be found
        if (!userOptional.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        //Get user from optional
        User candidateUser = userOptional.get();

        //Only non-admin users, non-owners and already approved users may be disapproved
        if (candidateUser.isAdmin() || (userEntity.isUserOwner(candidateUser)) || (!userEntity.isUserApproved(candidateUser))) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        //Disapprove user
        userEntity.disapproveUser(candidateUser);
        userEntityRepository.save(userEntity);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
