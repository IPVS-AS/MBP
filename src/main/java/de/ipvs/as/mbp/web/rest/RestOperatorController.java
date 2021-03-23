package de.ipvs.as.mbp.web.rest;

import de.ipvs.as.mbp.domain.operator.Operator;
import de.ipvs.as.mbp.domain.operator.OperatorRequestDTO;
import de.ipvs.as.mbp.repository.DataModelRepository;
import io.swagger.annotations.*;
import de.ipvs.as.mbp.RestConfiguration;
import de.ipvs.as.mbp.domain.access_control.ACAccessRequest;
import de.ipvs.as.mbp.domain.access_control.ACAccessType;
import de.ipvs.as.mbp.error.EntityAlreadyExistsException;
import de.ipvs.as.mbp.error.EntityNotFoundException;
import de.ipvs.as.mbp.error.MissingPermissionException;
import de.ipvs.as.mbp.repository.OperatorRepository;
import de.ipvs.as.mbp.service.UserEntityService;
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
 * REST Controller for managing {@link Operator}s.
 *
 * @author Jakob Benz
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH + "/operators")
@Api(tags = {"Operators"})
public class RestOperatorController {

    @Autowired
    private OperatorRepository operatorRepository;

    @Autowired
    private DataModelRepository dataModelRepository;

    @Autowired
    private UserEntityService userEntityService;


    @GetMapping(produces = "application/hal+json")
    @ApiOperation(value = "Retrieves all existing operator entities available for the requesting entity.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"),
            @ApiResponse(code = 404, message = "Operator or requesting user not found!")})
    public ResponseEntity<PagedModel<EntityModel<Operator>>> all(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @ApiParam(value = "Page parameters", required = true) Pageable pageable) {
        // Parse the access-request information
        ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);

        // Retrieve the corresponding operators (includes access-control)
        List<Operator> operators = userEntityService.getPageWithAccessControlCheck(operatorRepository, ACAccessType.READ, accessRequest, pageable);

        // Create self link
        Link selfLink = linkTo(methodOn(getClass()).all(accessRequestHeader, pageable)).withSelfRel();

        return ResponseEntity.ok(userEntityService.entitiesToPagedModel(operators, selfLink, pageable));
    }

    @GetMapping(path = "/{operatorId}", produces = "application/hal+json")
    @ApiOperation(value = "Retrieves an existing operator entity identified by its id if it's available for the requesting entity.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"),
            @ApiResponse(code = 401, message = "Not authorized to access the operator!"),
            @ApiResponse(code = 404, message = "OPerator or requesting user not found!")})
    public ResponseEntity<EntityModel<Operator>> one(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @PathVariable("operatorId") String operatorId,
            @ApiParam(value = "Page parameters", required = true) Pageable pageable) throws EntityNotFoundException, MissingPermissionException {
        // Retrieve the corresponding operator (includes access-control)
        Operator operator = userEntityService.getForIdWithAccessControlCheck(operatorRepository, operatorId, ACAccessType.READ, ACAccessRequest.valueOf(accessRequestHeader));
        return ResponseEntity.ok(userEntityService.entityToEntityModel(operator));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/hal+json")
    @ApiOperation(value = "Retrieves an existing operator entity identified by its id if it's available for the requesting entity.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"),
            @ApiResponse(code = 409, message = "Operator already exists!")})
    public ResponseEntity<EntityModel<Operator>> create(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @ApiParam(value = "Page parameters", required = true) Pageable pageable,
            @RequestBody OperatorRequestDTO requestDTO) throws EntityAlreadyExistsException, EntityNotFoundException {
        // Create operator from request DTO
        Operator operator = (Operator) new Operator()
                .setName(requestDTO.getName())
                .setDataModel(requestDTO.getDataModelId() == null ? null : userEntityService.getForId(dataModelRepository, requestDTO.getDataModelId()))
                .setDescription(requestDTO.getDescription())
                .setParameters(requestDTO.getParameters())
                .setRoutines(requestDTO.getRoutines())
                .setUnit(requestDTO.getUnit())
                .setAccessControlPolicyIds(requestDTO.getAccessControlPolicyIds());

        //Replace bad line breaks of plain text operator files
        operator.replaceLineBreaks();

        // Save operator in the database
        Operator createdOperator = userEntityService.create(operatorRepository, operator);
        return ResponseEntity.ok(userEntityService.entityToEntityModel(createdOperator));
    }

    @DeleteMapping(path = "/{operatorId}")
    @ApiOperation(value = "Deletes an existing operator entity identified by its id if it's available for the requesting entity.")
    @ApiResponses({@ApiResponse(code = 204, message = "Success!"),
            @ApiResponse(code = 401, message = "Not authorized to delete the operator!"),
            @ApiResponse(code = 404, message = "Operator or requesting user not found!")})
    public ResponseEntity<Void> delete(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @PathVariable("operatorId") String operatorId) throws EntityNotFoundException, MissingPermissionException {
        // Delete the operator (includes access-control)
        userEntityService.deleteWithAccessControlCheck(operatorRepository, operatorId, ACAccessRequest.valueOf(accessRequestHeader));
        return ResponseEntity.noContent().build();
    }

}
