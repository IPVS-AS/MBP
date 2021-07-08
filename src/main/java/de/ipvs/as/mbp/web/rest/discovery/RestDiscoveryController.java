package de.ipvs.as.mbp.web.rest.discovery;

import de.ipvs.as.mbp.RestConfiguration;
import de.ipvs.as.mbp.domain.access_control.ACAccessRequest;
import de.ipvs.as.mbp.domain.access_control.ACAccessType;
import de.ipvs.as.mbp.domain.discovery.collections.DeviceDescriptionRanking;
import de.ipvs.as.mbp.domain.discovery.collections.ScoredDeviceDescription;
import de.ipvs.as.mbp.domain.discovery.device.DeviceTemplate;
import de.ipvs.as.mbp.domain.discovery.device.DeviceTemplateCreateValidator;
import de.ipvs.as.mbp.domain.discovery.device.DeviceTemplateTestDTO;
import de.ipvs.as.mbp.domain.discovery.topic.RequestTopic;
import de.ipvs.as.mbp.error.EntityNotFoundException;
import de.ipvs.as.mbp.error.MissingPermissionException;
import de.ipvs.as.mbp.repository.discovery.RequestTopicRepository;
import de.ipvs.as.mbp.service.discovery.DiscoveryService;
import de.ipvs.as.mbp.service.user.UserEntityService;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * REST Controller for discovery-related tasks.
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH + "/discovery")
@Api(tags = {"Discovery"})
public class RestDiscoveryController {

    @Autowired
    private DeviceTemplateCreateValidator deviceTemplateCreateValidator;

    @Autowired
    private RequestTopicRepository requestTopicRepository;

    @Autowired
    private UserEntityService userEntityService;

    @Autowired
    private DiscoveryService discoveryService;

    /**
     * Retrieves device descriptions that match the requirements of a given {@link DeviceTemplate} from discovery
     * repositories that are available under a given collection of {@link RequestTopic}s, provided as set
     * of their IDs. The resulting device descriptions are then processed and and ranked with respect to the scoring
     * criteria of the device template. The resulting {@link DeviceDescriptionRanking} is subsequently returned as
     * response. This way, the query results of a device template can be tested before the template is actually created.
     *
     * @param accessRequestHeader   Access request headers
     * @param deviceTemplateTestDTO A DTO containing the device template to test and the set of request topic IDs
     * @return A response containing the resulting {@link DeviceDescriptionRanking}
     * @throws EntityNotFoundException    In case a request topic or user could not be found
     * @throws MissingPermissionException In case of insufficient permissions to access one of the request topics
     */
    @PostMapping("/testDeviceTemplate")
    @ApiOperation(value = "Retrieves device descriptions that match the requirements of a given device template and processes them to a ranking.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"), @ApiResponse(code = 400, message = "The device template is invalid!"), @ApiResponse(code = 401, message = "Not authorized to access the request topic!"), @ApiResponse(code = 404, message = "Request topic or user not found!")})
    public ResponseEntity<List<ScoredDeviceDescription>> testDeviceTemplate(@RequestHeader("X-MBP-Access-Request") String accessRequestHeader, @RequestBody DeviceTemplateTestDTO deviceTemplateTestDTO) throws EntityNotFoundException, MissingPermissionException {
        //Unpack the DTO
        DeviceTemplate deviceTemplate = deviceTemplateTestDTO.getDeviceTemplate();
        Set<String> requestTopicIds = deviceTemplateTestDTO.getRequestTopicIds();

        //Get access request
        ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);

        //Validate the device template
        this.deviceTemplateCreateValidator.validateCreatable(deviceTemplate);

        //Create set for storing the request topics that result from the IDs
        Set<RequestTopic> requestTopics = new HashSet<>();

        //Iterate over all request topic IDs
        for (String topicId : requestTopicIds) {
            //Get request topic object and add it to the set
            requestTopics.add(userEntityService.getForIdWithAccessControlCheck(requestTopicRepository, topicId,
                    ACAccessType.READ, accessRequest));
        }

        //Retrieve the device descriptions and process them to a ranking
        DeviceDescriptionRanking ranking = discoveryService.retrieveDeviceDescriptions(deviceTemplate, requestTopics);

        //Return device descriptions and their associated scores
        return new ResponseEntity<>(ranking.toList(), HttpStatus.OK);
    }

    /**
     * Retrieves information about the repositories that are available for a given request topic. This way,
     * it can be tested whether a registered request topics works as intended by the user.
     *
     * @param accessRequestHeader Access request headers
     * @param topicId             The ID of the request topic to test
     * @return A response entity containing a map (repository name --> device count) with the repository information
     * @throws EntityNotFoundException    In case the request topic or user could not be found
     * @throws MissingPermissionException In case of insufficient permissions to access the request topic
     */
    @GetMapping("/getRepositories/{topic-id}")
    @ApiOperation(value = "Retrieves information about the repositories that are available for a given request topic.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"), @ApiResponse(code = 401, message = "Not authorized to access the request topic!"), @ApiResponse(code = 404, message = "Request topic or user not found!")})
    public ResponseEntity<Map<String, Integer>> getRepositories(@RequestHeader("X-MBP-Access-Request") String accessRequestHeader, @PathVariable(value = "topic-id") @ApiParam(value = "ID of the request topic", example = "5c97dc2583aeb6078c5ab672", required = true) String topicId) throws EntityNotFoundException, MissingPermissionException {
        //Retrieve request topic from repository
        RequestTopic requestTopic = userEntityService.getForIdWithAccessControlCheck(requestTopicRepository, topicId, ACAccessType.READ, ACAccessRequest.valueOf(accessRequestHeader));

        //Retrieve information about the available repositories
        Map<String, Integer> repositoryData = discoveryService.getAvailableRepositories(requestTopic);

        //Return result map
        return new ResponseEntity<>(repositoryData, HttpStatus.OK);
    }
}
