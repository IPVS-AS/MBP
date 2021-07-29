package de.ipvs.as.mbp.web.rest.discovery;

import de.ipvs.as.mbp.RestConfiguration;
import de.ipvs.as.mbp.domain.access_control.ACAccessRequest;
import de.ipvs.as.mbp.domain.access_control.ACAccessType;
import de.ipvs.as.mbp.domain.discovery.device.DeviceTemplate;
import de.ipvs.as.mbp.error.EntityNotFoundException;
import de.ipvs.as.mbp.error.MissingPermissionException;
import de.ipvs.as.mbp.repository.discovery.DeviceTemplateRepository;
import de.ipvs.as.mbp.service.discovery.DiscoveryService;
import de.ipvs.as.mbp.service.user.UserEntityService;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * REST Controller for managing {@link DeviceTemplate}s.
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH + "/discovery/device-templates")
@Api(tags = {"Device templates"})
public class RestDeviceTemplateController {

    @Autowired
    private DeviceTemplateRepository deviceTemplateRepository;

    @Autowired
    private DiscoveryService discoveryService;

    @Autowired
    private UserEntityService userEntityService;


    @GetMapping(produces = "application/hal+json")
    @ApiOperation(value = "Retrieves all device templates that are available for the requesting user.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"), @ApiResponse(code = 404, message = "Requesting user not found!")})
    public ResponseEntity<PagedModel<EntityModel<DeviceTemplate>>> all(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @ApiParam(value = "Page parameters", required = true) Pageable pageable) {
        // Parse the access-request information
        ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);

        // Retrieve the corresponding device templates (includes access-control)
        List<DeviceTemplate> deviceTemplates = userEntityService.getPageWithAccessControlCheck(deviceTemplateRepository, ACAccessType.READ, accessRequest, pageable);

        // Create self link
        Link selfLink = linkTo(methodOn(getClass()).all(accessRequestHeader, pageable)).withSelfRel();

        return ResponseEntity.ok(userEntityService.entitiesToPagedModel(deviceTemplates, selfLink, pageable));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/hal+json")
    @ApiOperation(value = "Creates a new device template.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"), @ApiResponse(code = 400, message = "Device template is invalid.")})
    public ResponseEntity<EntityModel<DeviceTemplate>> create(@RequestBody DeviceTemplate deviceTemplate) throws EntityNotFoundException {
        //Save device template in repository
        DeviceTemplate createdDeviceTemplate = userEntityService.create(deviceTemplateRepository, deviceTemplate);

        //Return created device template
        return ResponseEntity.ok(userEntityService.entityToEntityModel(createdDeviceTemplate));
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/hal+json")
    @ApiOperation(value = "Updates an existing device template.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"), @ApiResponse(code = 400, message = "Device template is invalid."), @ApiResponse(code = 404, message = "Device template not found!")})
    public ResponseEntity<EntityModel<DeviceTemplate>> update(@PathVariable(name = "id") String id, @RequestHeader("X-MBP-Access-Request") String accessRequestHeader, @RequestBody DeviceTemplate deviceTemplate) throws MissingPermissionException, EntityNotFoundException {
        // Parse the access request information
        ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);

        //Update device template with access control check
        DeviceTemplate updatedDeviceTemplate = userEntityService.updateWithAccessControlCheck(deviceTemplateRepository, id, deviceTemplate.setId(id), accessRequest);

        //Return updated device template
        return ResponseEntity.ok(userEntityService.entityToEntityModel(updatedDeviceTemplate));
    }

    @DeleteMapping(path = "/{id}")
    @ApiOperation(value = "Deletes an existing device template, identified by its ID.")
    @ApiResponses({@ApiResponse(code = 204, message = "Success!"),
            @ApiResponse(code = 401, message = "Not authorized to delete this device template!"),
            @ApiResponse(code = 404, message = "Device template or requesting user not found!")})
    public ResponseEntity<Void> delete(@RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
                                       @PathVariable("id") String id) throws EntityNotFoundException, MissingPermissionException {
        // Parse the access request information
        ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);

        //Retrieve the device template with access control check
        DeviceTemplate deviceTemplate = userEntityService.getForIdWithAccessControlCheck(deviceTemplateRepository, id, ACAccessType.DELETE, accessRequest);

        //Delete the device template
        discoveryService.deleteDeviceTemplate(deviceTemplate);
        return ResponseEntity.noContent().build();
    }
}
