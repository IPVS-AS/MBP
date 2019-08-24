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
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 404, message = "Device not found or not authorized")})
    public ResponseEntity<Resource<Device>> one(@PathVariable @ApiParam(value = "ID of the device", example = "5c97dc2583aeb6078c5ab672") String deviceId) {
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
    public ResponseEntity create(@RequestBody Device device, BindingResult bindingResult) throws URISyntaxException {

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

    @DeleteMapping("/devices")
    @ApiOperation(value = "Deletes a device entity", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 404, message = "Device not found or not authorized")})
    public ResponseEntity<Void> delete(@PathVariable String deviceId) {
        //Get device from repository by id
        UserEntity entity = userService.getUserEntityFromRepository(deviceRepository, deviceId);

        //Check if entity oould be found
        if (entity == null) {
            return ResponseEntity.notFound().build();
        }

        //Delete device
        deviceRepository.delete(deviceId);

        return ResponseEntity.ok().build();
    }
}
