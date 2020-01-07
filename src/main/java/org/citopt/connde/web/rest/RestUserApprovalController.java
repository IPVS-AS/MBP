package org.citopt.connde.web.rest;

import io.swagger.annotations.*;
import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.user.User;
import org.citopt.connde.domain.user_entity.UserEntity;
import org.citopt.connde.repository.AdapterRepository;
import org.citopt.connde.repository.DeviceRepository;
import org.citopt.connde.repository.UserEntityRepository;
import org.citopt.connde.service.UserEntityService;
import org.citopt.connde.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<Void> disapproveForAdapter(@PathVariable @ApiParam(value = "ID of the adapter to disapprove an user for", example = "5c97dc2583aeb6078c5ab672", required = true) String adapterId, @RequestBody @ApiParam(value = "Name of the user to disapprove", example = "johndoe", required = true) String username) {
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
    public ResponseEntity<Void> disapproveForDevice(@PathVariable @ApiParam(value = "ID of the device to disapprove an user for", example = "5c97dc2583aeb6078c5ab672", required = true) String deviceId, @RequestBody @ApiParam(value = "Name of the user to disapprove", example = "johndoe", required = true) String username) {
        return disapproveUserEntity(deviceId, username, deviceRepository);
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
