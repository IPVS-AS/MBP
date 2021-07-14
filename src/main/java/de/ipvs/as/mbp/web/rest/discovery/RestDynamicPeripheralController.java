package de.ipvs.as.mbp.web.rest.discovery;

import de.ipvs.as.mbp.RestConfiguration;
import de.ipvs.as.mbp.domain.access_control.ACAccessRequest;
import de.ipvs.as.mbp.domain.access_control.ACAccessType;
import de.ipvs.as.mbp.domain.discovery.peripheral.DynamicPeripheral;
import de.ipvs.as.mbp.domain.discovery.peripheral.DynamicPeripheralDTO;
import de.ipvs.as.mbp.domain.discovery.topic.RequestTopic;
import de.ipvs.as.mbp.error.EntityNotFoundException;
import de.ipvs.as.mbp.error.MissingPermissionException;
import de.ipvs.as.mbp.repository.OperatorRepository;
import de.ipvs.as.mbp.repository.discovery.DeviceTemplateRepository;
import de.ipvs.as.mbp.repository.discovery.DynamicPeripheralRepository;
import de.ipvs.as.mbp.repository.discovery.RequestTopicRepository;
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
 * REST Controller for managing {@link DynamicPeripheral}s.
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH + "/discovery/dynamic-peripherals")
@Api(tags = {"Dynamic peripherals"})
public class RestDynamicPeripheralController {

    @Autowired
    private DynamicPeripheralRepository dynamicPeripheralRepository;

    @Autowired
    private OperatorRepository operatorRepository;

    @Autowired
    private DeviceTemplateRepository deviceTemplateRepository;

    @Autowired
    private RequestTopicRepository requestTopicRepository;

    @Autowired
    private UserEntityService userEntityService;


    @GetMapping(produces = "application/hal+json")
    @ApiOperation(value = "Retrieves all dynamic peripherals that are available for the requesting user.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"), @ApiResponse(code = 404, message = "Requesting user not found!")})
    public ResponseEntity<PagedModel<EntityModel<DynamicPeripheral>>> all(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @ApiParam(value = "Page parameters", required = true) Pageable pageable) {
        //Parse the access-request information
        ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);

        //Retrieve the corresponding dynamic peripherals (includes access-control)
        List<DynamicPeripheral> dynamicPeripherals = userEntityService.getPageWithAccessControlCheck(dynamicPeripheralRepository, ACAccessType.READ, accessRequest, pageable);

        //Create self link
        Link selfLink = linkTo(methodOn(getClass()).all(accessRequestHeader, pageable)).withSelfRel();

        return ResponseEntity.ok(userEntityService.entitiesToPagedModel(dynamicPeripherals, selfLink, pageable));
    }

    @GetMapping(path = "/{dynamicPeripheralId}", produces = "application/hal+json")
    @ApiOperation(value = "Retrieves an existing dynamic peripheral, identified by its ID.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"),
            @ApiResponse(code = 401, message = "Not authorized to access the dynamic peripheral!"),
            @ApiResponse(code = 404, message = "Dynamic peripheral or requesting user not found!")})
    public ResponseEntity<EntityModel<DynamicPeripheral>> one(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @ApiParam(value = "Page parameters", required = true) Pageable pageable,
            @PathVariable("dynamicPeripheralId") String dynamicPeripheralId) throws EntityNotFoundException, MissingPermissionException {
        //Parse the access-request information
        ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);

        // Retrieve the corresponding dynamic peripheral (includes access-control)
        DynamicPeripheral dynamicPeripheral = userEntityService.getForIdWithAccessControlCheck(dynamicPeripheralRepository, dynamicPeripheralId, ACAccessType.READ, accessRequest);

        return ResponseEntity.ok(userEntityService.entityToEntityModel(dynamicPeripheral));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/hal+json")
    @ApiOperation(value = "Creates a new dynamic peripheral.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"), @ApiResponse(code = 400, message = "Dynamic peripheral is invalid."), @ApiResponse(code = 401, message = "Not authorized to access the provided operator, device template or request topics."), @ApiResponse(code = 404, message = "Provided operator, device template, request topics or user not found.")})
    public ResponseEntity<EntityModel<DynamicPeripheral>> create(@RequestHeader("X-MBP-Access-Request") String accessRequestHeader, @RequestBody DynamicPeripheralDTO requestDTO) throws EntityNotFoundException, MissingPermissionException {
        //Parse the access-request information
        ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);

        //Create list of request topics
        List<RequestTopic> requestTopics = new ArrayList<>();

        //Populate the list using the request topic IDs of the DTO
        for (String requestTopicId : requestDTO.getRequestTopicIds()) {
            requestTopics.add(userEntityService.getForIdWithAccessControlCheck(requestTopicRepository, requestTopicId, ACAccessType.READ, accessRequest));
        }

        //Transform DTO to dynamic peripheral
        DynamicPeripheral dynamicPeripheral = new DynamicPeripheral()
                .setName(requestDTO.getName())
                .setOperator(requestDTO.getOperatorId() == null ? null : userEntityService.getForIdWithAccessControlCheck(operatorRepository, requestDTO.getOperatorId(), ACAccessType.READ, accessRequest))
                .setDeviceTemplate(requestDTO.getDeviceTemplateId() == null ? null : userEntityService.getForIdWithAccessControlCheck(deviceTemplateRepository, requestDTO.getDeviceTemplateId(), ACAccessType.READ, accessRequest))
                .setRequestTopics(requestTopics);

        //Save dynamic peripheral in repository
        DynamicPeripheral createdDynamicPeripheral = userEntityService.create(dynamicPeripheralRepository, dynamicPeripheral);

        //Return created request topic
        return ResponseEntity.ok(userEntityService.entityToEntityModel(createdDynamicPeripheral));
    }

    @DeleteMapping(path = "/{id}")
    @ApiOperation(value = "Deletes an existing dynamic peripheral, identified by its ID.")
    @ApiResponses({@ApiResponse(code = 204, message = "Success!"),
            @ApiResponse(code = 401, message = "Not authorized to delete this dynamic peripheral!"),
            @ApiResponse(code = 404, message = "Dynamic peripheral or requesting user not found!")})
    public ResponseEntity<Void> delete(@RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
                                       @PathVariable("id") String id) throws EntityNotFoundException, MissingPermissionException {
        //Parse the access request information
        ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);

        //Delete the dynamic peripheral
        userEntityService.deleteWithAccessControlCheck(dynamicPeripheralRepository, id, accessRequest);
        return ResponseEntity.noContent().build();
    }
}
