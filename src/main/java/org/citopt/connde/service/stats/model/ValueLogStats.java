package org.citopt.connde.service.stats.model;

import org.citopt.connde.domain.valueLog.ValueLog;

/**
 * Objects of this class are models that hold basic descriptive statistical information for a set of value logs
 * of a certain component. They can be used as DTOs in order to retrieve the stats to the client.
 *
 * @author Jan
 */
public class ValueLogStats {
    //Number of logs
    private int numberLogs = 0;

    //Remarkable logs
    private ValueLog firstLog = null;
    private ValueLog lastLog = null;
    private ValueLog minimumLog = null;
    private ValueLog maximumLog = null;

    //Descriptive statistics
    private double average = 0;
    private double variance = 0;
    private double standardDeviation = 0;

    /**
     * Creates a new and empty stats object.
     */
    public ValueLogStats() {
    }

    /**
     * Returns the total number of logs considered in this stats object.
     *
     * @return The number of logs
     */
    public int getNumberLogs() {
        return numberLogs;
    }

    /**
     * Sets the number of logs considered in this stats object.
     *
     * @param numberLogs The number of logs
     */
    public void setNumberLogs(int numberLogs) {
        this.numberLogs = numberLogs;
    }

    /**
     * Returns the very first/the oldest available log.
     *
     * @return The first log
     */
    public ValueLog getFirstLog() {
        return firstLog;
    }

    /**
     * Sets the very first/the oldest available log.
     *
     * @param firstLog The first log
     */
    public void setFirstLog(ValueLog firstLog) {
        this.firstLog = firstLog;
    }

    /**
     * Returns the last/most recent available log.
     *
     * @return The last log
     */
    public ValueLog getLastLog() {
        return lastLog;
    }

    /**
     * Sets the very first/most recent available log.
     *
     * @param lastLog The last log
     */
    public void setLastLog(ValueLog lastLog) {
        this.lastLog = lastLog;
    }

    /**
     * Returns the log with the smallest available data value.
     *
     * @return The log with the smallest value
     */
    public ValueLog getMinimumLog() {
        return minimumLog;
    }

    /**
     * Sets the log with the smallest available data value.
     *
     * @param minimumLog The log with the smallest value
     */
    public void setMinimumLog(ValueLog minimumLog) {
        this.minimumLog = minimumLog;
    }

    /**
     * Returns the log with the biggest available data value.
     *
     * @return The log with the biggest value
     */
    public ValueLog getMaximumLog() {
        return maximumLog;
    }

    /**
     * Sets the log with the biggest available data value.
     *
     * @param maximumLog The log with the biggest value
     */
    public void setMaximumLog(ValueLog maximumLog) {
        this.maximumLog = maximumLog;
    }

    /**
     * Returns the average value of all logs.
     *
     * @return The average
     */
    public double getAverage() {
        return average;
    }

    /**
     * Sets the average value of all logs.
     *
     * @param average The average
     */
    public void setAverage(double average) {
        this.average = average;
    }

    /**
     * Returns the variance of all logs.
     *
     * @return The variance
     */
    public double getVariance() {
        return variance;
    }

    /**
     * Sets the variance of all logs
     *
     * @param variance The variance
     */
    public void setVariance(double variance) {
        this.variance = variance;
    }

    /**
     * Returns the standard deviation of all logs.
     *
     * @return The standard deviation
     */
    public double getStandardDeviation() {
        return standardDeviation;
    }

    /**
     * Sets the standard deviation of all logs
     *
     * @param standardDeviation The standard deviation
     */
    public void setStandardDeviation(double standardDeviation) {
        this.standardDeviation = standardDeviation;
    }
}
