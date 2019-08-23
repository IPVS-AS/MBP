package org.citopt.connde.domain.units;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;

/**
 * Objects of this class represent predefined units which can be used as unit suggestions by the user.
 *
 * @param <Q> The quantity type of the unit object
 * @author Jan
 */
@ApiModel(description = "A unit of a certain quantity defined by the application")
public class PredefinedUnit<Q extends Quantity> {

    //Name of the unit
    @ApiModelProperty(notes = "The name of the unit", example = "Meters per square second")
    private String name;

    //JScience unit that corresponds to this unit object
    @JsonIgnore
    private Unit<Q> unit;

    /**
     * Creates a new predefined unit object.
     *
     * @param name The name of the unit that is displayed to the user
     * @param unit The JScience unit which corresponds to the new object
     */
    public PredefinedUnit(String name, Unit<Q> unit) {
        this.name = name;
        this.unit = unit;
    }

    /**
     * Returns the name of the unit.
     *
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the unit.
     *
     * @param name The name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the JScience unit of the unit.
     *
     * @return The unit
     */
    public Unit<Q> getUnit() {
        return unit;
    }

    /**
     * Sets the JScience unit of the unit.
     *
     * @param unit The unit to set
     */
    public void setUnit(Unit<Q> unit) {
        this.unit = unit;
    }

    /**
     * Returns a symbol string that describes the unit in a mathematical way.
     *
     * @return The symbol string
     */
    @JsonProperty("format")
    @ApiModelProperty("Symbol string describing the unit mathematically")
    public String getSymbolString() {
        return this.unit.toString();
    }

    /**
     * Returns whether the unit is a standard unit for its physical quantity.
     *
     * @return True, if the unit is a standard unit; false otherwise
     */
    @JsonProperty("standardUnit")
    @ApiModelProperty("Whether the unit is a standard unit for its quantity")
    public boolean isStandardUnit() {
        return this.unit.isStandardUnit();
    }
}
