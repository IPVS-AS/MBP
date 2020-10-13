package org.citopt.connde.web.rest;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;
import java.util.stream.Collectors;

import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.access_control.ACAccessRequest;
import org.citopt.connde.domain.access_control.ACAccessType;
import org.citopt.connde.domain.device.Device;
import org.citopt.connde.domain.key_pair.KeyPair;
import org.citopt.connde.error.EntityAlreadyExistsException;
import org.citopt.connde.error.EntityNotFoundException;
import org.citopt.connde.error.MissingPermissionException;
import org.citopt.connde.repository.DeviceRepository;
import org.citopt.connde.repository.KeyPairRepository;
import org.citopt.connde.service.UserEntityService;
import org.citopt.connde.service.UserService;
import org.citopt.connde.service.crypto.SSHKeyPairGenerator;
import org.citopt.connde.util.S;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * REST Controller for functions related to RSA key-pairs that may be used for
 * establishing SSH connections.
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH + "/key-pairs")
@Api(tags = {"key-pairs"})
public class RestKeyPairController {

    @Autowired
    private KeyPairRepository keyPairRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private UserEntityService userEntityService;

    @Autowired
    private SSHKeyPairGenerator keyPairGenerator;


    @GetMapping(produces = "application/hal+json")
    @ApiOperation(value = "Retrieves all existing key-pair entities available for the requesting entity.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"),
            @ApiResponse(code = 404, message = "Key-Pair or requesting user not found!")})
    public ResponseEntity<PagedModel<EntityModel<KeyPair>>> all(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @ApiParam(value = "Page parameters", required = true) Pageable pageable) {
        // Parse the access-request information
        ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);

        // Retrieve the corresponding key-pairs (includes access-control)
        List<KeyPair> adapters = userEntityService.getPageWithAccessControlCheck(keyPairRepository, ACAccessType.READ, accessRequest, pageable);

        // Create self link
        Link selfLink = linkTo(methodOn(getClass()).all(accessRequestHeader, pageable)).withSelfRel();

        return ResponseEntity.ok(userEntityService.entitiesToPagedModel(adapters, selfLink, pageable));
    }

    @GetMapping(path = "/{keyPairId}", produces = "application/hal+json")
    @ApiOperation(value = "Retrieves an existing key-pair entity identified by its id if it's available for the requesting entity.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"),
            @ApiResponse(code = 401, message = "Not authorized to access the key-pair!"),
            @ApiResponse(code = 404, message = "Key-Pair or requesting user not found!")})
    public ResponseEntity<EntityModel<KeyPair>> one(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @PathVariable("keyPairId") String keyPairId,
            @ApiParam(value = "Page parameters", required = true) Pageable pageable) throws EntityNotFoundException {
        // Retrieve the corresponding key-pair (includes access-control)
        KeyPair adapter = userEntityService.getForIdWithAccessControlCheck(keyPairRepository, keyPairId, ACAccessType.READ, ACAccessRequest.valueOf(accessRequestHeader));
        return ResponseEntity.ok(userEntityService.entityToEntityModel(adapter));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/hal+json")
    @ApiOperation(value = "Creates a new key pair entity.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"), @ApiResponse(code = 409, message = "Key pair already exists!")})
    public ResponseEntity<EntityModel<KeyPair>> create(
            @ApiParam(value = "Page parameters", required = true) Pageable pageable,
            @RequestBody KeyPair keyPair) throws EntityAlreadyExistsException, EntityNotFoundException {
        // Save key pair in the database
        KeyPair createdKeyPair = userEntityService.create(keyPairRepository, keyPair);
        return ResponseEntity.ok(userEntityService.entityToEntityModel(createdKeyPair));
    }

    @DeleteMapping(path = "/{keyPairId}")
    @ApiOperation(value = "Deletes an existing key-pair entity identified by its id if it's available for the requesting entity.")
    @ApiResponses({@ApiResponse(code = 204, message = "Success!"),
            @ApiResponse(code = 401, message = "Not authorized to delete the key-pair!"),
            @ApiResponse(code = 404, message = "Key-Pair or requesting user not found!")})
    public ResponseEntity<Void> delete(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @PathVariable("keyPairId") String keyPairId) throws EntityNotFoundException {
        // Delete the key-pair (includes access-control)
        userEntityService.deleteWithAccessControlCheck(keyPairRepository, keyPairId, ACAccessRequest.valueOf(accessRequestHeader));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/generate")
    @ApiOperation(value = "Generates and registers a new SSH key-pair with a given name.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 201, message = "Success!"),
            @ApiResponse(code = 400, message = "Invalid name!"),
            @ApiResponse(code = 409, message = "Key-pair name already exists!")})
    public ResponseEntity<KeyPair> generateKeyPair(@RequestBody @ApiParam(value = "The name of the key-pair") String name) {
        if (S.nullOrEmpty(name)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name must not be empty!");
        } else if (keyPairRepository.existsByName(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Key-pair with name '" + name + "' already exists!");
        }

        // Generate key-pair
        KeyPair keyPair = (KeyPair) keyPairGenerator.generateKeyPair()
                .setName(name)
                .setOwner(userService.getLoggedInUser());

        // Save key-pair in the database
        keyPair = keyPairRepository.save(keyPair);
        return ResponseEntity.status(HttpStatus.CREATED).body(keyPair);
    }


    @GetMapping("/devices/by-key/{id}")
    @ApiOperation(value = "Retrieves the devices which use a certain key-pair and for which the user is authorized", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"),
            @ApiResponse(code = 404, message = "Key-pair or requesting user not found!")})
    public ResponseEntity<List<Device>> getDevicesByKeyPair(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @PathVariable(value = "id") @ApiParam(value = "ID of the key-pair", example = "5c97dc2583aeb6078c5ab672", required = true) String keyPairId) throws EntityNotFoundException, MissingPermissionException {
        // Parse the access-request information
        ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);

        // Check permission for key-pair
        userEntityService.requirePermission(keyPairRepository, keyPairId, ACAccessType.READ, accessRequest);

        // Retrieve all devices from the database (includes access-control)
        List<Device> devices = userEntityService.getAllWithAccessControlCheck(deviceRepository, ACAccessType.READ, accessRequest)
                .stream()
                // Filter devices that do not use the key-pair
                .filter(d -> d.hasRSAKey() && d.getKeyPair().getId().equals(keyPairId))
                .collect(Collectors.toList());
        return ResponseEntity.ok(devices);
    }
}
