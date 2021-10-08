package de.ipvs.as.mbp.util;

import java.util.List;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.testcontainers.containers.GenericContainer;

public class MongoDbContainer extends GenericContainer<MongoDbContainer> {

    private static MongoDbContainer instance = null;
    private static final int MONGODB_STANDARD_PORT = 27017;
    private static final List<String> MONGODB_CLEANUP_BLACKLIST = List.of(new String[] {"admin", "config", "local"});

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
            MongoClient mongoClient = new MongoClient(this.getHost(), this.getMappedPort(MONGODB_STANDARD_PORT));

            for (String dbName : mongoClient.listDatabaseNames()) {
                MongoDatabase database = mongoClient.getDatabase(dbName);
                if (!MONGODB_CLEANUP_BLACKLIST.contains(dbName)) {
                    database.drop();
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
