package de.ipvs.as.mbp.util;

import java.util.Arrays;
import java.util.List;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.testcontainers.containers.GenericContainer;

public class MongoDbContainer extends GenericContainer<MongoDbContainer> {

    private static MongoDbContainer instance = null;
    private static final int MONGODB_STANDARD_PORT = 27017;
    private static final List<String> MONGODB_CLEANUP_BLACKLIST = Arrays.asList("admin", "config", "local");

    public MongoDbContainer() {
        super("mongo:latest");
        this.withExposedPorts(MONGODB_STANDARD_PORT);
    }

    public static MongoDbContainer getInstance() {
        if (instance == null) {
            instance = new MongoDbContainer();
        }
        return instance;
    }

    /**
     * This is needed after each test, because @DirtiesContext does not function reliably,
     * therefore we connect to the mongoDB via the MongoClient and manually wipe all databases
     * which are not protected from deletion
     */
    public void wipeMongoDB() {
        if (this.isRunning()) {
            MongoClient mongoClient = MongoClients.create(String.format("mongodb://%s:%d", getHost(), getFirstMappedPort()));

            for (String dbName : mongoClient.listDatabaseNames()) {
                if (!MONGODB_CLEANUP_BLACKLIST.contains(dbName)) {
                    mongoClient.getDatabase(dbName).drop();
                }
            }
        }
    }

    @Override
    public void start() {
        super.start();

        System.setProperty("MONGO_PORT", this.getMappedPort(MONGODB_STANDARD_PORT).toString());
    }
}
