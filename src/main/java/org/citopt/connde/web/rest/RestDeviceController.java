package org.citopt.connde.web.rest;

import io.swagger.annotations.*;
import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.UserEntity;
import org.citopt.connde.domain.device.Device;
import org.citopt.connde.domain.device.DeviceValidator;
import org.citopt.connde.domain.user.User;
import org.citopt.connde.repository.DeviceRepository;
import org.citopt.connde.service.UserService;
import org.citopt.connde.util.ValidationErrorCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

/**
 * REST Controller for device CRUD requests.
 */
@RestController
@ExposesResourceFor(Device.class)
@RequestMapping(RestConfiguration.BASE_PATH)
@Api(tags = {"Device entities"}, description = "CRUD for device entities")
public class RestDeviceController {

    @Autowired
    private UserService userService;

    @Autowired
    ProjectionFactory projectionFactory;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private DeviceValidator deviceValidator;

    @GetMapping("/devices/{deviceId}")
    @ApiOperation(value = "Returns a device entity", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 404, message = "Device not found or not authorized to access this device")})
    public ResponseEntity<Resource<Device>> one(@PathVariable @ApiParam(value = "ID of the device", example = "5c97dc2583aeb6078c5ab672", required = true) String deviceId) {
        //Get device from repository by id
        UserEntity entity = userService.getUserEntityFromRepository(deviceRepository, deviceId);

        //Check if entity could be found
        if (entity == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        //Wrap device into resource
        Resource<Device> resource = new Resource<>((Device) entity,
                linkTo(methodOn(RestDeviceController.class).one(deviceId)).withSelfRel(),
                linkTo(methodOn(RestDeviceController.class).all()).withRel("devices"));

        return ResponseEntity.ok(resource);
    }

    @GetMapping("/devices")
    @ApiOperation(value = "Returns all available device entities", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success")})
    public ResponseEntity<PagedResources<Resource<Device>>> all() {
        //Get all device user entities the current user has access to
        List<UserEntity> userEntities = userService.getUserEntitiesFromRepository(deviceRepository);

        List<Resource<Device>> deviceList = userEntities.stream()
                .map(userEntity -> (Device) userEntity)
                .map(device -> new Resource<>(device,
                        linkTo(methodOn(RestDeviceController.class).one(device.getId())).withSelfRel(),
                        linkTo(methodOn(RestDeviceController.class).all()).withRel("devices")))
                .collect(Collectors.toList());

        PagedResources.PageMetadata metadata = new PagedResources.PageMetadata(deviceList.size(), 0, deviceList.size());

        PagedResources<Resource<Device>> resources = new PagedResources<>(deviceList, metadata,
                linkTo(methodOn(RestDeviceController.class).all()).withSelfRel());

        return ResponseEntity.ok(resources);
    }

    @PostMapping("/devices")
    @ApiOperation(value = "Creates a new device entity", notes = "A ValidationErrorCollection object is returned in case of a failure.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 400, message = "Invalid device properties")})
    public ResponseEntity create(@RequestBody @ApiParam(value = "The device to create", required = true) Device device, BindingResult bindingResult) throws URISyntaxException {

        ///Validate device object
        deviceValidator.validate(device, bindingResult);

        //Check if validation errors occurred
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(new ValidationErrorCollection(bindingResult), HttpStatus.BAD_REQUEST);
        }

        //Get current user
        User currentUser = userService.getUserWithAuthorities();

        //Make current user to owner of the new resource
        device.setOwner(currentUser);

        //Save device to repository
        Device createdDevice = deviceRepository.save(device);

        //Create resource from newly created device
        Resource<Device> deviceResource = new Resource<>(createdDevice,
                linkTo(methodOn(RestDeviceController.class).one(createdDevice.getId())).withSelfRel());

        //Return resource as response
        return ResponseEntity
                .created(new URI(deviceResource.getId().expand().getHref()))
                .body(deviceResource);
    }

    @DeleteMapping("/devices/{deviceId}")
    @ApiOperation(value = "Deletes a device entity", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 403, message = "Not authorized to delete this device"), @ApiResponse(code = 404, message = "Device not found or not authorized to access this device")})
    public ResponseEntity<Void> delete(@PathVariable @ApiParam(value = "ID of the device to delete", example = "5c97dc2583aeb6078c5ab672", required = true) String deviceId) {
        //Get device from repository by id
        UserEntity entity = userService.getUserEntityFromRepository(deviceRepository, deviceId);

        //Check if entity could be found
        if (entity == null) {
            return ResponseEntity.notFound().build();
        }

        //Check if current user is allowed to delete the device
        User user = userService.getUserWithAuthorities();
        if (!(user.isAdmin() || entity.isUserOwner(user))) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        //Delete device
        deviceRepository.delete(deviceId);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/devices/{deviceId}/approve")
    @ApiOperation(value = "Approves an user for a device entity", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 400, message = "User is already approved for this device"), @ApiResponse(code = 403, message = "Not authorized to approve an user for this device"), @ApiResponse(code = 404, message = "Device or user not found or not authorized to access this device")})
    public ResponseEntity<Void> approveUser(@PathVariable @ApiParam(value = "ID of the device to approve an user for", example = "5c97dc2583aeb6078c5ab672", required = true) String deviceId, @RequestBody @ApiParam(value = "Name of the user to approve", example = "johndoe", required = true) String username) {
        //Get device from repository by id
        Device entity = (Device) userService.getUserEntityFromRepository(deviceRepository, deviceId);

        //Check if entity could be found
        if (entity == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        //Check if current user is allowed to approve an user for this device
        User user = userService.getUserWithAuthorities();
        if (!(user.isAdmin() || entity.isUserOwner(user))) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        //Get user by ID
        Optional<User> userOptional = userService.getUserWithAuthoritiesByUsername(username);

        //Check if user could be found
        if (!userOptional.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        //Get user from optional
        User candidateUser = userOptional.get();

        //Check if user is already approved
        if (candidateUser.isAdmin() || entity.isUserApproved(candidateUser)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        //Approve user
        entity.approveUser(candidateUser);
        deviceRepository.save(entity);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/devices/{deviceId}/disapprove")
    @ApiOperation(value = "Disapproves an user for a device entity", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 400, message = "User cannot be disapproved for this device"), @ApiResponse(code = 403, message = "Not authorized to disapprove an user for this device"), @ApiResponse(code = 404, message = "Device or user not found or not authorized to access this device")})
    public ResponseEntity<Void> disapproveUser(@PathVariable @ApiParam(value = "ID of the device to disapprove an user for", example = "5c97dc2583aeb6078c5ab672", required = true) String deviceId, @RequestBody @ApiParam(value = "Name of the user to disapprove", example = "johndoe", required = true) String username) {
        //Get device from repository by id
        Device entity = (Device) userService.getUserEntityFromRepository(deviceRepository, deviceId);

        //Check if entity could be found
        if (entity == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        //Check if current user is allowed to disapprove an user for this device
        User user = userService.getUserWithAuthorities();
        if (!(user.isAdmin() || entity.isUserOwner(user))) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        //Get user by ID
        Optional<User> userOptional = userService.getUserWithAuthoritiesByUsername(username);

        //Check if user could be found
        if (!userOptional.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        //Get user from optional
        User candidateUser = userOptional.get();

        //Check if user may be disapproved
        if (candidateUser.isAdmin() || (entity.isUserOwner(candidateUser)) || (!entity.isUserApproved(candidateUser))) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        //Disapprove user
        entity.disapproveUser(candidateUser);
        deviceRepository.save(entity);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
