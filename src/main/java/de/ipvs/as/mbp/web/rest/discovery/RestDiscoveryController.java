package de.ipvs.as.mbp.web.rest.discovery;

import de.ipvs.as.mbp.RestConfiguration;
import de.ipvs.as.mbp.domain.access_control.ACAccessRequest;
import de.ipvs.as.mbp.domain.access_control.ACAccessType;
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

import java.util.Map;

/**
 * REST Controller for discovery-related tasks.
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH + "/discovery")
@Api(tags = {"Discovery"})
public class RestDiscoveryController {

    @Autowired
    private RequestTopicRepository requestTopicRepository;

    @Autowired
    private UserEntityService userEntityService;

    @Autowired
    private DiscoveryService discoveryService;

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
