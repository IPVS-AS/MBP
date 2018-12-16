package org.citopt.connde;

import com.mongodb.MongoClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.net.UnknownHostException;

/**
 * @author rafaelkperes, Jan
 */
@Configuration
@Import({MongoConfiguration.class})
public class RootConfiguration {

    @Bean(name = "mongo")
    public MongoClient mongo() throws UnknownHostException {
        System.out.println("load Mongo");
        return new MongoClient();
    }
}
