package org.citopt.websensor;

import com.mongodb.Mongo;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories
public class MongoTestConfiguration extends AbstractMongoConfiguration {

    @Override
    protected String getDatabaseName() {
        return "websensor-test";
    }

    @Override
    public Mongo mongo() throws Exception {
        return new Mongo();
    }

    @Override
    protected String getMappingBasePackage() {
        return "org.citopt.websensor.repository";
    }
}
