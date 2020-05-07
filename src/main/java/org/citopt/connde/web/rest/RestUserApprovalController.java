package org.citopt.connde.web.rest;

import io.swagger.annotations.*;
import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.component.Actuator;
import org.citopt.connde.domain.component.Sensor;
import org.citopt.connde.domain.device.Device;
import org.citopt.connde.domain.monitoring.MonitoringAdapter;
import org.citopt.connde.domain.monitoring.MonitoringComponent;
import org.citopt.connde.domain.rules.Rule;
import org.citopt.connde.domain.user.User;
import org.citopt.connde.domain.user_entity.UserEntity;
import org.citopt.connde.repository.*;
import org.citopt.connde.repository.projection.ComponentExcerpt;
import org.citopt.connde.service.UserEntityService;
import org.citopt.connde.service.UserService;
import org.citopt.connde.service.deploy.SSHDeployer;
import org.citopt.connde.service.rules.RuleEngine;
import org.citopt.connde.web.rest.helper.MonitoringHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST Controller for approval and disapproval of users for user entities.
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH)
@Api(tags = {"User entity approval"}, description = "Approval and disapproval of users for entities")
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
    private MonitoringAdapterRepository monitoringAdapterRepository;

    @Autowired
    private ActuatorRepository actuatorRepository;

    @Autowired
    private SensorRepository sensorRepository;

    @Autowired
    private RuleTriggerRepository ruleTriggerRepository;

    @Autowired
    private RuleActionRepository ruleActionRepository;

    @Autowired
    private RuleRepository ruleRepository;

    @Autowired
    private SSHDeployer sshDeployer;

    @Autowired
    private RuleEngine ruleEngine;

    @Autowired
    private MonitoringHelper monitoringHelper;

    @PostMapping("/adapters/{adapterId}/approve")
    @ApiOperation(value = "Approves an user for an adapter entity", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 400, message = "User is already approved for this adapter"), @ApiResponse(code = 403, message = "Not authorized to approve an user for this adapter"), @ApiResponse(code = 404, message = "Adapter or user not found")})
    public ResponseEntity<Void> approveForAdapter(@PathVariable @ApiParam(value = "ID of the adapter to approve an user for", example = "5c97dc2583aeb6078c5ab672", required = true) String adapterId, @RequestBody @ApiParam(value = "Name of the user to approve", example = "johndoe", required = true) String username) {
        return approveUserEntity(adapterId, username, adapterRepository);
    }

    @PostMapping("/adapters/{adapterId}/disapprove")
    @ApiOperation(value = "Disapproves an user for an adapter entity", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 400, message = "User cannot be disapproved for this adapter"), @ApiResponse(code = 403, message = "Not authorized to disapprove an user for this adapter"), @ApiResponse(code = 404, message = "Adapter or user not found")})
    public ResponseEntity<Void> disapproveForAdapter(@PathVariable @ApiParam(value = "ID of the adapter to disapprove an user for", example = "5c97dc2583aeb6078c5ab672", required = true) String adapterId, @RequestBody @ApiParam(value = "Name of the user to disapprove", example = "johndoe", required = true) String username) throws IOException {
        //TODO clean up and extract common helper method for this

        //Disapprove user for the entity
        ResponseEntity<Void> disapprovalResult = disapproveUserEntity(adapterId, username, adapterRepository);

        //Check result
        if (!disapprovalResult.getStatusCode().equals(HttpStatus.OK)) {
            return disapprovalResult;
        }

        //Get all possibly affected actuators
        List<ComponentExcerpt> affectedActuators = actuatorRepository.findAllByAdapterId(adapterId);

        //Check actuators
        for (ComponentExcerpt actuatorExcerpt : affectedActuators) {
            //Get actuator entity
            Actuator actuator = actuatorRepository.get(actuatorExcerpt.getId());

            //Check if actuator is owned by the affected user
            if (username.equals(actuator.getOwnerName())) {
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
            if (username.equals(sensor.getOwnerName())) {
                //Undeploy actuator if necessary
                sshDeployer.undeployIfRunning(sensor);

                //Delete actuator
                sensorRepository.delete(sensorExcerpt.getId());
            }
        }

        return disapprovalResult;
    }

    @PostMapping("/devices/{deviceId}/approve")
    @ApiOperation(value = "Approves an user for a device entity", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 400, message = "User is already approved for this device"), @ApiResponse(code = 403, message = "Not authorized to approve an user for this device"), @ApiResponse(code = 404, message = "Device or user not found")})
    public ResponseEntity<Void> approveForDevice(@PathVariable @ApiParam(value = "ID of the device to approve an user for", example = "5c97dc2583aeb6078c5ab672", required = true) String deviceId, @RequestBody @ApiParam(value = "Name of the user to approve", example = "johndoe", required = true) String username) {
        return approveUserEntity(deviceId, username, deviceRepository);
    }

    @PostMapping("/devices/{deviceId}/disapprove")
    @ApiOperation(value = "Disapproves an user for a device entity", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 400, message = "User cannot be disapproved for this device"), @ApiResponse(code = 403, message = "Not authorized to disapprove an user for this device"), @ApiResponse(code = 404, message = "Device or user not found")})
    public ResponseEntity<Void> disapproveForDevice(@PathVariable @ApiParam(value = "ID of the device to disapprove an user for", example = "5c97dc2583aeb6078c5ab672", required = true) String deviceId, @RequestBody @ApiParam(value = "Name of the user to disapprove", example = "johndoe", required = true) String username) throws IOException {
        //TODO clean up and extract common helper method for this

        //Disapprove user for the entity
        ResponseEntity<Void> disapprovalResult = disapproveUserEntity(deviceId, username, deviceRepository);

        //Check result
        if (!disapprovalResult.getStatusCode().equals(HttpStatus.OK)) {
            return disapprovalResult;
        }

        //Get all possibly affected actuators
        List<ComponentExcerpt> affectedActuators = actuatorRepository.findAllByDeviceId(deviceId);

        //Check actuators
        for (ComponentExcerpt actuatorExcerpt : affectedActuators) {
            //Get actuator entity
            Actuator actuator = actuatorRepository.get(actuatorExcerpt.getId());

            //Check if actuator is owned by the affected user
            if (username.equals(actuator.getOwnerName())) {
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
            if (username.equals(sensor.getOwnerName())) {

                //Undeploy actuator if necessary
                sshDeployer.undeployIfRunning(sensor);

                //Delete actuator
                sensorRepository.delete(sensorExcerpt.getId());
            }
        }

        //Get device from repository
        Device device = deviceRepository.get(deviceId);

        //Get all monitoring adapters that are compatible to the device
        List<MonitoringAdapter> compatibleMonitoringAdapters = monitoringHelper.getCompatibleAdapters(device);

        //Iterate over the compatible monitoring adapters
        for (MonitoringAdapter monitoringAdapter : compatibleMonitoringAdapters) {

            //Check if adapter is owned by the affected user
            if (username.equals(monitoringAdapter.getOwnerName())) {
                //Create monitoring component from monitoring adapter and device
                MonitoringComponent monitoringComponent = new MonitoringComponent(monitoringAdapter, device);

                //Undeploy monitoring component if necessary
                sshDeployer.undeployIfRunning(monitoringComponent);
            }
        }

        return disapprovalResult;
    }

    @PostMapping("/monitoring-adapters/{adapterId}/approve")
    @ApiOperation(value = "Approves an user for a monitoring adapter entity", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 400, message = "User is already approved for this monitoring adapter"), @ApiResponse(code = 403, message = "Not authorized to approve an user for this monitoring adapter"), @ApiResponse(code = 404, message = "Monitoring adapter or user not found")})
    public ResponseEntity<Void> approveForMonitoringAdapter(@PathVariable @ApiParam(value = "ID of the monitoring adapter to approve an user for", example = "5c97dc2583aeb6078c5ab672", required = true) String adapterId, @RequestBody @ApiParam(value = "Name of the user to approve", example = "johndoe", required = true) String username) {
        return approveUserEntity(adapterId, username, monitoringAdapterRepository);
    }

    @PostMapping("/monitoring-adapters/{adapterId}/disapprove")
    @ApiOperation(value = "Disapproves an user for a monitoring adapter entity", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 400, message = "User cannot be disapproved for this monitoring adapter"), @ApiResponse(code = 403, message = "Not authorized to disapprove an user for this monitoring adapter"), @ApiResponse(code = 404, message = "Monitoring adapter or user not found")})
    public ResponseEntity<Void> disapproveForMonitoringAdapter(@PathVariable @ApiParam(value = "ID of the monitoring adapter to disapprove an user for", example = "5c97dc2583aeb6078c5ab672", required = true) String adapterId, @RequestBody @ApiParam(value = "Name of the user to disapprove", example = "johndoe", required = true) String username) throws IOException {
        //TODO clean up and extract common helper method for this

        //Disapprove user for the entity
        ResponseEntity<Void> disapprovalResult = disapproveUserEntity(adapterId, username, monitoringAdapterRepository);

        //Check result
        if (!disapprovalResult.getStatusCode().equals(HttpStatus.OK)) {
            return disapprovalResult;
        }

        //Get monitoring adapter
        MonitoringAdapter monitoringAdapter = monitoringAdapterRepository.get(adapterId);

        //Get all devices that are compatible to the monitoring adapter
        List<Device> compatibleDevices = monitoringHelper.getCompatibleDevices(monitoringAdapter);

        //Iterate over the compatible devices
        for (Device device : compatibleDevices) {
            //Check if adapter is owned by the affected user
            if (username.equals(device.getOwnerName())) {
                //Create monitoring component from monitoring adapter and device
                MonitoringComponent monitoringComponent = new MonitoringComponent(monitoringAdapter, device);

                //Undeploy monitoring component if necessary
                sshDeployer.undeployIfRunning(monitoringComponent);
            }
        }


        return disapprovalResult;
    }

    @PostMapping("/actuators/{actuatorId}/approve")
    @ApiOperation(value = "Approves an user for an actuator entity", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 400, message = "User is already approved for this actuator"), @ApiResponse(code = 403, message = "Not authorized to approve an user for this actuator"), @ApiResponse(code = 404, message = "Actuator or user not found")})
    public ResponseEntity<Void> approveForActuator(@PathVariable @ApiParam(value = "ID of the actuator to approve an user for", example = "5c97dc2583aeb6078c5ab672", required = true) String actuatorId, @RequestBody @ApiParam(value = "Name of the user to approve", example = "johndoe", required = true) String username) {
        return approveUserEntity(actuatorId, username, actuatorRepository);
    }

    @PostMapping("/actuators/{actuatorId}/disapprove")
    @ApiOperation(value = "Disapproves an user for an actuator entity", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 400, message = "User cannot be disapproved for this actuator"), @ApiResponse(code = 403, message = "Not authorized to disapprove an user for this actuator"), @ApiResponse(code = 404, message = "Actuator or user not found")})
    public ResponseEntity<Void> disapproveForActuator(@PathVariable @ApiParam(value = "ID of the actuator to disapprove an user for", example = "5c97dc2583aeb6078c5ab672", required = true) String actuatorId, @RequestBody @ApiParam(value = "Name of the user to disapprove", example = "johndoe", required = true) String username) {
        return disapproveUserEntity(actuatorId, username, actuatorRepository);
    }

    @PostMapping("/sensors/{sensorId}/approve")
    @ApiOperation(value = "Approves an user for a sensor entity", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 400, message = "User is already approved for this sensor"), @ApiResponse(code = 403, message = "Not authorized to approve an user for this sensor"), @ApiResponse(code = 404, message = "Sensor or user not found")})
    public ResponseEntity<Void> approveForSensor(@PathVariable @ApiParam(value = "ID of the sensor to approve an user for", example = "5c97dc2583aeb6078c5ab672", required = true) String sensorId, @RequestBody @ApiParam(value = "Name of the user to approve", example = "johndoe", required = true) String username) {
        return approveUserEntity(sensorId, username, sensorRepository);
    }

    @PostMapping("/sensors/{sensorId}/disapprove")
    @ApiOperation(value = "Disapproves an user for a sensor entity", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 400, message = "User cannot be disapproved for this sensor"), @ApiResponse(code = 403, message = "Not authorized to disapprove an user for this sensor"), @ApiResponse(code = 404, message = "Sensor or user not found")})
    public ResponseEntity<Void> disapproveForSensor(@PathVariable @ApiParam(value = "ID of the sensor to disapprove an user for", example = "5c97dc2583aeb6078c5ab672", required = true) String sensorId, @RequestBody @ApiParam(value = "Name of the user to disapprove", example = "johndoe", required = true) String username) {
        return disapproveUserEntity(sensorId, username, sensorRepository);
    }

    @PostMapping("/rule-triggers/{triggerId}/approve")
    @ApiOperation(value = "Approves an user for a rule trigger entity", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 400, message = "User is already approved for this rule trigger"), @ApiResponse(code = 403, message = "Not authorized to approve an user for this rule trigger"), @ApiResponse(code = 404, message = "Rule trigger or user not found")})
    public ResponseEntity<Void> approveForRuleTrigger(@PathVariable @ApiParam(value = "ID of the rule trigger to approve an user for", example = "5c97dc2583aeb6078c5ab672", required = true) String triggerId, @RequestBody @ApiParam(value = "Name of the user to approve", example = "johndoe", required = true) String username) {
        return approveUserEntity(triggerId, username, ruleTriggerRepository);
    }

    @PostMapping("/rule-triggers/{triggerId}/disapprove")
    @ApiOperation(value = "Disapproves an user for a rule trigger entity", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 400, message = "User cannot be disapproved for this rule trigger"), @ApiResponse(code = 403, message = "Not authorized to disapprove an user for this rule trigger"), @ApiResponse(code = 404, message = "Rule trigger or user not found")})
    public ResponseEntity<Void> disapproveForRuleTrigger(@PathVariable @ApiParam(value = "ID of the rule trigger to disapprove an user for", example = "5c97dc2583aeb6078c5ab672", required = true) String triggerId, @RequestBody @ApiParam(value = "Name of the user to disapprove", example = "johndoe", required = true) String username) {
        //TODO clean up and extract common helper method for this

        //Disapprove user for the entity
        ResponseEntity<Void> disapprovalResult = disapproveUserEntity(triggerId, username, ruleTriggerRepository);

        //Check result
        if (!disapprovalResult.getStatusCode().equals(HttpStatus.OK)) {
            return disapprovalResult;
        }

        //Get all possibly affected rules
        List<Rule> affectedRules = ruleRepository.findAllByTriggerId(triggerId);

        //Check rules
        for (Rule affectedRule : affectedRules) {
            //Check if rule is owned by the affected user
            if (username.equals(affectedRule.getOwnerName())) {
                //Disable and delete rule
                ruleEngine.disableRule(affectedRule);
                ruleRepository.delete(affectedRule);
            }
        }

        return disapprovalResult;
    }

    @PostMapping("/rule-actions/{actionId}/approve")
    @ApiOperation(value = "Approves an user for a rule action entity", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 400, message = "User is already approved for this rule action"), @ApiResponse(code = 403, message = "Not authorized to approve an user for this rule action"), @ApiResponse(code = 404, message = "Rule action or user not found")})
    public ResponseEntity<Void> approveForRuleAction(@PathVariable @ApiParam(value = "ID of the rule action to approve an user for", example = "5c97dc2583aeb6078c5ab672", required = true) String actionId, @RequestBody @ApiParam(value = "Name of the user to approve", example = "johndoe", required = true) String username) {
        return approveUserEntity(actionId, username, ruleActionRepository);
    }

    @PostMapping("/rule-actions/{actionId}/disapprove")
    @ApiOperation(value = "Disapproves an user for a rule action entity", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 400, message = "User cannot be disapproved for this rule action"), @ApiResponse(code = 403, message = "Not authorized to disapprove an user for this rule action"), @ApiResponse(code = 404, message = "Rule action or user not found")})
    public ResponseEntity<Void> disapproveForRuleAction(@PathVariable @ApiParam(value = "ID of the rule action to disapprove an user for", example = "5c97dc2583aeb6078c5ab672", required = true) String actionId, @RequestBody @ApiParam(value = "Name of the user to disapprove", example = "johndoe", required = true) String username) {
        //Disapprove user for the entity
        ResponseEntity<Void> disapprovalResult = disapproveUserEntity(actionId, username, ruleActionRepository);

        //Check result
        if (!disapprovalResult.getStatusCode().equals(HttpStatus.OK)) {
            return disapprovalResult;
        }

        //Get all possibly affected rules
        List<Rule> affectedRules = ruleRepository.findAll()
                .stream() //Consider each rule in repository
                //Filter for rules that contain an action with matching id
                .filter(rule -> rule.getActions().stream().anyMatch(action -> action.getId().equals(actionId)))
                .collect(Collectors.toList());

        //Check rules
        for (Rule affectedRule : affectedRules) {
            //Check if rule is owned by the affected user
            if (username.equals(affectedRule.getOwnerName())) {
                //Disable and delete rule
                ruleEngine.disableRule(affectedRule);
                ruleRepository.delete(affectedRule);
            }
        }

        return disapprovalResult;
    }

    @PostMapping("/rules/{ruleId}/approve")
    @ApiOperation(value = "Approves an user for a rule entity", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 400, message = "User is already approved for this rule"), @ApiResponse(code = 403, message = "Not authorized to approve an user for this rule"), @ApiResponse(code = 404, message = "Rule or user not found")})
    public ResponseEntity<Void> approveForRule(@PathVariable @ApiParam(value = "ID of the rule to approve an user for", example = "5c97dc2583aeb6078c5ab672", required = true) String ruleId, @RequestBody @ApiParam(value = "Name of the user to approve", example = "johndoe", required = true) String username) {
        return approveUserEntity(ruleId, username, ruleRepository);
    }

    @PostMapping("/rules/{ruleId}/disapprove")
    @ApiOperation(value = "Disapproves an user for a rule entity", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 400, message = "User cannot be disapproved for this rule"), @ApiResponse(code = 403, message = "Not authorized to disapprove an user for this rule"), @ApiResponse(code = 404, message = "Rule or user not found")})
    public ResponseEntity<Void> disapproveForRule(@PathVariable @ApiParam(value = "ID of the rule to disapprove an user for", example = "5c97dc2583aeb6078c5ab672", required = true) String ruleId, @RequestBody @ApiParam(value = "Name of the user to disapprove", example = "johndoe", required = true) String username) {
        return disapproveUserEntity(ruleId, username, ruleRepository);
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
        UserEntity userEntity = userEntityRepository.get(userEntityId);

        //Check if entity could be found
        if (userEntity == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        //Check if user is permitted to approve
        if (!userEntityService.isUserPermitted(userEntity, "approve")) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
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
        UserEntity userEntity = userEntityRepository.get(userEntityId);

        //Check if entity could be found
        if (userEntity == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        //Check if user is permitted to disapprove
        if (!userEntityService.isUserPermitted(userEntity, "disapprove")) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
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
