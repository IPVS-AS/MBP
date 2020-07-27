package org.citopt.connde;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bson.Document;
import org.citopt.connde.constants.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

@Configuration
@EnableMongoRepositories
public class MongoConfiguration extends AbstractMongoClientConfiguration {
	
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public static String DB_NAME = "connde";
    
    private static MongoClient mongoClient;
    
    @Override
    protected String getDatabaseName() {
        return DB_NAME;
    }

    @Override
    public MongoClient mongoClient() {
        if(mongoClient == null) {
            mongoClient = MongoClients.create();
        }
		addValuesInDatabase(mongoClient);
        return mongoClient;
    }

    @Override
    protected String getMappingBasePackage() {
        return "org.citopt.connde.repository";
    }
    
	private void addValuesInDatabase(MongoClient mongoClient) {
		MongoDatabase database = mongoClient.getDatabase(DB_NAME);

		// Add component types
		if (!collectionExists(database, "componentType")){
			database.createCollection("componentType", null);
			for (int i = 0; i < Constants.componentTypes.length; i++) {
				Document document = new Document();
				document.put("name", Constants.componentTypes[i][0]);
				document.put("component", Constants.componentTypes[i][1]);
				database.getCollection("componentType").insertOne(document);
			}	
		}
		
		// Add authorities
		Document authorityAdmin = new Document();
		authorityAdmin.put("_id", Constants.ADMIN);
		Document authorityUser = new Document();
		authorityUser.put("_id", Constants.USER);
		Document authorityDevice = new Document();
		authorityDevice.put("_id", Constants.DEVICE);
		Document authorityAnonymous = new Document();
		authorityAnonymous.put("_id", Constants.ANONYMOUS);
		if(!collectionExists(database, "authority")) {
			database.createCollection("authority", null);
			database.getCollection("componentType").insertOne(authorityAdmin);
			database.getCollection("componentType").insertOne(authorityUser);
			database.getCollection("componentType").insertOne(authorityDevice);
			database.getCollection("componentType").insertOne(authorityAnonymous);
		}
		
		if(!collectionExists(database, "user")) {
			database.createCollection("user", null);
			List<Document> documents = new ArrayList<>();
			Set<Document> authorities = new HashSet<>();
			authorities.add(authorityAdmin);
			authorities.add(authorityUser);

			// An administration user
			Document adminUser = new Document();
			adminUser.put("_class", "org.citopt.connde.domain.user.User");
			adminUser.put("first_name", "Admin");
			adminUser.put("last_name", "Admin");
			adminUser.put("username", "admin");
			adminUser.put("password", passwordEncoder.encode("admin"));
			adminUser.put("authorities", authorities);

			documents.add(adminUser);

			// A user for the MBP platform to authenticate itself over http to retrieve an OAuth token
			Document mbpUser = new Document();
			mbpUser.put("_class", "org.citopt.connde.domain.user.User");
			mbpUser.put("first_name", "MBP");
			mbpUser.put("last_name", "Platform");
			mbpUser.put("username", "mbp");
			mbpUser.put("password", passwordEncoder.encode("mbp-platform"));
			mbpUser.put("authorities", authorities);

			documents.add(mbpUser);

			Set<Document> deviceAuthorities = new HashSet<>();
			deviceAuthorities.add(authorityDevice);
			deviceAuthorities.add(authorityUser);

			// A user which is used by IoT devices for http authentication
			Document deviceUser = new Document();
			deviceUser.put("_class", "org.citopt.connde.domain.user.User");
			deviceUser.put("first_name", "Device");
			deviceUser.put("last_name", "Client");
			deviceUser.put("username", "device-client");
			deviceUser.put("password", passwordEncoder.encode("device"));
			deviceUser.put("authorities", deviceAuthorities);

			documents.add(deviceUser);
			database.getCollection("user").insertMany(documents);
		}
	}
	
	private boolean collectionExists(MongoDatabase database, String collectionName) {
	    for (final String name : database.listCollectionNames()) {
	        if (name.equalsIgnoreCase(collectionName)) {
	            return true;
	        }
	    }
	    return false;
	}
	
}
