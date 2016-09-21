package org.citopt.websensor;

import com.mongodb.Mongo;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({MongoConfiguration.class})
@ComponentScan({"org.citopt.websensor.service"})
public class RootConfiguration {
    
    @Bean(name="mqtt")
    public MqttClient mqttClient() throws MqttException {
        MqttClient mqttClient = new MqttClient("tcp://localhost:1883", "root-server");
        return mqttClient;
    }
    
    @Bean(name="mongo")
    public Mongo mongo() throws Exception {
        System.out.println("mongo bean");
        return new Mongo();
    }

}
