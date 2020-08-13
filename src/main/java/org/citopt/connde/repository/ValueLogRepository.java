package org.citopt.connde.repository;

import org.citopt.connde.InfluxDBConfiguration;
import org.citopt.connde.domain.valueLog.ValueLog;
import org.influxdb.InfluxDB;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Query;
import org.influxdb.impl.InfluxDBMapper;
import org.influxdb.querybuilder.SelectQueryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.influxdb.querybuilder.BuiltQuery.QueryBuilder.*;

/**
 * This component represents a repository for persisting and querying value logs, powered by a InfluxDB database.
 */
@Component
public class ValueLogRepository {
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
    private ValueLogRepository(InfluxDB influxDB) {
        this.influxDB = influxDB;

        //Create object mapper from influxDB instance
        this.influxDBMapper = new InfluxDBMapper(influxDB);
    }

    /**
     * Writes a given value log object into the repository.
     *
     * @param valueLog The value log to write
     */
    public void write(ValueLog valueLog) {
        //Just save the value log
        influxDBMapper.save(valueLog);
    }

    /**
     * Finds and returns a list of value logs that match a certain id reference of a component.
     *
     * @param idref The idref to match
     * @return The requested list of value logs
     */
    public List<ValueLog> findAllByIdRef(String idref) {
        //Sanity check
        if ((idref == null) || idref.isEmpty()) {
            throw new IllegalArgumentException("Idref must not be null or empty.");
        }

        //Build query
        Query query = select().all().from(InfluxDBConfiguration.DATABASE_NAME, getMeasurementReference())
                .where("idref='" + idref + "'");

        //Execute query and get list of value logs
        List<ValueLog> valueLogs = influxDBMapper.query(query, ValueLog.class);

        return valueLogs;
    }

    /**
     * Finds and returns a page of value logs that match a certain id reference of a component.
     *
     * @param idref    The idref to match
     * @param pageable The pageable describing the desired page of value logs
     * @return The requested page of value logs
     */
    public Page<ValueLog> findAllByIdRef(String idref, Pageable pageable) {
        //Sanity check
        if ((idref == null) || idref.isEmpty()) {
            throw new IllegalArgumentException("Idref must not be null or empty.");
        }

        //Get limit and offset from pageable
        int limit = pageable.getPageSize();
        long offset = pageable.getOffset();

        //Build query
        SelectQueryImpl selectQuery = select().all().from(InfluxDBConfiguration.DATABASE_NAME, getMeasurementReference());

        //Get desired sort option from pageable
        Sort sort = pageable.getSort();

        //Iterate over all specified sort properties
        for (Sort.Order order : sort) {
            String property = order.getProperty();

            //Only sorting for time property is supported, thus ignore the other ones
            if (!property.equals("time")) {
                continue;
            }

            //Extend query for ordering with the chosen direction
            if (order.isAscending()) {
                selectQuery = selectQuery.orderBy(asc());
            } else {
                selectQuery = selectQuery.orderBy(desc());
            }

            //Only ordering for time is supported, so no need to consider other properties
            break;
        }

        //Add limit and offset if meaningful
        if ((offset > 0) && (limit > 0)) {
            selectQuery = selectQuery.limit(limit, offset);
        } else if (limit > 0) {
            selectQuery = selectQuery.limit(limit);
        }

        //Add where clause in order to filter for idref
        Query query = selectQuery.where("idref='" + idref + "'");

        //Execute query
        List<ValueLog> valueLogs = influxDBMapper.query(query, ValueLog.class);

        //Return value logs as page
        return new PageImpl<>(valueLogs, pageable, valueLogs.size());
    }

    public void deleteByIdRef(String idref) {
        //TODO Does not work
        //Sanity check
        if ((idref == null) || idref.isEmpty()) {
            throw new IllegalArgumentException("Idref must not be null or empty.");
        }

        //Create query
        Query query = new Query("DELETE FROM " +
                InfluxDBConfiguration.MEASUREMENT_NAME + " WHERE idref='" + idref + "'",
                InfluxDBConfiguration.DATABASE_NAME);

        //Query query = new Query("DROP SERIES FROM \"value_log\" WHERE idref='5c97dc2583aeb6078c5ab672'");
        influxDB.query(query);
    }

    /**
     * Returns a string that might be used for referencing measurements within queries to the InfluxDB database. It is
     * a fully qualified name consisting out of the database name, the retention policy name and the measurements name.
     *
     * @return A string containing the measurement reference
     */
    private static String getMeasurementReference() {
        return "\"" + InfluxDBConfiguration.DATABASE_NAME + "\".\"" +
                InfluxDBConfiguration.RETENTION_POLICY_NAME + "\".\"" +
                InfluxDBConfiguration.MEASUREMENT_NAME + "\"";
    }
}
