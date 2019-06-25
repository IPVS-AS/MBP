package org.citopt.connde.repository;

import org.citopt.connde.InfluxDBConfiguration;
import org.citopt.connde.domain.valueLog.ValueLog;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.impl.InfluxDBMapper;
import org.influxdb.querybuilder.BuiltQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.influxdb.querybuilder.BuiltQuery.QueryBuilder.select;

/**
 * This component represents a repository for persisting and querying value logs, powered by a InfluxDB database.
 * <p>
 * //TODO Rename to "ValueLogRepository" after conversion
 */
@Component
public class ValueLogInfluxDBRepository {

    private static final String QUERY_FIND_BY_IDREF = "";
    private static final String QUERY_DELETE_ALL = "DROP SERIES FROM $measurement";
    private static final String QUERY_DELETE_BY_IDREF = "DROP SERIES FROM $measurement WHERE idref='$idref'";

    //InfluxDB bean to use
    private InfluxDB influxDB;

    //Derived mapper for mapping value log objects
    private InfluxDBMapper influxDBMapper;

    /**
     * Instantiates the repository by passing a reference to the InfluxDB database bean
     * that is supposed to be used (auto-wired).
     *
     * @param influxDB The InfluxDB database bean
     */
    @Autowired
    private ValueLogInfluxDBRepository(InfluxDB influxDB) {
        this.influxDB = influxDB;
        this.influxDBMapper = new InfluxDBMapper(influxDB);
    }

    /**
     * Writes a given value log object into the repository.
     *
     * @param valueLog The value log to write
     */
    public void write(ValueLog valueLog) {
        influxDBMapper.save(valueLog);
        /*
        //Convert value log to time series point
        Point point = convertValueLogToPoint(valueLog);

        //Insert point into InfluxDB
        influxDB.write(point);*/
    }

    public Page<ValueLog> findAllByIdRef(String idref, Pageable pageable) {
        if ((idref == null) || idref.isEmpty()) {
            throw new IllegalArgumentException("Idref must not be null or empty.");
        }

        //Build query
        //Query query = new Query("SELECT * FROM " + InfluxDBConfiguration.getQueryMeasurementReference() +
        //        " WHERE idref='" + idref + "' LIMIT " + pageable.getPageSize() + " OFFSET " + pageable.getOffset());

        Query query = select().all().from(InfluxDBConfiguration.DATABASE_NAME,
                InfluxDBConfiguration.getQueryMeasurementReference());
                //= BuiltQuery.QueryBuilder.select().all().from(InfluxDBConfiguration.DATABASE_NAME).buildQueryString().toString();

        //Execute query
        List<ValueLog> valueLogs = influxDBMapper.query(query, ValueLog.class);

        return new PageImpl<ValueLog>(valueLogs, pageable, valueLogs.size());
    }

    public void deleteByIdRef(String idref) {
        //TODO
    }

    public void deleteAll() {
        //TODO
    }

    /**
     * Converts a value log object into a InfluxDB database series point.
     *
     * @param valueLog The value log to convert
     * @return The resulting time series point
     */
    private Point convertValueLogToPoint(ValueLog valueLog) {
        //Sanity check
        if (valueLog == null) {
            throw new IllegalArgumentException("Value log must not be null.");
        }

        //Determine timestamp to use as time in the time series database
        long timestamp = valueLog.getTime().getEpochSecond();

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
