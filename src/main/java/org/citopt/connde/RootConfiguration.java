package org.citopt.connde;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.mongodb.MongoClient;

/**
 * Root configuration for the MBP app.
 */
@Configuration
@Import({MongoConfiguration.class})
public class RootConfiguration {
    /**
     * Creates a MongoDB client as bean.
     *
     * @return The bean
     */
    @Bean(name = "mongo")
    public MongoClient mongo() {
        System.out.println("load Mongo");
        return new MongoClient();
    }
}
