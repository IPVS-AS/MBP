package de.ipvs.as.mbp.web.rest;

import de.ipvs.as.mbp.constants.Constants;
import de.ipvs.as.mbp.domain.access_control.ACAccessRequest;
import de.ipvs.as.mbp.domain.access_control.ACAccessType;
import de.ipvs.as.mbp.domain.entity_type.DeviceType;
import de.ipvs.as.mbp.domain.monitoring.MonitoringOperator;
import de.ipvs.as.mbp.domain.monitoring.MonitoringOperatorDTO;
import de.ipvs.as.mbp.error.EntityAlreadyExistsException;
import de.ipvs.as.mbp.error.EntityNotFoundException;
import de.ipvs.as.mbp.error.MissingPermissionException;
import de.ipvs.as.mbp.repository.DeviceTypeRepository;
import de.ipvs.as.mbp.repository.MonitoringOperatorRepository;
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

import java.util.ArrayList;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * REST Controller for managing {@link MonitoringOperator}s.
 *
 * @author Jakob Benz
 */
@RestController
@RequestMapping(Constants.BASE_PATH + "/monitoring-operators")
@Api(tags = {"Monitoring Operators"})
public class RestMonitoringOperatorController {
    @Autowired
    private DeviceTypeRepository deviceTypeRepository;

    @Autowired
    private MonitoringOperatorRepository monitoringOperatorRepository;

    @Autowired
    private UserEntityService userEntityService;


    @GetMapping(produces = "application/hal+json")
    @ApiOperation(value = "Retrieves all existing monitoring operator entities available for the requesting entity.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"),
            @ApiResponse(code = 404, message = "Monitoring monitoring operator or requesting user not found!")})
    public ResponseEntity<PagedModel<EntityModel<MonitoringOperator>>> all(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @ApiParam(value = "Page parameters", required = true) Pageable pageable) {
        // Parse the access-request information
        ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);

        // Retrieve the corresponding monitoring operators (includes access-control)
        List<MonitoringOperator> monitoringOperators = userEntityService.getPageWithAccessControlCheck(monitoringOperatorRepository, ACAccessType.READ, accessRequest, pageable);

        // Create self link
        Link selfLink = linkTo(methodOn(getClass()).all(accessRequestHeader, pageable)).withSelfRel();

        return ResponseEntity.ok(userEntityService.entitiesToPagedModel(monitoringOperators, selfLink, pageable));
    }

    @GetMapping(path = "/{monitoringOperatorId}", produces = "application/hal+json")
    @ApiOperation(value = "Retrieves an existing monitoring operator entity identified by its id if it's available for the requesting entity.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"),
            @ApiResponse(code = 401, message = "Not authorized to access the monitoring operator!"),
            @ApiResponse(code = 404, message = "Monitoring operator or requesting user not found!")})
    public ResponseEntity<EntityModel<MonitoringOperator>> one(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @PathVariable("monitoringOperatorId") String monitoringOperatorId,
            @ApiParam(value = "Page parameters", required = true) Pageable pageable) throws EntityNotFoundException, MissingPermissionException {
        // Retrieve the corresponding monitoring operator (includes access-control)
        MonitoringOperator monitoringOperator = userEntityService.getForIdWithAccessControlCheck(monitoringOperatorRepository, monitoringOperatorId, ACAccessType.READ, ACAccessRequest.valueOf(accessRequestHeader));
        return ResponseEntity.ok(userEntityService.entityToEntityModel(monitoringOperator));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/hal+json")
    @ApiOperation(value = "Retrieves an existing monitoring operator entity identified by its id if it's available for the requesting entity.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"),
            @ApiResponse(code = 409, message = "Monitoring operator already exists!")})
    public ResponseEntity<EntityModel<MonitoringOperator>> create(@ApiParam(value = "Page parameters", required = true) Pageable pageable, @RequestBody MonitoringOperatorDTO monitoringOperatorDTO) throws EntityAlreadyExistsException, EntityNotFoundException {
        //Create monitoring operator from DTO
        MonitoringOperator monitoringOperator = (MonitoringOperator) new MonitoringOperator()
                .setName(monitoringOperatorDTO.getName())
                .setDescription(monitoringOperatorDTO.getDescription())
                .setUnit(monitoringOperatorDTO.getUnit())
                .setRoutines(monitoringOperatorDTO.getRoutines())
                .setParameters(monitoringOperatorDTO.getParameters())
                .setAccessControlPolicyIds(monitoringOperatorDTO.getAccessControlPolicyIds());

        //Check whether device types were provided
        if (monitoringOperatorDTO.getDeviceTypes() == null) {
            monitoringOperator.setDeviceTypes(new ArrayList<>());
        } else {
            //Resolve device types
            List<DeviceType> deviceTypes = new ArrayList<>();
            for (String deviceTypeId : monitoringOperatorDTO.getDeviceTypes()) {
                deviceTypes.add(userEntityService.getForId(deviceTypeRepository, deviceTypeId));
            }
            monitoringOperator.setDeviceTypes(deviceTypes);
        }

        // Save monitoring operator in the database
        MonitoringOperator createdMonitoringOperator = userEntityService.create(monitoringOperatorRepository, monitoringOperator);
        return ResponseEntity.ok(userEntityService.entityToEntityModel(createdMonitoringOperator));
    }

    @DeleteMapping(path = "/{monitoringOperatorId}")
    @ApiOperation(value = "Deletes an existing monitoring operator entity identified by its id if it's available for the requesting entity.")
    @ApiResponses({@ApiResponse(code = 204, message = "Success!"),
            @ApiResponse(code = 401, message = "Not authorized to delete the monitoring operator!"),
            @ApiResponse(code = 404, message = "Monitoring operator or requesting user not found!")})
    public ResponseEntity<Void> delete(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @PathVariable("monitoringOperatorId") String monitoringOperatorId) throws EntityNotFoundException, MissingPermissionException {
        // Delete the monitoring operator (includes access-control)
        userEntityService.deleteWithAccessControlCheck(monitoringOperatorRepository, monitoringOperatorId, ACAccessRequest.valueOf(accessRequestHeader));
        return ResponseEntity.noContent().build();
    }

}
