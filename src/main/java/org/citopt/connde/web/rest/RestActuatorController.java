package org.citopt.connde.web.rest;

import io.swagger.annotations.*;
import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.component.Actuator;
import org.citopt.connde.domain.component.ActuatorValidator;
import org.citopt.connde.domain.user.User;
import org.citopt.connde.domain.user_entity.UserEntity;
import org.citopt.connde.repository.ActuatorRepository;
import org.citopt.connde.service.UserEntityService;
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
 * REST Controller for actuator CRUD requests.
 */
@RestController
@ExposesResourceFor(Actuator.class)
@RequestMapping(RestConfiguration.BASE_PATH)
@Api(tags = {"Actuator entities"}, description = "CRUD for actuator entities")
public class RestActuatorController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserEntityService userEntityService;

    @Autowired
    private ActuatorRepository actuatorRepository;

    @Autowired
    private ActuatorValidator actuatorValidator;

    @GetMapping("/actuators/{actuatorId}")
    @ApiOperation(value = "Returns an actuator entity", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 404, message = "Actuator not found or not authorized to access this actuator")})
    public ResponseEntity<Resource<Actuator>> one(@PathVariable @ApiParam(value = "ID of the actuator", example = "5c97dc2583aeb6078c5ab672", required = true) String actuatorId) {
        //Get actuator from repository by id
        UserEntity entity = userEntityService.getUserEntityFromRepository(actuatorRepository, actuatorId);

        //Check if entity could be found
        if (entity == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        //Wrap actuator into resource
        Resource<Actuator> resource = new Resource<>((Actuator) entity,
                linkTo(methodOn(RestActuatorController.class).one(actuatorId)).withSelfRel(),
                linkTo(methodOn(RestActuatorController.class).all()).withRel("actuators"));

        return ResponseEntity.ok(resource);
    }

    @GetMapping("/actuators")
    @ApiOperation(value = "Returns all available actuator entities", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success")})
    public ResponseEntity<PagedResources<Resource<Actuator>>> all() {
        //Get all actuator user entities the current user has access to
        List<UserEntity> userEntities = userEntityService.getUserEntitiesFromRepository(actuatorRepository);

        List<Resource<Actuator>> actuatorList = userEntities.stream()
                .map(userEntity -> (Actuator) userEntity)
                .map(actuator -> new Resource<>(actuator,
                        linkTo(methodOn(RestActuatorController.class).one(actuator.getId())).withSelfRel(),
                        linkTo(methodOn(RestActuatorController.class).all()).withRel("actuators")))
                .collect(Collectors.toList());

        PagedResources.PageMetadata metadata = new PagedResources.PageMetadata(actuatorList.size(), 0, actuatorList.size());

        PagedResources<Resource<Actuator>> resources = new PagedResources<>(actuatorList, metadata,
                linkTo(methodOn(RestActuatorController.class).all()).withSelfRel());

        return ResponseEntity.ok(resources);
    }

    @PostMapping("/actuators")
    @ApiOperation(value = "Creates a new actuator entity", notes = "A ValidationErrorCollection object is returned in case of a failure.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 400, message = "Invalid actuator properties"), @ApiResponse(code = 401, message = "Not authorized to create a new actuator")})
    public ResponseEntity create(@RequestBody @ApiParam(value = "The actuator to create", required = true) Actuator actuator, BindingResult bindingResult) throws URISyntaxException {
        ///Validate actuator object
        actuatorValidator.validate(actuator, bindingResult);

        //Check if validation errors occurred
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(new ValidationErrorCollection(bindingResult), HttpStatus.BAD_REQUEST);
        }

        if (!actuator.isCreatable()) {
            return new ResponseEntity<>(new ValidationErrorCollection(bindingResult), HttpStatus.UNAUTHORIZED);
        }

        //Set current user as owner of the new resource
        actuator.setOwner();

        //Save actuator to repository
        Actuator createdActuator = actuatorRepository.save(actuator);

        //Create resource from newly created actuator
        Resource<Actuator> actuatorResource = new Resource<>(createdActuator,
                linkTo(methodOn(RestActuatorController.class).one(createdActuator.getId())).withSelfRel());

        //Return resource as response
        return ResponseEntity
                .created(new URI(actuatorResource.getId().expand().getHref()))
                .body(actuatorResource);
    }

    @DeleteMapping("/actuators/{actuatorId}")
    @ApiOperation(value = "Deletes an actuator entity", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 403, message = "Not authorized to delete this actuator"), @ApiResponse(code = 404, message = "Actuator not found or not authorized to access this actuator")})
    public ResponseEntity<Void> delete(@PathVariable @ApiParam(value = "ID of the actuator to delete", example = "5c97dc2583aeb6078c5ab672", required = true) String actuatorId) {
        //Get actuator from repository by id
        UserEntity entity = userEntityService.getUserEntityFromRepository(actuatorRepository, actuatorId);

        //Check if entity could be found
        if (entity == null) {
            return ResponseEntity.notFound().build();
        }

        //Check if user is allowed to delete the actuator
        if (!entity.isDeletable()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        //Delete actuator
        actuatorRepository.delete(actuatorId);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/actuators/{actuatorId}/approve")
    @ApiOperation(value = "Approves an user for an actuator entity", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 400, message = "User is already approved for this actuator"), @ApiResponse(code = 403, message = "Not authorized to approve an user for this actuator"), @ApiResponse(code = 404, message = "Actuator or user not found or not authorized to access this actuator")})
    public ResponseEntity<Void> approveUser(@PathVariable @ApiParam(value = "ID of the actuator to approve an user for", example = "5c97dc2583aeb6078c5ab672", required = true) String actuatorId, @RequestBody @ApiParam(value = "Name of the user to approve", example = "johndoe", required = true) String username) {
        //Get actuator from repository by id
        Actuator entity = (Actuator) userEntityService.getUserEntityFromRepository(actuatorRepository, actuatorId);

        //Check if entity could be found
        if (entity == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        //Check if current user is allowed to approve an user for this actuator
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
        actuatorRepository.save(entity);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/actuators/{actuatorId}/disapprove")
    @ApiOperation(value = "Disapproves an user for an actuator entity", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 400, message = "User cannot be disapproved for this actuator"), @ApiResponse(code = 403, message = "Not authorized to disapprove an user for this actuator"), @ApiResponse(code = 404, message = "Actuator or user not found or not authorized to access this actuator")})
    public ResponseEntity<Void> disapproveUser(@PathVariable @ApiParam(value = "ID of the actuator to disapprove an user for", example = "5c97dc2583aeb6078c5ab672", required = true) String actuatorId, @RequestBody @ApiParam(value = "Name of the user to disapprove", example = "johndoe", required = true) String username) {
        //Get actuator from repository by id
        Actuator entity = (Actuator) userEntityService.getUserEntityFromRepository(actuatorRepository, actuatorId);

        //Check if entity could be found
        if (entity == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        //Check if current user is allowed to disapprove an user for this actuator
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
        actuatorRepository.save(entity);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
