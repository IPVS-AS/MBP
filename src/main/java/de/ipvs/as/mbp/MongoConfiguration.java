package de.ipvs.as.mbp;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import de.ipvs.as.mbp.constants.Constants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories
@PropertySource("classpath:application.properties")
public class MongoConfiguration extends AbstractMongoClientConfiguration {

    @Value("${spring.data.mongodb.host}")
    private String mongoHost;

    @Value("${spring.data.mongodb.port}")
    private String mongoPort;

    @Value("${spring.data.mongodb.database}")
    private String mongoDatabase;

    @Value("${spring.data.mongodb.username}")
    private String mongoUsername;

    @Value("${spring.data.mongodb.password}")
    private String mongoPassword;

    @Bean
    public MongoClient mongoClient() {
        //String for mongoDB credentials
        String credString = "";

        //CHeck whether credentials are available
        if ((!mongoUsername.isEmpty()) && (!mongoPassword.isEmpty())) {
            credString = String.format("%s:%s@", mongoUsername, mongoPassword);
        }

        //Put full connection string together
        String connString = String.format("mongodb://%s%s:%s", credString, mongoHost, mongoPort);

        //Create corresponding MongoClient
        return MongoClients.create(connString);
    }

    @Override
    protected String getDatabaseName() {
        return mongoDatabase;
    }

    @Override
    protected String getMappingBasePackage() {
        return Constants.ROOT_PACKAGE + ".repository";
    }

    /**
     * Returns the host name of the mongo configuration.
     *
     * @return The host name
     */
    public String getMongoHost() {
        return mongoHost;
    }

    /**
     * Returns the port of the mongo configuration.
     *
     * @return The port
     */
    public String getMongoPort() {
        return mongoPort;
    }

    /**
     * Returns the database name of the mongo configuration
     *
     * @return The database name
     */
    public String getMongoDatabase() {
        return mongoDatabase;
    }

    /**
     * Returns the username of the mongo configuration.
     *
     * @return The username
     */
    public String getMongoUsername() {
        return mongoUsername;
    }

    /**
     * Returns the password of the mongo configuration.
     *
     * @return The password
     */
    public String getMongoPassword() {
        return mongoPassword;
    }

    /**
     * Create a configurer for the injection of application-related property values, based on the
     * application.properties file.
     *
     * @return The resulting property configurer
     */
    @Bean
    public static PropertySourcesPlaceholderConfigurer applicationPropertiesConfigurer() {
        //Create corresponding property configurer and make it tolerant
        PropertySourcesPlaceholderConfigurer propsConfig = new PropertySourcesPlaceholderConfigurer();
        propsConfig.setIgnoreUnresolvablePlaceholders(true);

        //Return final configurer
        return propsConfig;
    }
}
