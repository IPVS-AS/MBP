package org.citopt.connde;


import org.influxdb.BatchOptions;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InfluxDBConfiguration {
    //General internal settings
    private static final String URL = "http://localhost:8086";
    public static final String DATABASE_NAME = "mbp";

    //Measurements name for value logs
    public static final String MEASUREMENT_NAME = "value_log";

    //Retention policy
    private static final String RETENTION_POLICY_NAME = "retentionPolicy";

    //Duration time
    private static final String DURATION_TIME = "365d";

    /**
     * Creates the InfluxDB bean.
     *
     * @return The bean
     */
    @Bean
    public InfluxDB influxDB() {
        //Connect to the InfluxDB
        InfluxDB influxDB = InfluxDBFactory.connect(URL);

        //Set database, create if it does not exist
        influxDB.query(new Query("CREATE DATABASE " + DATABASE_NAME));
        influxDB.setDatabase(DATABASE_NAME);

        //Create new retention policy
        influxDB.query(new Query("CREATE RETENTION POLICY " + RETENTION_POLICY_NAME + " ON " +
                DATABASE_NAME + " DURATION " + DURATION_TIME + " REPLICATION 1 DEFAULT"));
        influxDB.setRetentionPolicy(RETENTION_POLICY_NAME);

        //Enable batch processing
        influxDB.enableBatch(BatchOptions.DEFAULTS);

        return influxDB;
    }

    /**
     * Returns a string that might be used within queries to the InfluxDB in order to reference the measurements.
     *
     * @return A string containing the measurement reference
     */
    public static String getQueryMeasurementReference() {
        return "\"" + DATABASE_NAME + "\".autogen.\"" + MEASUREMENT_NAME + "\"";
    }
}