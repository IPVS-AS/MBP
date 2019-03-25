package org.citopt.connde.service.stats;

import org.citopt.connde.domain.component.Component;
import org.citopt.connde.domain.valueLog.ValueLog;
import org.citopt.connde.repository.ValueLogRepository;
import org.citopt.connde.service.UnitConverterService;
import org.citopt.connde.service.stats.model.ValueLogStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.measure.converter.UnitConverter;
import javax.measure.unit.Unit;
import java.util.List;

/**
 * Service that provides means for calculating descriptive statistical information from a set of value logs
 * of a certain component.
 *
 * @author Jan
 */
@Service
public class ValueLogStatsService {
    //Number of decimal places to spare from rounding
    private static final int ROUNDING_DECIMAL_PLACES = 2;

    @Autowired
    UnitConverterService unitConverterService;

    @Autowired
    private ValueLogRepository valueLogRepository;

    /**
     * Calculates statistical information from the value logs that are stored in the repository
     * for a certain component and returns them all as a ValueLogStats object. Additionally,
     * the returned values can be converted to a given unit.
     *
     * @param component The component whose value logs should be used to calculate the data
     * @param unit      The unit to which the values are supposed to be converted (null for default)
     * @return ValueLogStats object that holds the calculated data
     */
    public ValueLogStats calculateValueLogStats(Component component, Unit unit) {
        //Create empty stats object
        ValueLogStats stats = new ValueLogStats();

        //Retrieve a list of value logs from the repository
        String componentId = component.getId();
        List<ValueLog> valueLogList = valueLogRepository.findListByIdref(componentId);

        //Return object with default values if no logs are available
        if (valueLogList.isEmpty()) {
            return stats;
        }


        //Get adapter unit object
        Unit adapterUnit = component.getAdapter().getUnitObject();

        //Check if value conversion is desired and possible
        if ((unit != null) && adapterUnit.isCompatible(unit)) {
            //Get converter
            UnitConverter converter = adapterUnit.getConverterTo(unit);

            //Iterate over all value logs and convert them
            for (ValueLog valueLog : valueLogList) {
                //Convert
                unitConverterService.convertValueLogValue(valueLog, converter);
            }
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

        //Constant for rounding
        double roundingConst = Math.pow(10, ROUNDING_DECIMAL_PLACES);

        //Variables that hold the logs with the smallest/biggest known values
        ValueLog minLog = valueLogList.get(0);
        ValueLog maxLog = valueLogList.get(0);

        //Accumulator for calculating the average
        double averageAccumulator = 0;

        //Iterate over all value logs in the list
        for (ValueLog log : valueLogList) {
            //Get numeric value for the current log
            double logValue = Double.parseDouble(log.getValue());

            //Update min log if necessary
            if (logValue < Double.parseDouble(minLog.getValue())) {
                minLog = log;
            }

            //Update max log if necessary
            if (logValue > Double.parseDouble(maxLog.getValue())) {
                maxLog = log;
            }

            //Increase accumulator
            averageAccumulator += logValue;
        }

        //Finally set min and max log
        stats.setMinimumLog(minLog);
        stats.setMaximumLog(maxLog);

        //Calculate and set the average
        double average = averageAccumulator / ((double) valueLogList.size());
        stats.setAverage(Math.round(average * roundingConst) / roundingConst);

        /*
         Calculate the variance and standard deviation
        */

        //Accumulator for calculating the variance
        double varianceAccumulator = 0;

        //Iterate over all value logs in the list
        for (ValueLog log : valueLogList) {
            double logValue = Double.parseDouble(log.getValue());

            //Increase accumulator with respect to the value
            varianceAccumulator += Math.pow(logValue - average, 2);
        }

        //Calculate the variance
        double variance = varianceAccumulator / ((double) valueLogList.size());
        stats.setVariance(Math.round(variance * roundingConst) / roundingConst);

        //Derive the standard deviation from the variance
        double standardDeviation = Math.sqrt(variance);
        stats.setStandardDeviation(Math.round(standardDeviation * roundingConst) / roundingConst);

        //Return the final stats object
        return stats;
    }
}
