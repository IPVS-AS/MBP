package org.citopt.connde.repository;

import org.citopt.connde.InfluxDBConfiguration;
import org.citopt.connde.domain.valueLog.ValueLog;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * This component represents a repository for persisting and querying value logs, powered by a InfluxDB database.
 * <p>
 * //TODO Rename to "ValueLogRepository" after conversion
 */
@Component
public class ValueLogInfluxDBRepository {

    //InfluxDB bean to use
    private InfluxDB influxDB;

    /**
     * Instantiates the repository by passing a reference to the InfluxDB database bean
     * that is supposed to be used (auto-wired).
     *
     * @param influxDB The InfluxDB database bean
     */
    @Autowired
    private ValueLogInfluxDBRepository(InfluxDB influxDB) {
        this.influxDB = influxDB;
    }

    /**
     * Writes a given value log object into the repository.
     *
     * @param valueLog The value log to write
     */
    public void write(ValueLog valueLog) {
        //Convert value log to time series point
        Point point = convertToTimeSeriesPoint(valueLog);

        //Insert point into InfluxDB
        influxDB.write(point);
    }

    /**
     * Converts a value log object into a InfluxDB database series point.
     *
     * @param valueLog The value log to convert
     * @return The resulting time series point
     */
    private Point convertToTimeSeriesPoint(ValueLog valueLog) {
        //Sanity check
        if (valueLog == null) {
            throw new IllegalArgumentException("Value log must not be null.");
        }

        //Determine timestamp to use as time in the time series database
        long timestamp;
        try {
            //Try to extract timestamp from value log
            timestamp = valueLog.getDateObject().getTime();
        } catch (ParseException e) {
            //Extraction failed, use current timestamp (better than nothing)
            timestamp = new Date().getTime();
        }

        //Create InfluxDB point object from value log by using tags (indexed) and fields (not indexed)
        return Point.measurement(InfluxDBConfiguration.MEASUREMENT_NAME)
                .time(timestamp, TimeUnit.MILLISECONDS)
                .tag("id", valueLog.getId())
                .tag("idref", valueLog.getIdref())
                .tag("component", valueLog.getComponent())
                .addField("qos", valueLog.getQos())
                .addField("topic", valueLog.getTopic())
                .addField("message", valueLog.getMessage())
                .addField("value", valueLog.getValue())
                .build();
    }
}
