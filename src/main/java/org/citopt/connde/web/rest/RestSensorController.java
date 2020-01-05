package org.citopt.connde.web.rest;

import io.swagger.annotations.*;
import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.component.Sensor;
import org.citopt.connde.domain.component.SensorValidator;
import org.citopt.connde.domain.user.User;
import org.citopt.connde.domain.user_entity.UserEntity;
import org.citopt.connde.repository.AdapterRepository;
import org.citopt.connde.repository.DeviceRepository;
import org.citopt.connde.repository.SensorRepository;
import org.citopt.connde.service.UserEntityService;
import org.citopt.connde.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

/**
 * REST Controller for sensor CRUD requests.
 */
//@RestController
//@ExposesResourceFor(Sensor.class)
//@RequestMapping(RestConfiguration.BASE_PATH)
//@Api(tags = {"Sensor entities"}, description = "CRUD for sensor entities")
public class RestSensorController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserEntityService userEntityService;

    @Autowired
    private SensorRepository sensorRepository;

    @Autowired
    private SensorValidator sensorValidator;

    @Autowired
    private AdapterRepository adapterRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    /*
    @GetMapping("/sensors/{sensorId}")
    @ApiOperation(value = "Returns a sensor entity", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 404, message = "Sensor not found or not authorized to access this sensor")})
    public ResponseEntity<Resource<Sensor>> one(@PathVariable @ApiParam(value = "ID of the sensor", example = "5c97dc2583aeb6078c5ab672", required = true) String sensorId) {
        //Get sensor from repository by id
        UserEntity entity = userEntityService.getUserEntityFromRepository(sensorRepository, sensorId);

        //Check if entity could be found
        if (entity == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        //Wrap sensor into resource
        Resource<Sensor> resource = new Resource<>((Sensor) entity,
                linkTo(methodOn(RestSensorController.class).one(sensorId)).withSelfRel(),
                linkTo(methodOn(RestSensorController.class).all()).withRel("sensors"));

        return ResponseEntity.ok(resource);
    }*/

    @GetMapping("/sensors")
    @ApiOperation(value = "Returns all available sensor entities", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success")})
    public ResponseEntity<PagedResources<Resource<Sensor>>> all(Pageable pageable) {
        //Get all sensor user entities the current user has access to
        List<UserEntity> userEntities = userEntityService.getUserEntitiesFromRepository(sensorRepository);

        List<Resource<Sensor>> sensorList = userEntities.stream()
                .map(userEntity -> (Sensor) userEntity)
                .map(sensor -> new Resource<>(sensor,
                        //linkTo(methodOn(RestSensorController.class).one(sensor.getId())).withSelfRel(),
                        linkTo(methodOn(RestSensorController.class).all(pageable)).withRel("sensors")))
                .collect(Collectors.toList());

        PagedResources.PageMetadata metadata = new PagedResources.PageMetadata(sensorList.size(), 0, sensorList.size());

        PagedResources<Resource<Sensor>> resources = new PagedResources<>(sensorList, metadata,
                linkTo(methodOn(RestSensorController.class).all(pageable)).withSelfRel());

        return ResponseEntity.ok(resources);
    }

    /*
    @RequestMapping(value = "/sensors", method = RequestMethod.POST)
    @ApiOperation(value = "Creates a new sensor entity", notes = "A ValidationErrorCollection object is returned in case of a failure.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 400, message = "Invalid sensor properties"), @ApiResponse(code = 401, message = "Not authorized to create a new sensor")})
    public ResponseEntity create(@RequestBody @ApiParam(value = "The sensor to create", required = true) ComponentDTO dto, BindingResult bindingResult) throws URISyntaxException {

        Sensor sensor = new Sensor();
        sensor.setName(dto.getName());
        sensor.setComponentType(dto.getComponentType());
        sensor.setAdapter(adapterRepository.findOne(dto.getAdapter()));
        sensor.setDevice(deviceRepository.findOne(dto.getDevice()));

        ///Validate sensor object
        sensorValidator.validate(sensor, bindingResult);

        //Check if validation errors occurred
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(new ValidationErrorCollection(bindingResult), HttpStatus.BAD_REQUEST);
        } else if (!sensor.isCreatable()) {
            return new ResponseEntity<>(new ValidationErrorCollection(bindingResult), HttpStatus.UNAUTHORIZED);
        }

        //Set current user as owner of the new resource
        sensor.setOwner();

        //Save sensor to repository
        Sensor createdSensor = sensorRepository.save(sensor);

        //Create resource from newly created sensor
        Resource<Sensor> sensorResource = new Resource<>(createdSensor,
                linkTo(methodOn(RestSensorController.class).one(createdSensor.getId())).withSelfRel());

        //Return resource as response
        return ResponseEntity
                .created(new URI(sensorResource.getId().expand().getHref()))
                .body(sensorResource);
    }

    @DeleteMapping("/sensors/{sensorId}")
    @ApiOperation(value = "Deletes a sensor entity", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 403, message = "Not authorized to delete this sensor"), @ApiResponse(code = 404, message = "Sensor not found or not authorized to access this sensor")})
    public ResponseEntity<Void> delete(@PathVariable @ApiParam(value = "ID of the sensor to delete", example = "5c97dc2583aeb6078c5ab672", required = true) String sensorId) {
        //Get sensor from repository by id
        UserEntity entity = userEntityService.getUserEntityFromRepository(sensorRepository, sensorId);

        //Check if entity could be found
        if (entity == null) {
            return ResponseEntity.notFound().build();
        }

        //Check if user is allowed to delete the sensor
        if (!entity.isDeletable()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        //Delete sensor
        sensorRepository.delete(sensorId);

        return ResponseEntity.ok().build();
    }*/

    @PostMapping("/sensors/{sensorId}/approve")
    @ApiOperation(value = "Approves an user for a sensor entity", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 400, message = "User is already approved for this sensor"), @ApiResponse(code = 403, message = "Not authorized to approve an user for this sensor"), @ApiResponse(code = 404, message = "Sensor or user not found or not authorized to access this sensor")})
    public ResponseEntity<Void> approveUser(@PathVariable @ApiParam(value = "ID of the sensor to approve an user for", example = "5c97dc2583aeb6078c5ab672", required = true) String sensorId, @RequestBody @ApiParam(value = "Name of the user to approve", example = "johndoe", required = true) String username) {
        //Get sensor from repository by id
        Sensor entity = (Sensor) userEntityService.getUserEntityFromRepository(sensorRepository, sensorId);

        //Check if entity could be found
        if (entity == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        //Check if current user is allowed to approve an user for this sensor
        if (!entity.isApprovable()) {
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

        //Only non-approved and non-admin users may be approved
        if (candidateUser.isAdmin() || entity.isUserApproved(candidateUser)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        //Approve user
        entity.approveUser(candidateUser);
        sensorRepository.save(entity);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/sensors/{sensorId}/disapprove")
    @ApiOperation(value = "Disapproves an user for a sensor entity", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 400, message = "User cannot be disapproved for this sensor"), @ApiResponse(code = 403, message = "Not authorized to disapprove an user for this sensor"), @ApiResponse(code = 404, message = "Sensor or user not found or not authorized to access this sensor")})
    public ResponseEntity<Void> disapproveUser(@PathVariable @ApiParam(value = "ID of the sensor to disapprove an user for", example = "5c97dc2583aeb6078c5ab672", required = true) String sensorId, @RequestBody @ApiParam(value = "Name of the user to disapprove", example = "johndoe", required = true) String username) {
        //Get sensor from repository by id
        Sensor entity = (Sensor) userEntityService.getUserEntityFromRepository(sensorRepository, sensorId);

        //Check if entity could be found
        if (entity == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        //Check if current user is allowed to disapprove an user for this sensor
        if (!entity.isDisapprovable()) {
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

        //Only non-admin users, non-owners and already approved users may be disapproved
        if (candidateUser.isAdmin() || (entity.isUserOwner(candidateUser)) || (!entity.isUserApproved(candidateUser))) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        //Disapprove user
        entity.disapproveUser(candidateUser);
        sensorRepository.save(entity);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
