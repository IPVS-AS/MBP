package de.ipvs.as.mbp.service.discovery;

import de.ipvs.as.mbp.domain.discovery.messages.test.DiscoveryTestRequest;
import de.ipvs.as.mbp.domain.discovery.topic.RequestTopic;
import de.ipvs.as.mbp.service.messaging.PubSubService;
import de.ipvs.as.mbp.service.messaging.message.DomainMessageBody;
import de.ipvs.as.mbp.service.messaging.message.types.RequestMessage;
import de.ipvs.as.mbp.service.messaging.scatter_gather.RequestStageConfig;
import de.ipvs.as.mbp.service.messaging.scatter_gather.ScatterGatherRequestBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * This component provides various discovery-related service functions that may be offered to the user via the REST API.
 */
@Service
public class DiscoveryService {

    @Autowired
    private PubSubService pubSubService;

    /**
     * Creates and initializes the discovery service.
     */
    public DiscoveryService() {

    }


    /**
     * Tests the availability of gateways for a given {@link RequestTopic} and returns
     * a map (gateway ID --> device descriptions count) containing the identifiers of the gateways
     * that replied to the request and the number of device descriptions they contain.
     *
     * @param requestTopic The request topic for which the gateway availability is supposed to be tested
     * @return The resulting map (gateway ID --> device descriptions count) with information about the
     * gateways that replied to the request
     */
    public Map<String, Integer> testGatewayAvailability(RequestTopic requestTopic) {
        //Sanity check
        if (requestTopic == null) {
            throw new IllegalArgumentException("Request topic must not be null.");
        }

        //Create result map
        Map<String, Integer> resultMap = new HashMap<>();

        //Generate reply topic


        //Create request message
        RequestMessage<DiscoveryTestRequest> requestMessage = new RequestMessage<>(new DiscoveryTestRequest());

        //Create request stage config
        RequestStageConfig<RequestMessage<DiscoveryTestRequest>> requestStageConfig = new RequestStageConfig<>()

        //Create scatter gather request
        new ScatterGatherRequestBuilder(pubSubService).addRequestStage()
    }

    private <T extends DomainMessageBody> RequestStageConfig<RequestMessage<T>> createSGRequestStage(RequestTopic requestTopic){

    }
}
