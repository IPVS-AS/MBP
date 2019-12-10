package org.citopt.connde.web.rest;

import io.swagger.annotations.*;
import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.adapter.Adapter;
import org.citopt.connde.domain.adapter.AdapterValidator;
import org.citopt.connde.domain.user.User;
import org.citopt.connde.domain.user_entity.UserEntity;
import org.citopt.connde.repository.AdapterRepository;
import org.citopt.connde.service.UserEntityService;
import org.citopt.connde.service.UserService;
import org.citopt.connde.util.ValidationErrorCollection;
import org.springframework.beans.factory.annotation.Autowired;
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
 * REST Controller for adapter CRUD requests.
 */
@RestController
@ExposesResourceFor(Adapter.class)
@RequestMapping(RestConfiguration.BASE_PATH)
@Api(tags = {"Adapter entities"}, description = "CRUD for adapter entities")
public class RestAdapterController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserEntityService userEntityService;

    @Autowired
    private AdapterRepository adapterRepository;

    @Autowired
    private AdapterValidator adapterValidator;

    @RequestMapping(value = "/adapters/{adapterId}", method = RequestMethod.GET)
    @ApiOperation(value = "Returns an adapter entity", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 404, message = "Adapter not found or not authorized to access this adapter")})
    public ResponseEntity<Resource<Adapter>> one(@PathVariable @ApiParam(value = "ID of the adapter", example = "5c97dc2583aeb6078c5ab672", required = true) String adapterId) {
        //Get adapter from repository by id
        UserEntity entity = userEntityService.getUserEntityFromRepository(adapterRepository, adapterId);

        //Check if entity could be found
        if (entity == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        //Wrap adapter into resource
        Resource<Adapter> resource = new Resource<>((Adapter) entity,
                linkTo(methodOn(RestAdapterController.class).one(adapterId)).withSelfRel(),
                linkTo(methodOn(RestAdapterController.class).all()).withRel("adapters"));

        return ResponseEntity.ok(resource);
    }

    @RequestMapping(value = "/adapters", method = RequestMethod.GET)
    @ApiOperation(value = "Returns all available adapter entities", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success")})
    public ResponseEntity<PagedResources<Resource<Adapter>>> all() {
        //Get all adapter user entities the current user has access to
        List<UserEntity> userEntities = userEntityService.getUserEntitiesFromRepository(adapterRepository);

        List<Resource<Adapter>> adapterList = userEntities.stream()
                .map(userEntity -> (Adapter) userEntity)
                .map(adapter -> new Resource<>(adapter,
                        linkTo(methodOn(RestAdapterController.class).one(adapter.getId())).withSelfRel(),
                        linkTo(methodOn(RestAdapterController.class).all()).withRel("adapters")))
                .collect(Collectors.toList());

        PagedResources.PageMetadata metadata = new PagedResources.PageMetadata(adapterList.size(), 0, adapterList.size());

        PagedResources<Resource<Adapter>> resources = new PagedResources<>(adapterList, metadata,
                linkTo(methodOn(RestAdapterController.class).all()).withSelfRel());

        return ResponseEntity.ok(resources);
    }

    @PostMapping("/adapters")
    @ApiOperation(value = "Creates a new adapter entity", notes = "A ValidationErrorCollection object is returned in case of a failure.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 400, message = "Invalid adapter properties"), @ApiResponse(code = 401, message = "Not authorized to create a new adapter")})
    public ResponseEntity create(@RequestBody @ApiParam(value = "The adapter to create", required = true) Adapter adapter, BindingResult bindingResult) throws URISyntaxException {
        ///Validate adapter object
        adapterValidator.validate(adapter, bindingResult);

        //Check if validation errors occurred
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(new ValidationErrorCollection(bindingResult), HttpStatus.BAD_REQUEST);
        }

        if (!adapter.isCreatable()) {
            return new ResponseEntity<>(new ValidationErrorCollection(bindingResult), HttpStatus.UNAUTHORIZED);
        }

        //Set current user as owner of the new resource
        adapter.setOwner();

        //Save adapter to repository
        Adapter createdAdapter = adapterRepository.save(adapter);

        //Create resource from newly created adapter
        Resource<Adapter> adapterResource = new Resource<>(createdAdapter,
                linkTo(methodOn(RestAdapterController.class).one(createdAdapter.getId())).withSelfRel());

        //Return resource as response
        return ResponseEntity
                .created(new URI(adapterResource.getId().expand().getHref()))
                .body(adapterResource);
    }

    @DeleteMapping("/adapters/{adapterId}")
    @ApiOperation(value = "Deletes an adapter entity", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 403, message = "Not authorized to delete this adapter"), @ApiResponse(code = 404, message = "Adapter not found or not authorized to access this adapter")})
    public ResponseEntity<Void> delete(@PathVariable @ApiParam(value = "ID of the adapter to delete", example = "5c97dc2583aeb6078c5ab672", required = true) String adapterId) {
        //Get adapter from repository by id
        UserEntity entity = userEntityService.getUserEntityFromRepository(adapterRepository, adapterId);

        //Check if entity could be found
        if (entity == null) {
            return ResponseEntity.notFound().build();
        }

        //Check if user is allowed to delete the adapter
        if (!entity.isDeletable()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        //Delete adapter
        adapterRepository.delete(adapterId);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/adapters/{adapterId}/approve")
    @ApiOperation(value = "Approves an user for an adapter entity", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 400, message = "User is already approved for this adapter"), @ApiResponse(code = 403, message = "Not authorized to approve an user for this adapter"), @ApiResponse(code = 404, message = "Adapter or user not found or not authorized to access this adapter")})
    public ResponseEntity<Void> approveUser(@PathVariable @ApiParam(value = "ID of the adapter to approve an user for", example = "5c97dc2583aeb6078c5ab672", required = true) String adapterId, @RequestBody @ApiParam(value = "Name of the user to approve", example = "johndoe", required = true) String username) {
        //Get adapter from repository by id
        Adapter entity = (Adapter) userEntityService.getUserEntityFromRepository(adapterRepository, adapterId);

        //Check if entity could be found
        if (entity == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        //Check if current user is allowed to approve an user for this adapter
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
        adapterRepository.save(entity);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/adapters/{adapterId}/disapprove")
    @ApiOperation(value = "Disapproves an user for an adapter entity", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 400, message = "User cannot be disapproved for this adapter"), @ApiResponse(code = 403, message = "Not authorized to disapprove an user for this adapter"), @ApiResponse(code = 404, message = "Adapter or user not found or not authorized to access this adapter")})
    public ResponseEntity<Void> disapproveUser(@PathVariable @ApiParam(value = "ID of the adapter to disapprove an user for", example = "5c97dc2583aeb6078c5ab672", required = true) String adapterId, @RequestBody @ApiParam(value = "Name of the user to disapprove", example = "johndoe", required = true) String username) {
        //Get adapter from repository by id
        Adapter entity = (Adapter) userEntityService.getUserEntityFromRepository(adapterRepository, adapterId);

        //Check if entity could be found
        if (entity == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        //Check if current user is allowed to disapprove an user for this adapter
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
        adapterRepository.save(entity);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
