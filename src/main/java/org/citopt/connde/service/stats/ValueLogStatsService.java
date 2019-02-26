package org.citopt.connde.service.stats;

import org.citopt.connde.domain.component.Component;
import org.citopt.connde.repository.ValueLogRepository;
import org.citopt.connde.repository.projection.ValueLogProjection;
import org.citopt.connde.service.stats.model.ValueLogStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service that provides means for calculating descriptive statistical information from a set of value logs
 * of a certain component.
 *
 * @author Jan
 */
@Service
public class ValueLogStatsService {

    @Autowired
    private ValueLogRepository valueLogRepository;

    /**
     * Calculates statistical information from the value logs that are stored in the repository
     * for a certain component and returns them all as a ValueLogStats object.
     *
     * @param component The component whose value logs should be used to calculate the data
     * @return ValueLogStats object that holds the calculated data
     */
    public ValueLogStats calculateValueLogStats(Component component) {
        //Create empty stats object
        ValueLogStats stats = new ValueLogStats();

        //Retrieve a list of value logs from the repository
        String componentId = component.getId();
        List<ValueLogProjection> valueLogList = valueLogRepository.findProjectionByIdref(componentId);

        //Return object with default values if no logs are available
        if (valueLogList.isEmpty()) {
            return stats;
        }

        /*
        Extract data that can be directly derived from the value log list
        */
        stats.setNumberLogs(valueLogList.size());
        stats.setFirstLog(valueLogList.get(0));
        stats.setLastLog(valueLogList.get(valueLogList.size() - 1));

        /*
        Find min log, max log and calculate average
        */

        //Variables that hold the logs with the smallest/biggest known values
        ValueLogProjection minLog = valueLogList.get(0);
        ValueLogProjection maxLog = valueLogList.get(0);

        //Accumulator for calculating the average
        float averageAccumulator = 0;

        //Iterate over all value logs in the list
        for (ValueLogProjection log : valueLogList) {
            //Get numeric value for the current log
            float logValue = Float.parseFloat(log.getValue());

            //Update min log if necessary
            if (logValue < Float.parseFloat(minLog.getValue())) {
                minLog = log;
            }

            //Update max log if necessary
            if (logValue > Float.parseFloat(maxLog.getValue())) {
                maxLog = log;
            }

            //Increase accumulator
            averageAccumulator += logValue;
        }

        //Finally set min and max log
        stats.setMinimumLog(minLog);
        stats.setMaximumLog(maxLog);

        //Calculate and set the average
        float average = averageAccumulator / ((float) valueLogList.size());
        stats.setAverage(average);

        /*
         Calculate the variance and standard deviation
        */

        //Accumulator for calculating the variance
        float varianceAccumulator = 0;

        //Iterate over all value logs in the list
        for (ValueLogProjection log : valueLogList) {
            float logValue = Float.parseFloat(log.getValue());

            //Increase accumulator with respect to the value
            varianceAccumulator += Math.pow(logValue - average, 2);
        }

        //Calculate the variance
        float variance = varianceAccumulator / ((float) valueLogList.size());
        stats.setVariance(variance);

        //Derive the standard deviation from the variance
        float standardDeviation = (float) Math.sqrt(variance);
        stats.setStandardDeviation(standardDeviation);

        //Return the final stats object
        return stats;
    }
}
