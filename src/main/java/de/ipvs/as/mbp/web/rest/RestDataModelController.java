package de.ipvs.as.mbp.web.rest;

import de.ipvs.as.mbp.RestConfiguration;
import de.ipvs.as.mbp.domain.access_control.ACAccessRequest;
import de.ipvs.as.mbp.domain.access_control.ACAccessType;
import de.ipvs.as.mbp.domain.component.Sensor;
import de.ipvs.as.mbp.domain.data_model.DataModel;
import de.ipvs.as.mbp.domain.rules.RuleAction;
import de.ipvs.as.mbp.domain.units.PredefinedQuantity;
import de.ipvs.as.mbp.error.EntityAlreadyExistsException;
import de.ipvs.as.mbp.error.EntityNotFoundException;
import de.ipvs.as.mbp.error.MissingPermissionException;
import de.ipvs.as.mbp.repository.DataModelRepository;
import de.ipvs.as.mbp.repository.RuleActionRepository;
import de.ipvs.as.mbp.service.user.UserEntityService;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * REST controller for requests related to the creation of new data models.
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH + "/data-models")
@Api(tags = {"Data models"})
public class RestDataModelController {

    @Autowired
    private DataModelRepository dataModelRepository;

    @Autowired
    private UserEntityService userEntityService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/hal+json")
    @ApiOperation(value = "Creates a new data model entity.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"),
            @ApiResponse(code = 409, message = "Data model already exists!")})
    public ResponseEntity<EntityModel<DataModel>> create(
            @ApiParam(value = "Page parameters", required = true) Pageable pageable,
            @RequestBody DataModel dataModel) throws EntityAlreadyExistsException, EntityNotFoundException {
        // Save data model in the database
        DataModel createdDataModel = userEntityService.create(dataModelRepository, dataModel);
        return ResponseEntity.ok(userEntityService.entityToEntityModel(createdDataModel));
    }

    @GetMapping(path = "/{dataModelId}", produces = "application/hal+json")
    @ApiOperation(value = "Retrieves an existing data model entity identified by its id if it's available for the requesting entity.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"),
            @ApiResponse(code = 401, message = "Not authorized to access the data model!"),
            @ApiResponse(code = 404, message = "Data model or requesting user not found!")})
    public ResponseEntity<EntityModel<DataModel>> one(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @PathVariable("dataModelId") String dataModelId, @ApiParam(value = "Page parameters", required = true) Pageable pageable) throws EntityNotFoundException, MissingPermissionException {
        // Retrieve the corresponding rule action (includes access-control)
        DataModel dataModel = userEntityService.getForIdWithAccessControlCheck(dataModelRepository, dataModelId, ACAccessType.READ, ACAccessRequest.valueOf(accessRequestHeader));
        return ResponseEntity.ok(userEntityService.entityToEntityModel(dataModel));
    }

    @GetMapping(produces = "application/hal+json")
    @ApiOperation(value = "Retrieves all existing data model entities available for the requesting entity.", produces = "application/hal+json")
    @ApiResponses({ @ApiResponse(code = 200, message = "Success!"),
            @ApiResponse(code = 404, message = "Data model or requesting user not found!") })
    public ResponseEntity<PagedModel<EntityModel<DataModel>>> all(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @ApiParam(value = "Page parameters", required = true) Pageable pageable) {

        // Parse the access-request information
        ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);

        // Retrieve the corresponding data models (includes access-control)
        List<DataModel> dataModels = userEntityService.getPageWithAccessControlCheck(dataModelRepository, ACAccessType.READ, accessRequest, pageable);

        // Create self link
        Link selfLink = linkTo(methodOn(getClass()).all(accessRequestHeader, pageable)).withSelfRel();

        return ResponseEntity.ok(userEntityService.entitiesToPagedModel(dataModels, selfLink, pageable));
    }

    @DeleteMapping(path = "/{dataModelId}")
    @ApiOperation(value = "Deletes an existing data model entity identified by its id if it's available for the requesting entity.")
    @ApiResponses({@ApiResponse(code = 204, message = "Success!"),
            @ApiResponse(code = 401, message = "Not authorized to delete the data model!"),
            @ApiResponse(code = 404, message = "Data model or requesting user not found!")})
    public ResponseEntity<Void> delete(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @PathVariable("dataModelId") String dataModelId) throws EntityNotFoundException, MissingPermissionException {
        // Delete the data model (includes access-control)
        userEntityService.deleteWithAccessControlCheck(dataModelRepository, dataModelId, ACAccessRequest.valueOf(accessRequestHeader));
        return ResponseEntity.noContent().build();
    }
}
