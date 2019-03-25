package org.citopt.connde.domain.units;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jscience.physics.amount.Amount;

import javax.measure.unit.Unit;

public class UnitConversion {
    private String startUnit;
    private String targetUnit;
    private boolean convertible;
    private double conversionFactor;
    private double conversionOffset;

    public UnitConversion(Unit startUnit, Unit targetUnit) {
        this.startUnit = startUnit.toString();
        this.targetUnit = targetUnit.toString();

        this.convertible = startUnit.isCompatible(targetUnit);

        if (!this.convertible) {
            this.conversionFactor = 0.0;
            return;
        }

        //Get offset by transforming a value of 0.0
        Amount offsetTransformation = Amount.valueOf(0.0, startUnit).to(targetUnit);
        this.conversionOffset = offsetTransformation.getEstimatedValue();

        //Get conversion factor by transforming a value of 1.0
        Amount factorTransformation = Amount.valueOf(1.0, startUnit).to(targetUnit).minus(offsetTransformation);
        this.conversionFactor = factorTransformation.getEstimatedValue();
    }

    public String getStartUnit() {
        return startUnit;
    }

    public String getTargetUnit() {
        return targetUnit;
    }

    public boolean isConvertible() {
        return convertible;
    }

    public double getConversionFactor() {
        return conversionFactor;
    }

    public double getConversionOffset() {
        return conversionOffset;
    }
}
