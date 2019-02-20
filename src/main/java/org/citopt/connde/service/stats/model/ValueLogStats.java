package org.citopt.connde.service.stats.model;

import org.citopt.connde.domain.valueLog.ValueLog;
import org.citopt.connde.repository.projection.ValueLogProjection;

/**
 * @author Jan
 */
public class ValueLogStats {
    private int numberLogs = 0;

    private ValueLogProjection firstLog = null;
    private ValueLogProjection lastLog = null;
    private ValueLogProjection minimumLog = null;
    private ValueLogProjection maximumLog = null;

    private float average = 0;
    private float variance = 0;

    public ValueLogStats() {
    }

    public int getNumberLogs() {
        return numberLogs;
    }

    public void setNumberLogs(int numberLogs) {
        this.numberLogs = numberLogs;
    }

    public ValueLogProjection getFirstLog() {
        return firstLog;
    }

    public void setFirstLog(ValueLogProjection firstLog) {
        this.firstLog = firstLog;
    }

    public ValueLogProjection getLastLog() {
        return lastLog;
    }

    public void setLastLog(ValueLogProjection lastLog) {
        this.lastLog = lastLog;
    }

    public ValueLogProjection getMinimumLog() {
        return minimumLog;
    }

    public void setMinimumLog(ValueLogProjection minimumLog) {
        this.minimumLog = minimumLog;
    }

    public ValueLogProjection getMaximumLog() {
        return maximumLog;
    }

    public void setMaximumLog(ValueLogProjection maximumLog) {
        this.maximumLog = maximumLog;
    }

    public float getAverage() {
        return average;
    }

    public void setAverage(float average) {
        this.average = average;
    }

    public float getVariance() {
        return variance;
    }

    public void setVariance(float variance) {
        this.variance = variance;
    }
}
