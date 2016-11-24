package org.citopt.sensmonqtt;

import com.mongodb.Mongo;
import java.net.UnknownHostException;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 *
 * @author rafaelkperes
 */
@Configuration
@Import({MongoConfiguration.class})
public class RootConfiguration {

    @Bean(name = "mqtt")
    public MqttClient mqttClient() throws MqttException {
        System.out.println("load MqttClient");
        // CHANGE TO DYNAMIC IP
        MqttClient mqttClient = 
                new MqttClient("tcp://localhost:1883", "root-server");
        return mqttClient;
    }
    
    @Bean(name = "mongo")
    public Mongo mongo() throws UnknownHostException {
        System.out.println("load Mongo");
        return new Mongo();
    }

}
