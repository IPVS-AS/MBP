package de.ipvs.as.mbp.web.rest;

import de.ipvs.as.mbp.RestConfiguration;
import de.ipvs.as.mbp.domain.access_control.ACAccessRequest;
import de.ipvs.as.mbp.domain.access_control.ACAccessType;
import de.ipvs.as.mbp.domain.component.ComponentDTO;
import de.ipvs.as.mbp.domain.component.Sensor;
import de.ipvs.as.mbp.domain.testing.TestDetails;
import de.ipvs.as.mbp.error.EntityAlreadyExistsException;
import de.ipvs.as.mbp.error.EntityNotFoundException;
import de.ipvs.as.mbp.error.EntityStillInUseException;
import de.ipvs.as.mbp.error.MissingPermissionException;
import de.ipvs.as.mbp.repository.DeviceRepository;
import de.ipvs.as.mbp.repository.OperatorRepository;
import de.ipvs.as.mbp.repository.SensorRepository;
import de.ipvs.as.mbp.repository.TestDetailsRepository;
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
 * REST Controller for managing {@link Sensor}s.
 *
 * @author Jakob Benz
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH + "/sensors")
@Api(tags = {"Sensors"})
public class RestSensorController {

    @Autowired
    private SensorRepository sensorRepository;

    @Autowired
    private OperatorRepository operatorRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private TestDetailsRepository testDetailsRepository;

    @Autowired
    private UserEntityService userEntityService;


    @GetMapping(produces = "application/hal+json")
    @ApiOperation(value = "Retrieves all existing sensor entities available for the requesting entity.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"),
            @ApiResponse(code = 404, message = "Sensor or requesting user not found!")})
    public ResponseEntity<PagedModel<EntityModel<Sensor>>> all(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @ApiParam(value = "Page parameters", required = true) Pageable pageable) {
        // Parse the access-request information
        ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);

        // Retrieve the corresponding sensors (includes access-control)
        List<Sensor> sensors = userEntityService.getPageWithAccessControlCheck(sensorRepository, ACAccessType.READ, accessRequest, pageable);

        // Create self link
        Link selfLink = linkTo(methodOn(getClass()).all(accessRequestHeader, pageable)).withSelfRel();

        return ResponseEntity.ok(userEntityService.entitiesToPagedModel(sensors, selfLink, pageable));
    }

    @GetMapping(path = "/{sensorId}", produces = "application/hal+json")
    @ApiOperation(value = "Retrieves an existing sensor entity identified by its id if it's available for the requesting entity.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"),
            @ApiResponse(code = 401, message = "Not authorized to access the sensor!"),
            @ApiResponse(code = 404, message = "Sensor or requesting user not found!")})
    public ResponseEntity<EntityModel<Sensor>> one(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @PathVariable("sensorId") String sensorId,
            @ApiParam(value = "Page parameters", required = true) Pageable pageable) throws EntityNotFoundException, MissingPermissionException {
        // Retrieve the corresponding sensor (includes access-control)
        Sensor sensor = userEntityService.getForIdWithAccessControlCheck(sensorRepository, sensorId, ACAccessType.READ, ACAccessRequest.valueOf(accessRequestHeader));
        return ResponseEntity.ok(userEntityService.entityToEntityModel(sensor));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/hal+json")
    @ApiOperation(value = "Retrieves an existing sensor entity identified by its id if it's available for the requesting entity.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"),
            @ApiResponse(code = 409, message = "Sensor already exists!")})
    public ResponseEntity<EntityModel<Sensor>> create(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @ApiParam(value = "Page parameters", required = true) Pageable pageable,
            @RequestBody ComponentDTO requestDto) throws EntityAlreadyExistsException, EntityNotFoundException {
        // Create sensor from request DTO
        Sensor sensor = (Sensor) new Sensor()
                .setName(requestDto.getName())
                .setComponentType(requestDto.getComponentType())
                .setOperator(requestDto.getOperatorId() == null ? null : userEntityService.getForId(operatorRepository, requestDto.getOperatorId()))
                .setDevice(requestDto.getDeviceId() == null ? null : userEntityService.getForId(deviceRepository, requestDto.getDeviceId()))
                .setAccessControlPolicyIds(requestDto.getAccessControlPolicyIds());

        // Save sensor in the database
        Sensor createdSensor = userEntityService.create(sensorRepository, sensor);
        return ResponseEntity.ok(userEntityService.entityToEntityModel(createdSensor));
    }

    @DeleteMapping(path = "/{sensorId}")
    @ApiOperation(value = "Deletes an existing sensor entity identified by its id if it's available for the requesting entity.")
    @ApiResponses({@ApiResponse(code = 204, message = "Success!"),
            @ApiResponse(code = 401, message = "Not authorized to delete the sensor!"),
            @ApiResponse(code = 404, message = "Sensor or requesting user not found!")})
    public ResponseEntity<Void> delete(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @PathVariable("sensorId") String sensorId) throws EntityNotFoundException, MissingPermissionException, EntityStillInUseException {
        //Get access request
        ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);

        //Retrieve all test details that use the given sensor
        List<TestDetails> affectedTestDetails = userEntityService.filterForAdminOwnerAndPolicies(() -> testDetailsRepository.findAllBySensorId(sensorId), ACAccessType.READ, accessRequest);

        //Check if there are affected test details
        if (!affectedTestDetails.isEmpty()) {
            throw new EntityStillInUseException("The sensor is still used by at least one test and thus cannot be deleted.");
        }

        // Delete the sensor (including access control)
        userEntityService.deleteWithAccessControlCheck(sensorRepository, sensorId, accessRequest);
        return ResponseEntity.noContent().build();
    }

}
