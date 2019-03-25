package org.citopt.connde.service;

import org.citopt.connde.domain.valueLog.ValueLog;
import org.springframework.stereotype.Component;

import javax.measure.converter.UnitConverter;

@Component
public class UnitConverterService {
    //Number of decimal places to spare from rounding
    private static final int ROUNDING_DECIMAL_PLACES = 4;

    public void convertValueLogValue(ValueLog valueLog, UnitConverter converter) {
        //Get value of current log
        double value = Double.valueOf(valueLog.getValue());

        //Convert value
        double convertedValue = converter.convert(value);

        //Determine rounding constant
        double roundingConst = Math.pow(10, ROUNDING_DECIMAL_PLACES);

        //Round
        convertedValue = Math.round(convertedValue * roundingConst) / roundingConst;

        //Write value to log
        valueLog.setValue(Double.toString(convertedValue));
    }
}
