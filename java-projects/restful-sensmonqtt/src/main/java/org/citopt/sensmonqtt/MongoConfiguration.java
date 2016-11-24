package org.citopt.sensmonqtt;

import com.mongodb.Mongo;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories
public class MongoConfiguration extends AbstractMongoConfiguration {
    
    public static String DB_NAME = "sensmonqtt";
    
    private static Mongo mongo;
    
    @Override
    protected String getDatabaseName() {
        return DB_NAME;
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
        return "org.citopt.sensmonqtt.repository";
    }
}
