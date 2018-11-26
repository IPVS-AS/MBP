package org.citopt.connde;

import com.mongodb.MongoClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories
public class MongoConfiguration extends AbstractMongoConfiguration {
    
    public static String DB_NAME = "connde";
    
    private static MongoClient mongo;
    
    @Override
    protected String getDatabaseName() {
        return DB_NAME;
    }

    @Override
    public MongoClient mongo() throws Exception {
        if(mongo == null) {
            mongo = new MongoClient();
        }
        return mongo;
    }

    @Override
    protected String getMappingBasePackage() {
        return "org.citopt.connde.repository";
    }
}
