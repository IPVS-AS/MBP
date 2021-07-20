package de.ipvs.as.mbp.web.rest.discovery;

import de.ipvs.as.mbp.RestConfiguration;
import de.ipvs.as.mbp.domain.access_control.ACAccessRequest;
import de.ipvs.as.mbp.domain.access_control.ACAccessType;
import de.ipvs.as.mbp.domain.discovery.deployment.DynamicDeployment;
import de.ipvs.as.mbp.domain.discovery.deployment.DynamicDeploymentDTO;
import de.ipvs.as.mbp.domain.discovery.topic.RequestTopic;
import de.ipvs.as.mbp.error.EntityNotFoundException;
import de.ipvs.as.mbp.error.MissingPermissionException;
import de.ipvs.as.mbp.repository.OperatorRepository;
import de.ipvs.as.mbp.repository.discovery.DeviceTemplateRepository;
import de.ipvs.as.mbp.repository.discovery.DynamicDeploymentRepository;
import de.ipvs.as.mbp.repository.discovery.RequestTopicRepository;
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

import java.util.ArrayList;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * REST Controller for managing {@link DynamicDeployment}s.
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH + "/discovery/dynamic-deployments")
@Api(tags = {"Dynamic deployments"})
public class RestDynamicDeploymentController {

    @Autowired
    private DynamicDeploymentRepository dynamicDeploymentRepository;

    @Autowired
    private OperatorRepository operatorRepository;

    @Autowired
    private DeviceTemplateRepository deviceTemplateRepository;

    @Autowired
    private RequestTopicRepository requestTopicRepository;

    @Autowired
    private DiscoveryService discoveryService;

    @Autowired
    private UserEntityService userEntityService;


    @GetMapping(produces = "application/hal+json")
    @ApiOperation(value = "Retrieves all dynamic deployments that are available for the requesting user.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"), @ApiResponse(code = 404, message = "Requesting user not found!")})
    public ResponseEntity<PagedModel<EntityModel<DynamicDeployment>>> all(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @ApiParam(value = "Page parameters", required = true) Pageable pageable) {
        //Parse the access-request information
        ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);

        //Retrieve the corresponding dynamic deployments (includes access-control)
        List<DynamicDeployment> dynamicDeployments = userEntityService.getPageWithAccessControlCheck(dynamicDeploymentRepository, ACAccessType.READ, accessRequest, pageable);

        //Create self link
        Link selfLink = linkTo(methodOn(getClass()).all(accessRequestHeader, pageable)).withSelfRel();

        return ResponseEntity.ok(userEntityService.entitiesToPagedModel(dynamicDeployments, selfLink, pageable));
    }

    @GetMapping(path = "/{dynamicDeploymentId}", produces = "application/hal+json")
    @ApiOperation(value = "Retrieves an existing dynamic deployment, identified by its ID.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"),
            @ApiResponse(code = 401, message = "Not authorized to access the dynamic deployment!"),
            @ApiResponse(code = 404, message = "Dynamic deployment or requesting user not found!")})
    public ResponseEntity<EntityModel<DynamicDeployment>> one(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @PathVariable("dynamicDeploymentId") String dynamicDeploymentId) throws EntityNotFoundException, MissingPermissionException {
        //Parse the access-request information
        ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);

        // Retrieve the corresponding dynamic deployment (includes access-control)
        DynamicDeployment dynamicDeployment = userEntityService.getForIdWithAccessControlCheck(dynamicDeploymentRepository, dynamicDeploymentId, ACAccessType.READ, accessRequest);

        return ResponseEntity.ok(userEntityService.entityToEntityModel(dynamicDeployment));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/hal+json")
    @ApiOperation(value = "Creates a new dynamic deployment.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"), @ApiResponse(code = 400, message = "Dynamic deployment is invalid."), @ApiResponse(code = 401, message = "Not authorized to access the provided operator, device template or request topics."), @ApiResponse(code = 404, message = "Provided operator, device template, request topics or user not found.")})
    public ResponseEntity<EntityModel<DynamicDeployment>> create(@RequestHeader("X-MBP-Access-Request") String accessRequestHeader, @RequestBody DynamicDeploymentDTO requestDTO) throws EntityNotFoundException, MissingPermissionException {
        //Parse the access-request information
        ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);

        //Create list of request topics
        List<RequestTopic> requestTopics = new ArrayList<>();

        //Populate the list using the request topic IDs of the DTO
        for (String requestTopicId : requestDTO.getRequestTopicIds()) {
            requestTopics.add(userEntityService.getForIdWithAccessControlCheck(requestTopicRepository, requestTopicId, ACAccessType.READ, accessRequest));
        }

        //Transform DTO to dynamic deployment
        DynamicDeployment dynamicDeployment = new DynamicDeployment()
                .setName(requestDTO.getName())
                .setOperator(requestDTO.getOperatorId() == null ? null : userEntityService.getForIdWithAccessControlCheck(operatorRepository, requestDTO.getOperatorId(), ACAccessType.READ, accessRequest))
                .setDeviceTemplate(requestDTO.getDeviceTemplateId() == null ? null : userEntityService.getForIdWithAccessControlCheck(deviceTemplateRepository, requestDTO.getDeviceTemplateId(), ACAccessType.READ, accessRequest))
                .setRequestTopics(requestTopics);

        //Save dynamic deployment in repository
        DynamicDeployment createdDynamicDeployment = userEntityService.create(dynamicDeploymentRepository, dynamicDeployment);

        //Return created request topic
        return ResponseEntity.ok(userEntityService.entityToEntityModel(createdDynamicDeployment));
    }

    @DeleteMapping(path = "/{id}")
    @ApiOperation(value = "Deletes an existing dynamic deployment, identified by its ID.")
    @ApiResponses({@ApiResponse(code = 204, message = "Success!"),
            @ApiResponse(code = 401, message = "Not authorized to delete this dynamic deployment!"),
            @ApiResponse(code = 404, message = "Dynamic deployment or requesting user not found!")})
    public ResponseEntity<Void> delete(@RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
                                       @PathVariable("id") String id) throws EntityNotFoundException, MissingPermissionException {
        //Parse the access request information
        ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);

        //Delete the dynamic deployment
        userEntityService.deleteWithAccessControlCheck(dynamicDeploymentRepository, id, accessRequest);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(path = "/{id}/activate")
    @ApiOperation(value = "Activates a dynamic deployment, identified by its ID.")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"),
            @ApiResponse(code = 401, message = "Not authorized to access or activate this dynamic deployment!"),
            @ApiResponse(code = 404, message = "Dynamic deployment or requesting user not found!"),
            @ApiResponse(code = 412, message = "Dynamic deployment is already activated!")})
    public ResponseEntity<Void> activateDynamicDeployment(@RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
                                                          @PathVariable("id") String id) throws EntityNotFoundException, MissingPermissionException {
        //Parse the access request information
        ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);

        //Retrieve the dynamic deployment with access control check
        DynamicDeployment dynamicDeployment = userEntityService.getForIdWithAccessControlCheck(dynamicDeploymentRepository, id, ACAccessType.DEPLOY, accessRequest);

        //Try to activate the dynamic deployment
        this.discoveryService.activateDynamicDeployment(dynamicDeployment);

        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "/{id}/deactivate")
    @ApiOperation(value = "Deactivates a dynamic deployment, identified by its ID.")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"),
            @ApiResponse(code = 401, message = "Not authorized to access or deactivate this dynamic deployment!"),
            @ApiResponse(code = 404, message = "Dynamic deployment or requesting user not found!"),
            @ApiResponse(code = 412, message = "Dynamic deployment is already deactivated!")})
    public ResponseEntity<Void> deactivateDynamicDeployment(@RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
                                                            @PathVariable("id") String id) throws EntityNotFoundException, MissingPermissionException {
        //Parse the access request information
        ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);

        //Retrieve the dynamic deployment with access control check
        DynamicDeployment dynamicDeployment = userEntityService.getForIdWithAccessControlCheck(dynamicDeploymentRepository, id, ACAccessType.UNDEPLOY, accessRequest);

        //Try to deactivate the dynamic deployment
        this.discoveryService.deactivateDynamicDeployment(dynamicDeployment);

        return ResponseEntity.ok().build();
    }
}
