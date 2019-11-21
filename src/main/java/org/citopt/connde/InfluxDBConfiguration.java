package org.citopt.connde;


import okhttp3.OkHttpClient;
import org.influxdb.BatchOptions;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class InfluxDBConfiguration {
    //General internal settings
    private static final String URL = "http://localhost:8086";
    public static final String DATABASE_NAME = "mbp";

    //Measurements name for value logs
    public static final String MEASUREMENT_NAME = "value_log";

    //Retention policy
    public static final String RETENTION_POLICY_NAME = "retentionPolicy";

    //Duration time
    private static final String DURATION_TIME = "150d";

    /**
     * Creates the InfluxDB bean.
     *
     * @return The bean
     */
    @Bean
    public InfluxDB influxDB() {
        //Build HTTP client for InfluxDB
        OkHttpClient.Builder httpClient = new OkHttpClient().newBuilder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES)
                .writeTimeout(5, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true);

        //Connect to the InfluxDB
        InfluxDB influxDB = InfluxDBFactory.connect(URL, httpClient);

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
}