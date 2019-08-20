package org.citopt.connde;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

import java.util.HashSet;
import java.util.Set;

import org.citopt.connde.constants.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableMongoRepositories
public class MongoConfiguration extends AbstractMongoConfiguration {
	
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public static String DB_NAME = "connde";
    
    private static MongoClient mongoClient;
    
    @Override
    protected String getDatabaseName() {
        return DB_NAME;
    }

    @Override
    public MongoClient mongo() throws Exception {
        if(mongoClient == null) {
            mongoClient = new MongoClient();
        }
		addValuesInDatabase(mongoClient);
        return mongoClient;
    }

    @Override
    protected String getMappingBasePackage() {
        return "org.citopt.connde.repository";
    }
    
	private void addValuesInDatabase(MongoClient mongoClient) {
		DB database = mongoClient.getDB(DB_NAME);

		// Add component types
		if (!database.collectionExists("componentType")){
			DBCollection collection = database.createCollection("componentType", null);
			for (int i = 0; i < Constants.componentTypes.length; i++) {
				BasicDBObject document = new BasicDBObject();
				document.put("name", Constants.componentTypes[i][0]);
				document.put("component", Constants.componentTypes[i][1]);
				collection.insert(document);
			}	
		}
		
		// Add authorities
		BasicDBObject authorityAdmin = new BasicDBObject();
		authorityAdmin.put("_id", Constants.ADMIN);
		BasicDBObject authorityUser = new BasicDBObject();
		authorityUser.put("_id", Constants.USER);
		BasicDBObject authorityAnonymous = new BasicDBObject();
		authorityAnonymous.put("_id", Constants.ANONYMOUS);
		if(!database.collectionExists("authority")){
			DBCollection collection = database.createCollection("authority", null);
			collection.insert(authorityAdmin);
			collection.insert(authorityUser);
			collection.insert(authorityAnonymous);
		}
		
		// Add admin user
		if (!database.collectionExists("user")){
			DBCollection collection = database.createCollection("user", null);	

			Set<BasicDBObject> authorities = new HashSet<>();
			authorities.add(authorityAdmin);
			authorities.add(authorityUser);
			
			BasicDBObject adminUser = new BasicDBObject();
			adminUser.put("_class", "org.citopt.connde.domain.user.User");
			adminUser.put("first_name", "Admin");
			adminUser.put("last_name", "Admin");
			adminUser.put("username", "admin");
			adminUser.put("password", passwordEncoder.encode("admin"));
			adminUser.put("authorities", authorities);
			
			collection.insert(adminUser);
		}
	}
}
