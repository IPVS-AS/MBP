package de.ipvs.as.mbp.util;

import org.testcontainers.containers.GenericContainer;

public class MongoDbContainer extends GenericContainer<MongoDbContainer> {

    private static MongoDbContainer instance = null;

    public MongoDbContainer() {
        super("mongo:latest");
        this.withExposedPorts(27017);
    }

    public static MongoDbContainer getInstance() {
        if (instance == null) {
            instance = new MongoDbContainer();
        }
        return instance;
    }

    @Override
    public void start() {
        super.start();

        System.setProperty("MONGO_PORT", this.getMappedPort(27017).toString());
    }
}
