package de.ipvs.as.mbp.web.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import de.ipvs.as.mbp.RestConfiguration;
import de.ipvs.as.mbp.domain.discovery.messages.test.DiscoveryTestReply;
import de.ipvs.as.mbp.service.messaging.PubSubService;
import de.ipvs.as.mbp.service.messaging.message.DomainMessage;
import de.ipvs.as.mbp.service.messaging.message.DomainMessageBody;
import de.ipvs.as.mbp.service.messaging.message.types.ReplyMessage;
import de.ipvs.as.mbp.service.messaging.scatter_gather.RequestStageConfig;
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
        RequestStageConfig config1 = new RequestStageConfig("requestTopic", "replytome", "hallo")
                .setTimeout(60 * 1000)
                .setExpectedReplies(3);

        RequestStageConfig config2 = new RequestStageConfig("requestTopic2", "reply2", "hallo2")
                .setTimeout(60 * 1000)
                .setExpectedReplies(2);

        ScatterGatherRequest<? extends DomainMessage<? extends DomainMessageBody>> request = scatterGatherRequestBuilder.addRequestStage(config1)
                .addRequestStage(config2)
                .buildForDomain(new TypeReference<ReplyMessage<DiscoveryTestReply>>() {
                });

        List<? extends DomainMessage<? extends DomainMessageBody>> result = request.execute();

        return new ResponseEntity<>(Arrays.toString(result.toArray()), HttpStatus.OK);
    }
}
