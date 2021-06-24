package de.ipvs.as.mbp.web.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import de.ipvs.as.mbp.RestConfiguration;
import de.ipvs.as.mbp.domain.discovery.messages.test.DiscoveryTestReply;
import de.ipvs.as.mbp.domain.discovery.messages.test.DiscoveryTestRequest;
import de.ipvs.as.mbp.service.messaging.PubSubService;
import de.ipvs.as.mbp.service.messaging.dispatcher.listener.DomainMessageListener;
import de.ipvs.as.mbp.service.messaging.message.DomainDocumentMessage;
import de.ipvs.as.mbp.service.messaging.message.reply.ReplyMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

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
        /*
        ScatterGatherRequestBuilder scatterGatherRequestBuilder = new ScatterGatherRequestBuilder(pubSubService);

        CompletableFuture<Set<String>> future = scatterGatherRequestBuilder.scatterGatherOld(new RequestTopic().setSuffix("testtopic").setExpectedReplies(2).setTimeout(10 * 1000),
                "replytome", "This is just a test!");

        Set<String> replies = future.get();*/

        DiscoveryTestRequest request = new DiscoveryTestRequest();

        pubSubService.publish("testtopic", new DomainDocumentMessage(request));

        pubSubService.subscribeDomain("test",
                new DomainMessageListener<>(
                        new TypeReference<ReplyMessage<DiscoveryTestReply>>() {
                        }, (message, topic, topicFilter) -> {
                    System.out.println("da!");
                }));

        return new ResponseEntity<String>("done", HttpStatus.OK);
    }
}
