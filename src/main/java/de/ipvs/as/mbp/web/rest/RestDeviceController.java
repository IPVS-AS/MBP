package de.ipvs.as.mbp.web.rest;

import de.ipvs.as.mbp.RestConfiguration;
import de.ipvs.as.mbp.domain.access_control.ACAccessRequest;
import de.ipvs.as.mbp.domain.access_control.ACAccessType;
import de.ipvs.as.mbp.domain.device.Device;
import de.ipvs.as.mbp.domain.device.DeviceDTO;
import de.ipvs.as.mbp.error.EntityAlreadyExistsException;
import de.ipvs.as.mbp.error.EntityNotFoundException;
import de.ipvs.as.mbp.error.MissingPermissionException;
import de.ipvs.as.mbp.repository.DeviceRepository;
import de.ipvs.as.mbp.repository.KeyPairRepository;
import de.ipvs.as.mbp.service.UserEntityService;
import de.ipvs.as.mbp.util.Validation;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * REST Controller for managing {@link Device}s.
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH + "/devices")
@Api(tags = {"Devices"})
public class RestDeviceController {

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private KeyPairRepository keyPairRepository;

    @Autowired
    private UserEntityService userEntityService;


    @GetMapping(produces = "application/hal+json")
    @ApiOperation(value = "Retrieves all existing device entities available for the requesting entity.", produces = "application/hal+json")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Success!"),
            @ApiResponse(code = 404, message = "Device or requesting user not found!")})
    public ResponseEntity<PagedModel<EntityModel<Device>>> all(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @ApiParam(value = "Page parameters", required = true) Pageable pageable) {
        // Parse the access-request information
        ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);

        // Retrieve the corresponding devices (includes access-control)
        List<Device> devices = userEntityService.getPageWithAccessControlCheck(deviceRepository, ACAccessType.READ, accessRequest, pageable);

        // Create self link
        Link selfLink = linkTo(methodOn(getClass()).all(accessRequestHeader, pageable)).withSelfRel();

        return ResponseEntity.ok(userEntityService.entitiesToPagedModel(devices, selfLink, pageable));
    }

    @GetMapping(path = "/{deviceId}", produces = "application/hal+json")
    @ApiOperation(value = "Retrieves an existing device entity identified by its id if it's available for the requesting entity.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"),
            @ApiResponse(code = 401, message = "Not authorized to access the device!"),
            @ApiResponse(code = 404, message = "Device or requesting user not found!")})
    public ResponseEntity<EntityModel<Device>> one(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @PathVariable("deviceId") String deviceId,
            @ApiParam(value = "Page parameters", required = true) Pageable pageable) throws EntityNotFoundException, MissingPermissionException {
        // Parse the access-request information
        ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);

        // Retrieve the corresponding device (includes access-control)
        Device device = userEntityService.getForIdWithAccessControlCheck(deviceRepository, deviceId, ACAccessType.READ, accessRequest);
        return ResponseEntity.ok(userEntityService.entityToEntityModel(device));
    }

    @GetMapping("/by-key/{id}")
    @ApiOperation(value = "Retrieves the devices which use a certain key-pair and for which the user is authorized", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"),
            @ApiResponse(code = 404, message = "Key-pair or requesting user not found!")})
    public ResponseEntity<List<Device>> byKeyPair(
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

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/hal+json")
    @ApiOperation(value = "Retrieves an existing device entity identified by its id if it's available for the requesting entity.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"), @ApiResponse(code = 409, message = "Device already exists!")})
    public ResponseEntity<EntityModel<Device>> create(
            @ApiParam(value = "Page parameters", required = true) Pageable pageable,
            @RequestBody DeviceDTO requestDto) throws EntityAlreadyExistsException, EntityNotFoundException {
        // Create device from request DTO
        Device device = (Device) new Device()
                .setName(requestDto.getName())
                .setComponentType(requestDto.getComponentType())
                .setIpAddress(requestDto.getIpAddress())
                .setDate(LocalDateTime.now().toString())
                .setUsername(requestDto.getUsername())
                .setPassword(requestDto.getPassword() == null ? null : requestDto.getPassword())
                .setKeyPair(requestDto.getKeyPairId() == null ? null : userEntityService.getForId(keyPairRepository, requestDto.getKeyPairId()))
                .setAccessControlPolicyIds(requestDto.getAccessControlPolicyIds());

        // Save device in the database
        Device createdDevice = userEntityService.create(deviceRepository, device);
        return ResponseEntity.ok(userEntityService.entityToEntityModel(createdDevice));
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/hal+json")
    @ApiOperation(value = "Updates an existing device entity, identified by its ID.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"), @ApiResponse(code = 400, message = "Device is invalid."), @ApiResponse(code = 404, message = "Device not found!")})
    public ResponseEntity<EntityModel<Device>> update(@PathVariable(name = "id") String id, @RequestHeader("X-MBP-Access-Request") String accessRequestHeader, @RequestBody DeviceDTO requestDto) throws MissingPermissionException, EntityNotFoundException {

        // Parse the access request information
        ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);

        //Get affected device with access control check
        Device device = userEntityService.getForIdWithAccessControlCheck(deviceRepository, id, ACAccessType.READ, accessRequest);

        device.setName(requestDto.getName())
                .setComponentType(requestDto.getComponentType())
                .setIpAddress(requestDto.getIpAddress())
                .setUsername(requestDto.getUsername())
                .setPassword(requestDto.getPassword() == null ? null : requestDto.getPassword())
                .setKeyPair(requestDto.getKeyPairId() == null ? null : userEntityService.getForId(keyPairRepository, requestDto.getKeyPairId()))
                .setAccessControlPolicyIds(requestDto.getAccessControlPolicyIds());

        //Check name for update
        if (Validation.isNullOrEmpty(requestDto.getName())) device.setName(requestDto.getName());

        //Check component type for update
        if (Validation.isNullOrEmpty(requestDto.getComponentType()))
            device.setComponentType(requestDto.getComponentType());

        //Check IP address for update
        if (Validation.isNullOrEmpty(requestDto.getIpAddress())) device.setIpAddress(requestDto.getIpAddress());

        //Check username for update
        if (Validation.isNullOrEmpty(requestDto.getUsername())) device.setUsername(requestDto.getUsername());

        //Check password for update
        if (Validation.isNullOrEmpty(requestDto.getPassword())) device.setUsername(requestDto.getPassword());

        //Update device template with access control check
        Device updatedDevice = userEntityService.updateWithAccessControlCheck(deviceRepository, id, device.setId(id), accessRequest);

        //Return updated device
        return ResponseEntity.ok(userEntityService.entityToEntityModel(updatedDevice));
    }

    @DeleteMapping(path = "/{deviceId}")
    @ApiOperation(value = "Deletes an existing device entity identified by its id if it's available for the requesting entity.")
    @ApiResponses({@ApiResponse(code = 204, message = "Success!"),
            @ApiResponse(code = 401, message = "Not authorized to delete the device!"),
            @ApiResponse(code = 404, message = "Device or requesting user not found!")})
    public ResponseEntity<Void> delete(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @PathVariable("deviceId") String deviceId) throws EntityNotFoundException, MissingPermissionException {
        // Parse the access-request information
        ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);

        // Delete the device (includes access-control)
        userEntityService.deleteWithAccessControlCheck(deviceRepository, deviceId, accessRequest);
        return ResponseEntity.noContent().build();
    }
}
