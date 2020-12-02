package de.ipvs.as.mbp.service;

import de.ipvs.as.mbp.domain.valueLog.ValueLog;
import org.springframework.stereotype.Component;

import javax.measure.converter.UnitConverter;

/**
 * Service that provides means for converting values from one unit into another unit.
 *
 * @author Jan
 */
@Component
public class UnitConverterService {
    //Number of decimal places to spare from rounding
    private static final int ROUNDING_DECIMAL_PLACES = 4;

    /**
     * Converts the value of a value log into a different unit by using a given unit converter.
     * Rounding to a certain number of decimal places (as specified in ROUNDING_DECIMAL_PLACES)
     * is applied to the resulting value.
     *
     * @param valueLog  The value log whose value is supposed to be converted
     * @param converter The unit converter which converts the values of the value logs into the
     *                  desired target unit
     */
    public void convertValueLogValue(ValueLog valueLog, UnitConverter converter) {
        //Get value of current log
        double value = Double.valueOf(valueLog.getValue());

        //Convert value
        double convertedValue = converter.convert(value);

        //Determine rounding constant
        double roundingConst = Math.pow(10, ROUNDING_DECIMAL_PLACES);

        //Apply rounding
        convertedValue = Math.round(convertedValue * roundingConst) / roundingConst;

        //Write value to log
        valueLog.setValue(convertedValue);
    }
}
