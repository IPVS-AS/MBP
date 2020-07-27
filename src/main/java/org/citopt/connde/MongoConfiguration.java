package org.citopt.connde;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.citopt.connde.constants.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

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
    public MongoClient mongo() {
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
		BasicDBObject authorityDevice = new BasicDBObject();
		authorityDevice.put("_id", Constants.DEVICE);
		BasicDBObject authorityAnonymous = new BasicDBObject();
		authorityAnonymous.put("_id", Constants.ANONYMOUS);
		if(!database.collectionExists("authority")){
			DBCollection collection = database.createCollection("authority", null);
			collection.insert(authorityAdmin);
			collection.insert(authorityUser);
			collection.insert(authorityDevice);
			collection.insert(authorityAnonymous);
		}
		
		if (!database.collectionExists("user")){
			DBCollection collection = database.createCollection("user", null);
			List<BasicDBObject> documents = new ArrayList<>();

			Set<BasicDBObject> authorities = new HashSet<>();
			authorities.add(authorityAdmin);
			authorities.add(authorityUser);

			// An administration user
			BasicDBObject adminUser = new BasicDBObject();
			adminUser.put("_class", "org.citopt.connde.domain.user.User");
			adminUser.put("first_name", "Admin");
			adminUser.put("last_name", "Admin");
			adminUser.put("username", "admin");
			adminUser.put("password", passwordEncoder.encode("admin"));
			adminUser.put("authorities", authorities);

			documents.add(adminUser);

			// A user for the MBP platform to authenticate itself over http to retrieve an OAuth token
			BasicDBObject mbpUser = new BasicDBObject();
			mbpUser.put("_class", "org.citopt.connde.domain.user.User");
			mbpUser.put("first_name", "MBP");
			mbpUser.put("last_name", "Platform");
			mbpUser.put("username", "mbp");
			mbpUser.put("password", passwordEncoder.encode("mbp-platform"));
			mbpUser.put("authorities", authorities);

			documents.add(mbpUser);

			Set<BasicDBObject> deviceAuthorities = new HashSet<>();
			deviceAuthorities.add(authorityDevice);
			deviceAuthorities.add(authorityUser);

			// A user which is used by IoT devices for http authentication
			BasicDBObject deviceUser = new BasicDBObject();
			deviceUser.put("_class", "org.citopt.connde.domain.user.User");
			deviceUser.put("first_name", "Device");
			deviceUser.put("last_name", "Client");
			deviceUser.put("username", "device-client");
			deviceUser.put("password", passwordEncoder.encode("device"));
			deviceUser.put("authorities", deviceAuthorities);

			documents.add(deviceUser);
			collection.insert(documents);
		}
	}
	
}
