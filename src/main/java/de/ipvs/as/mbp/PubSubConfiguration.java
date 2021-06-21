package de.ipvs.as.mbp;

import de.ipvs.as.mbp.service.messaging.PubSubClient;
import de.ipvs.as.mbp.service.messaging.impl.mqtt.MQTTClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration of a client that enables publish-subscribe-based messaging in cooperation with an external
 * messaging broker.
 */
@Configuration
public class PubSubConfiguration {

    /**
     * Creates a bean that represents a client for publish-subscribe-based messaging. This bean offers an uniform
     * and technology-agnostic interface for messaging methods, so that the middleware technology that is actually
     * used behind the scenes is hidden from MBP components that need to make use of messaging.
     *
     * @return The configured client for publish-subscribe-based messaging.
     */
    @Bean
    public PubSubClient pubSubClient() {
        //Use MQTT for publish-subscribe-based messaging
        return new MQTTClient();
    }
}
