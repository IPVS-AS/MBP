package org.citopt.websensor;

import com.mongodb.Mongo;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories
public class MongoConfiguration extends AbstractMongoConfiguration {
    
    private static Mongo mongo;
    
    @Override
    protected String getDatabaseName() {
        return "websensor";
    }

    @Override
    public Mongo mongo() throws Exception {
        if(mongo == null) {
            mongo = new Mongo();
        }
        return mongo;
    }

    @Override
    protected String getMappingBasePackage() {
        return "org.citopt.websensor.repository";
    }
}
