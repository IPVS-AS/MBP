package org.citopt.connde.domain.units;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.measure.unit.Unit;

public class PredefinedUnit<Q extends javax.measure.quantity.Quantity> {

    private String name;

    @JsonIgnore
    private Unit<Q> unit;

    public PredefinedUnit(String name, Unit<Q> unit) {
        this.name = name;
        this.unit = unit;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Unit<Q> getUnit() {
        return unit;
    }

    public void setUnit(Unit<Q> unit) {
        this.unit = unit;
    }

    @JsonProperty("format")
    public String getSymbolString() {
        return this.unit.toString();
    }

    @JsonProperty("standardUnit")
    public boolean isStandardUnit() {
        return this.unit.isStandardUnit();
    }
}
