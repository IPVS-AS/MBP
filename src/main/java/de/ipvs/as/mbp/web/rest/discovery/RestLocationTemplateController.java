package de.ipvs.as.mbp.web.rest.discovery;

import de.ipvs.as.mbp.RestConfiguration;
import de.ipvs.as.mbp.domain.access_control.ACAccessRequest;
import de.ipvs.as.mbp.domain.access_control.ACAccessType;
import de.ipvs.as.mbp.domain.discovery.location.LocationTemplate;
import de.ipvs.as.mbp.domain.discovery.location.circle.CircleLocationTemplate;
import de.ipvs.as.mbp.domain.discovery.location.informal.InformalLocationTemplate;
import de.ipvs.as.mbp.domain.discovery.location.point.PointLocationTemplate;
import de.ipvs.as.mbp.domain.discovery.location.polygon.PolygonLocationTemplate;
import de.ipvs.as.mbp.error.EntityNotFoundException;
import de.ipvs.as.mbp.error.MissingPermissionException;
import de.ipvs.as.mbp.repository.discovery.LocationTemplateRepository;
import de.ipvs.as.mbp.service.UserEntityService;
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
 * REST Controller for managing {@link LocationTemplate}s.
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH + "/discovery/location-templates")
@Api(tags = {"Location templates"})
public class RestLocationTemplateController {

    @Autowired
    private LocationTemplateRepository locationTemplateRepository;

    @Autowired
    private UserEntityService userEntityService;


    @GetMapping(produces = "application/hal+json")
    @ApiOperation(value = "Retrieves all location templates that are available for the requesting user.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"), @ApiResponse(code = 404, message = "Requesting user not found!")})
    public ResponseEntity<PagedModel<EntityModel<LocationTemplate>>> all(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @ApiParam(value = "Page parameters", required = true) Pageable pageable) {
        // Parse the access-request information
        ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);

        // Retrieve the corresponding location templates (includes access-control)
        List<LocationTemplate> locationTemplates = userEntityService.getPageWithAccessControlCheck(locationTemplateRepository, ACAccessType.READ, accessRequest, pageable);

        // Create self link
        Link selfLink = linkTo(methodOn(getClass()).all(accessRequestHeader, pageable)).withSelfRel();

        return ResponseEntity.ok(userEntityService.entitiesToPagedModel(locationTemplates, selfLink, pageable));
    }

    @PostMapping(path = "/informal", consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/hal+json")
    @ApiOperation(value = "Creates a new location template of type informal.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"), @ApiResponse(code = 400, message = "Location template is invalid.")})
    public ResponseEntity<EntityModel<LocationTemplate>> createInformal(@RequestBody InformalLocationTemplate locationTemplate) throws EntityNotFoundException {
        //Save location template in repository
        LocationTemplate createdLocationTemplate = userEntityService.create(locationTemplateRepository, locationTemplate);

        //Return created location template
        return ResponseEntity.ok(userEntityService.entityToEntityModel(createdLocationTemplate));
    }

    @PostMapping(path = "/point", consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/hal+json")
    @ApiOperation(value = "Creates a new location template of type point.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"), @ApiResponse(code = 400, message = "Location template is invalid.")})
    public ResponseEntity<EntityModel<LocationTemplate>> createPoint(@RequestBody PointLocationTemplate locationTemplate) throws EntityNotFoundException {
        //Save location template in repository
        LocationTemplate createdLocationTemplate = userEntityService.create(locationTemplateRepository, locationTemplate);

        //Return created location template
        return ResponseEntity.ok(userEntityService.entityToEntityModel(createdLocationTemplate));
    }

    @PostMapping(path = "/circle", consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/hal+json")
    @ApiOperation(value = "Creates a new location template of type circle area.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"), @ApiResponse(code = 400, message = "Location template is invalid.")})
    public ResponseEntity<EntityModel<LocationTemplate>> createCircleArea(@RequestBody CircleLocationTemplate locationTemplate) throws EntityNotFoundException {
        //Save location template in repository
        LocationTemplate createdLocationTemplate = userEntityService.create(locationTemplateRepository, locationTemplate);

        //Return created location template
        return ResponseEntity.ok(userEntityService.entityToEntityModel(createdLocationTemplate));
    }

    @PostMapping(path = "/polygon", consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/hal+json")
    @ApiOperation(value = "Creates a new location template of type polygon area.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"), @ApiResponse(code = 400, message = "Location template is invalid.")})
    public ResponseEntity<EntityModel<LocationTemplate>> createPolygonArea(@RequestBody PolygonLocationTemplate locationTemplate) throws EntityNotFoundException {
        //Save location template in repository
        LocationTemplate createdLocationTemplate = userEntityService.create(locationTemplateRepository, locationTemplate);

        //Return created location template
        return ResponseEntity.ok(userEntityService.entityToEntityModel(createdLocationTemplate));
    }


    @PutMapping(path = "/informal/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/hal+json")
    @ApiOperation(value = "Updates an existing location template of type informal.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"), @ApiResponse(code = 400, message = "Location template is invalid."), @ApiResponse(code = 404, message = "Location template not found!")})
    public ResponseEntity<EntityModel<LocationTemplate>> updateInformal(@PathVariable(name = "id") String id, @RequestHeader("X-MBP-Access-Request") String accessRequestHeader, @RequestBody InformalLocationTemplate locationTemplate) throws MissingPermissionException, EntityNotFoundException {
        // Parse the access request information
        ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);

        //Update location template with access control check
        LocationTemplate updatedLocationTemplate = userEntityService.updateWithAccessControlCheck(locationTemplateRepository, id, locationTemplate.setId(id), accessRequest);

        //Return updated location template
        return ResponseEntity.ok(userEntityService.entityToEntityModel(updatedLocationTemplate));
    }

    @PutMapping(path = "/point/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/hal+json")
    @ApiOperation(value = "Updates an existing location template of type point.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"), @ApiResponse(code = 400, message = "Location template is invalid."), @ApiResponse(code = 404, message = "Location template not found!")})
    public ResponseEntity<EntityModel<LocationTemplate>> updatePoint(@PathVariable(name = "id") String id, @RequestHeader("X-MBP-Access-Request") String accessRequestHeader, @RequestBody PointLocationTemplate locationTemplate) throws MissingPermissionException, EntityNotFoundException {
        // Parse the access request information
        ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);

        //Update location template with access control check
        LocationTemplate updatedLocationTemplate = userEntityService.updateWithAccessControlCheck(locationTemplateRepository, id, locationTemplate.setId(id), accessRequest);

        //Return updated location template
        return ResponseEntity.ok(userEntityService.entityToEntityModel(updatedLocationTemplate));
    }

    @PutMapping(path = "/circle/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/hal+json")
    @ApiOperation(value = "Updates an existing location template of type circle area.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"), @ApiResponse(code = 400, message = "Location template is invalid."), @ApiResponse(code = 404, message = "Location template not found!")})
    public ResponseEntity<EntityModel<LocationTemplate>> updateCircleArea(@PathVariable(name = "id") String id, @RequestHeader("X-MBP-Access-Request") String accessRequestHeader, @RequestBody CircleLocationTemplate locationTemplate) throws MissingPermissionException, EntityNotFoundException {
        // Parse the access request information
        ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);

        //Update location template with access control check
        LocationTemplate updatedLocationTemplate = userEntityService.updateWithAccessControlCheck(locationTemplateRepository, id, locationTemplate.setId(id), accessRequest);

        //Return updated location template
        return ResponseEntity.ok(userEntityService.entityToEntityModel(updatedLocationTemplate));
    }

    @PutMapping(path = "/polygon/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/hal+json")
    @ApiOperation(value = "Updates an existing location template of type polygon area.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"), @ApiResponse(code = 400, message = "Location template is invalid."), @ApiResponse(code = 404, message = "Location template not found!")})
    public ResponseEntity<EntityModel<LocationTemplate>> updatePolygonArea(@PathVariable(name = "id") String id, @RequestHeader("X-MBP-Access-Request") String accessRequestHeader, @RequestBody PolygonLocationTemplate locationTemplate) throws MissingPermissionException, EntityNotFoundException {
        // Parse the access request information
        ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);

        //Update location template with access control check
        LocationTemplate updatedLocationTemplate = userEntityService.updateWithAccessControlCheck(locationTemplateRepository, id, locationTemplate.setId(id), accessRequest);

        //Return updated location template
        return ResponseEntity.ok(userEntityService.entityToEntityModel(updatedLocationTemplate));
    }

    @DeleteMapping(path = "/{id}")
    @ApiOperation(value = "Deletes an existing location template, identified by its ID.")
    @ApiResponses({@ApiResponse(code = 204, message = "Success!"),
            @ApiResponse(code = 401, message = "Not authorized to delete this location template!"),
            @ApiResponse(code = 404, message = "Location template or requesting user not found!")})
    public ResponseEntity<Void> delete(@RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
                                       @PathVariable("id") String id) throws EntityNotFoundException, MissingPermissionException {
        // Parse the access request information
        ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);

        //Delete the location template
        userEntityService.deleteWithAccessControlCheck(locationTemplateRepository, id, accessRequest);
        return ResponseEntity.noContent().build();
    }
}
