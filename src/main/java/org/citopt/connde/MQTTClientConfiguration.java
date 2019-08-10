package org.citopt.connde;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MQTTClientConfiguration {

    //URL frame of the broker to use (protocol and port, address will be filled in)
    private static final String BROKER_URL = "tcp://%s:1883";
    //Client id that is supposed to be assigned to the client instance
    private static final String CLIENT_ID = "root-server";

    /**
     *
     * @return
     */
    @Bean(name = "MQTTClient")
    public MqttClient mqttClient() {

    }
}
