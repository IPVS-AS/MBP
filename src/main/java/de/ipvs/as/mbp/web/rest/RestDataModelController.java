package de.ipvs.as.mbp.web.rest;

import de.ipvs.as.mbp.RestConfiguration;
import de.ipvs.as.mbp.domain.access_control.ACAccessRequest;
import de.ipvs.as.mbp.domain.access_control.ACAccessType;
import de.ipvs.as.mbp.domain.data_model.DataModel;
import de.ipvs.as.mbp.domain.rules.RuleAction;
import de.ipvs.as.mbp.error.EntityAlreadyExistsException;
import de.ipvs.as.mbp.error.EntityNotFoundException;
import de.ipvs.as.mbp.error.MissingPermissionException;
import de.ipvs.as.mbp.repository.DataModelRepository;
import de.ipvs.as.mbp.repository.RuleActionRepository;
import de.ipvs.as.mbp.service.UserEntityService;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        System.out.println("------------------\n" + dataModel.getName() + "\n" + dataModel.getDescription()
        + "\n" + dataModel.getId() + "\n" + dataModel.getOwnerName());
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
        System.out.println("test");
        DataModel dataModel = userEntityService.getForIdWithAccessControlCheck(dataModelRepository, dataModelId, ACAccessType.READ, ACAccessRequest.valueOf(accessRequestHeader));
        return ResponseEntity.ok(userEntityService.entityToEntityModel(dataModel));
    }
}
