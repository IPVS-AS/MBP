package de.ipvs.as.mbp.web.rest;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;

import de.ipvs.as.mbp.RestConfiguration;
import de.ipvs.as.mbp.error.EntityAlreadyExistsException;
import de.ipvs.as.mbp.error.EntityNotFoundException;
import de.ipvs.as.mbp.error.MBPException;
import de.ipvs.as.mbp.error.MissingPermissionException;
import de.ipvs.as.mbp.repository.KeyPairRepository;
import de.ipvs.as.mbp.service.user.UserEntityService;
import de.ipvs.as.mbp.service.user.UserService;
import de.ipvs.as.mbp.util.S;
import de.ipvs.as.mbp.domain.access_control.ACAccessRequest;
import de.ipvs.as.mbp.domain.access_control.ACAccessType;
import de.ipvs.as.mbp.domain.key_pair.KeyPair;
import de.ipvs.as.mbp.service.crypto.SSHKeyPairGenerator;
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
            @ApiParam(value = "Page parameters", required = true) Pageable pageable) throws EntityNotFoundException, MissingPermissionException {
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
            @PathVariable("keyPairId") String keyPairId) throws EntityNotFoundException, MissingPermissionException {
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
            throw new MBPException(HttpStatus.BAD_REQUEST, "Name must not be empty!");
        } else if (keyPairRepository.existsByName(name)) {
            throw new MBPException(HttpStatus.CONFLICT, "Key-pair with name '" + name + "' already exists!");
        }

        // Generate key-pair
        KeyPair keyPair = (KeyPair) keyPairGenerator.generateKeyPair()
                .setName(name)
                .setOwner(userService.getLoggedInUser());

        // Save key-pair in the database
        keyPair = keyPairRepository.save(keyPair);
        return ResponseEntity.status(HttpStatus.CREATED).body(keyPair);
    }

}
