package de.ipvs.as.mbp.web.rest;

import de.ipvs.as.mbp.RestConfiguration;
import de.ipvs.as.mbp.domain.discovery.topic.RequestTopic;
import de.ipvs.as.mbp.repository.discovery.RequestTopicRepository;
import de.ipvs.as.mbp.service.discovery.DiscoveryGateway;
import de.ipvs.as.mbp.service.discovery.DiscoveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * REST Controller for debugging.
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH)
@ApiIgnore("Controller exists only for debugging purposes")
public class RestDebugController {

    @Autowired
    private RequestTopicRepository requestTopicRepository;

    @Autowired
    private DiscoveryService discoveryService;

    /**
     * REST interface for debugging purposes. Feel free to implement your own debugging and testing stuff here,
     * but please clean up before committing.
     *
     * @return Debugging output specified by the developer
     */
    @RequestMapping(value = "/debug", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Integer>> debug() throws ExecutionException, InterruptedException {
        //Check for empty
        if (requestTopicRepository.findAll().size() < 1) {
            return new ResponseEntity<>(null, HttpStatus.OK);
        }

        //Get first request topic
        RequestTopic firstTopic = requestTopicRepository.findAll().get(0);

        Map<String, Integer> result = discoveryService.getAvailableRepositories(firstTopic);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
