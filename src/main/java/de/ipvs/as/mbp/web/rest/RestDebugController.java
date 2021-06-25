package de.ipvs.as.mbp.web.rest;

import de.ipvs.as.mbp.RestConfiguration;
import de.ipvs.as.mbp.service.messaging.PubSubService;
import de.ipvs.as.mbp.service.messaging.scatter_gather.ScatterGatherConfig;
import de.ipvs.as.mbp.service.messaging.scatter_gather.ScatterGatherRequest;
import de.ipvs.as.mbp.service.messaging.scatter_gather.ScatterGatherRequestBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * REST Controller for debugging.
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH)
@ApiIgnore("Controller exists only for debugging purposes")
public class RestDebugController {

    @Autowired
    private PubSubService pubSubService;

    /**
     * REST interface for debugging purposes. Feel free to implement your own debugging and testing stuff here,
     * but please clean up before committing.
     *
     * @return Debugging output specified by the developer
     */
    @RequestMapping(value = "/debug", method = RequestMethod.GET)
    public ResponseEntity<String> debug() throws ExecutionException, InterruptedException {
        ScatterGatherRequestBuilder scatterGatherRequestBuilder = new ScatterGatherRequestBuilder(pubSubService);

        //Create request configurations
        ScatterGatherConfig config1 = new ScatterGatherConfig("requestTopic", "replytome", "hallo")
                .setTimeout(60 * 1000)
                .setExpectedReplies(3);

        ScatterGatherConfig config2 = new ScatterGatherConfig("requestTopic2", "reply2", "hallo2")
                .setTimeout(60 * 1000)
                .setExpectedReplies(2);

        ScatterGatherRequest<String> request = scatterGatherRequestBuilder.create(Arrays.asList(config1, config2));
        List<String> result = request.execute();

        return new ResponseEntity<>(Arrays.toString(result.toArray()), HttpStatus.OK);
    }
}
