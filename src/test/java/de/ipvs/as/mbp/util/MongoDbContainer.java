package de.ipvs.as.mbp.util;

import org.testcontainers.containers.GenericContainer;

public class MongoDbContainer extends GenericContainer<MongoDbContainer> {

    private static MongoDbContainer instance = null;
    private static final String WIPE_COMMAND = "db.getCollectionNames().forEach( function(collection_name) { if (collection_name.indexOf(\"system.\") == -1) {db[collection_name].drop();} else{db[collection_name].remove({});}});";

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

    public void wipeMongoDB() throws Exception {
        if (this.isRunning()) {
            ExecResult execResult = this.execInContainer("/usr/bin/mongo", "--eval", String.format("'%s'", WIPE_COMMAND));
            System.out.println("Attempted Wipe on MongoDB. Exit Code: " + execResult.getExitCode() + " Stdout Output:");
            System.out.println(execResult.getStdout());
            System.out.println("Stderr Output:");
            System.out.println(execResult.getStderr());
        }
    }

    @Override
    public void start() {
        super.start();

        System.setProperty("MONGO_PORT", this.getMappedPort(27017).toString());
    }
}
