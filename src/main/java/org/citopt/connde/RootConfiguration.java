package org.citopt.connde;

import java.net.UnknownHostException;

import com.mongodb.MongoClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
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
    public MqttClient mqttClient()  {
        System.out.println("load MqttClient");
        // CHANGE TO DYNAMIC IP
        MqttClient mqttClient = null;
        MemoryPersistence persistence = new MemoryPersistence();

		try {
			mqttClient = new MqttClient("tcp://localhost:1883", "root-server", persistence);
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("reason " + e.getReasonCode());
		}
        return mqttClient;
    }
    
    @Bean(name = "mongo")
    public MongoClient mongo() throws UnknownHostException {
        System.out.println("load Mongo");
        return new MongoClient();
    }

}
