package org.citopt.connde.domain.units;

import static javax.measure.unit.SI.AMPERE;
import static javax.measure.unit.SI.BECQUEREL;
import static javax.measure.unit.SI.CANDELA;
import static javax.measure.unit.SI.CELSIUS;
import static javax.measure.unit.SI.CENTI;
import static javax.measure.unit.SI.COULOMB;
import static javax.measure.unit.SI.CUBIC_METRE;
import static javax.measure.unit.SI.DECI;
import static javax.measure.unit.SI.FARAD;
import static javax.measure.unit.SI.GIGA;
import static javax.measure.unit.SI.GRAM;
import static javax.measure.unit.SI.GRAY;
import static javax.measure.unit.SI.HENRY;
import static javax.measure.unit.SI.HERTZ;
import static javax.measure.unit.SI.JOULE;
import static javax.measure.unit.SI.KELVIN;
import static javax.measure.unit.SI.KILO;
import static javax.measure.unit.SI.KILOGRAM;
import static javax.measure.unit.SI.LUMEN;
import static javax.measure.unit.SI.LUX;
import static javax.measure.unit.SI.MEGA;
import static javax.measure.unit.SI.METER;
import static javax.measure.unit.SI.METERS_PER_SECOND;
import static javax.measure.unit.SI.MICRO;
import static javax.measure.unit.SI.MILLI;
import static javax.measure.unit.SI.NEWTON;
import static javax.measure.unit.SI.OHM;
import static javax.measure.unit.SI.PASCAL;
import static javax.measure.unit.SI.SECOND;
import static javax.measure.unit.SI.SIEMENS;
import static javax.measure.unit.SI.SIEVERT;
import static javax.measure.unit.SI.TESLA;
import static javax.measure.unit.SI.VOLT;
import static javax.measure.unit.SI.WATT;
import static javax.measure.unit.SI.WEBER;

import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.AngularAcceleration;
import javax.measure.quantity.AngularVelocity;
import javax.measure.quantity.DataRate;
import javax.measure.quantity.MassFlowRate;
import javax.measure.quantity.Quantity;
import javax.measure.quantity.Torque;
import javax.measure.quantity.VolumetricDensity;
import javax.measure.quantity.VolumetricFlowRate;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Enumeration of objects that represent predefined quantities holding predefined units. They may
 * be used as unit suggestions by the user.
 *
 * @author Jan
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@ApiModel(description = "A physical quantity predefined by the application")
public enum PredefinedQuantity {

    //No dimension
    DIMENSIONLESS("Dimensionless", new PredefinedUnit<?>[]{
            new PredefinedUnit<>("No Unit", Unit.ONE),
            new PredefinedUnit<>("Percent", NonSI.PERCENT),
            new PredefinedUnit<>("Decibel", NonSI.DECIBEL)
    }),

    ACCELERATION("Acceleration", new PredefinedUnit<?>[]{
            new PredefinedUnit<>("Meters per square second", SI.METERS_PER_SQUARE_SECOND)
    }),

    ANGLE("Angle", new PredefinedUnit<?>[]{
            new PredefinedUnit<>("Radian", SI.RADIAN), new PredefinedUnit<>("Degree", NonSI.DEGREE_ANGLE)
    }),

    ANGULAR_ACCELERATION("Angular Acceleration", new PredefinedUnit<?>[]{
            new PredefinedUnit<>("Radian per square second", AngularAcceleration.UNIT),
            new PredefinedUnit<>("Degree per square second", NonSI.DEGREE_ANGLE.divide(SI.SECOND.pow(2)))
    }),

    ANGULAR_VELOCITY("Angular Velocity", new PredefinedUnit<?>[]{
            new PredefinedUnit<>("Radian per second", AngularVelocity.UNIT),
            new PredefinedUnit<>("Degree per second", NonSI.DEGREE_ANGLE.divide(SI.SECOND))
    }),

    AREA("Area", new PredefinedUnit<?>[]{
            new PredefinedUnit<>("Square millimeters", MILLI(SI.METER).pow(2)),
            new PredefinedUnit<>("Square meters", SI.SQUARE_METRE),
            new PredefinedUnit<>("Hectare", NonSI.HECTARE),
            new PredefinedUnit<>("Are", NonSI.ARE)
    }),

    CATALYTIC_ACTIVITY("Catalytic activity", new PredefinedUnit<?>[]{
            new PredefinedUnit<>("Katal", SI.KATAL)
    }),

    DATA_AMOUNT("Data amount", new PredefinedUnit<?>[]{
            new PredefinedUnit<>("Bit", SI.BIT),
            new PredefinedUnit<>("Byte", NonSI.BYTE),
            new PredefinedUnit<>("Kilobyte", KILO(NonSI.BYTE)),
            new PredefinedUnit<>("Megabyte", MEGA(NonSI.BYTE)),
            new PredefinedUnit<>("Gigabyte", GIGA(NonSI.BYTE))
    }),

    DATA_RATE("Data rate", new PredefinedUnit<?>[]{
            new PredefinedUnit<>("Bit per second", DataRate.UNIT),
            new PredefinedUnit<>("Byte per second", NonSI.BYTE.divide(SI.SECOND))
    }),

    DURATION("Duration", new PredefinedUnit<?>[]{
            new PredefinedUnit<>("Second", SECOND),
            new PredefinedUnit<>("Minute", NonSI.MINUTE),
            new PredefinedUnit<>("Hour", NonSI.HOUR),
            new PredefinedUnit<>("Day", NonSI.DAY),
            new PredefinedUnit<>("Month", NonSI.MONTH),
            new PredefinedUnit<>("Year", NonSI.YEAR)
    }),

    ELECTRIC_CAPACITANCE("Electric capacitance", new PredefinedUnit<?>[]{
            new PredefinedUnit<>("Farad", FARAD)
    }),

    ELECTRIC_CHARGE("Electric charge", new PredefinedUnit<?>[]{
            new PredefinedUnit<>("Coulomb", COULOMB)
    }),

    ELECTRIC_CONDUCTANCE("Electric conductance", new PredefinedUnit<?>[]{
            new PredefinedUnit<>("Siemens", SIEMENS)
    }),

    ELECTRIC_CURRENT("Electric current", new PredefinedUnit<?>[]{
            new PredefinedUnit<>("Ampere", AMPERE)
    }),

    ELECTRIC_INDUCTANCE("Electric inductance", new PredefinedUnit<?>[]{
            new PredefinedUnit<>("Henry", HENRY)
    }),

    ELECTRIC_POTENTIAL("Electric potential", new PredefinedUnit<?>[]{
            new PredefinedUnit<>("Volt", VOLT)
    }),

    ELECTRIC_RESISTANCE("Electric resistance", new PredefinedUnit<?>[]{
            new PredefinedUnit<>("Ohm", OHM),
            new PredefinedUnit<>("Kilo Ohm", KILO(OHM)),
            new PredefinedUnit<>("Mega Ohm", MEGA(OHM))
    }),

    ENERGY("Energy", new PredefinedUnit<?>[]{
            new PredefinedUnit<>("Joule", JOULE)
    }),

    FORCE("Force", new PredefinedUnit<?>[]{
            new PredefinedUnit<>("Newton", NEWTON)
    }),

    FREQUENCY("Frequency", new PredefinedUnit<?>[] {
            new PredefinedUnit<>("Hertz", HERTZ)
    }),

    ILLUMINANCE("Illuminance", new PredefinedUnit<?>[]{
            new PredefinedUnit<>("Lux", LUX)
    }),

    LENGTH("Length", new PredefinedUnit<?>[]{
            new PredefinedUnit<>("Micrometer", MICRO(METER)),
            new PredefinedUnit<>("Millimeter", MILLI(METER)),
            new PredefinedUnit<>("Centimeter", CENTI(METER)),
            new PredefinedUnit<>("Decimeter", DECI(METER)),
            new PredefinedUnit<>("Meter", METER),
            new PredefinedUnit<>("Kilometer", KILO(METER)),
            new PredefinedUnit<>("Yard", NonSI.YARD),
            new PredefinedUnit<>("Mile", NonSI.MILE),
            new PredefinedUnit<>("Light year", NonSI.LIGHT_YEAR)
    }),

    LUMINOUS_FLUX("Luminous Flux", new PredefinedUnit<?>[]{
            new PredefinedUnit<>("Lumen", LUMEN)
    }),

    LUMINOUS_INTENSITY("Luminous Intensity", new PredefinedUnit<?>[]{
            new PredefinedUnit<>("Candela", CANDELA)
    }),

    MAGNETIC_FLUX("Magnetic Flux", new PredefinedUnit<?>[]{
            new PredefinedUnit<>("Weber", WEBER)
    }),

    MAGNETIC_FLUX_DENSITY("Magnetic Flux Density", new PredefinedUnit<?>[]{
            new PredefinedUnit<>("Tesla", TESLA)
    }),

    MASS("Mass", new PredefinedUnit<?>[]{
            new PredefinedUnit<>("Milligram", MILLI(GRAM)),
            new PredefinedUnit<>("Gram", GRAM),
            new PredefinedUnit<>("Kilogram", KILOGRAM),
            new PredefinedUnit<>("Pound", NonSI.POUND),
            new PredefinedUnit<>("Ton", NonSI.METRIC_TON)
    }),

    MASS_FLOW_RATE("Mass Flow Rate", new PredefinedUnit<?>[]{
            new PredefinedUnit<>("Kilogram per second", MassFlowRate.UNIT)
    }),

    POWER("Power", new PredefinedUnit<?>[]{
            new PredefinedUnit<>("Watt", WATT)
    }),

    PRESSURE("Pressure", new PredefinedUnit<?>[]{
            new PredefinedUnit<>("Pascal", PASCAL),
            new PredefinedUnit<>("Bar", NonSI.BAR)
    }),

    RADIATION_DOSE_ABSORBED("Radiation Dose Absorbed", new PredefinedUnit<?>[]{
            new PredefinedUnit<>("Gray", GRAY)
    }),

    RADIATION_DOSE_EFFECTIVE("Radiation Dose Effective", new PredefinedUnit<?>[]{
            new PredefinedUnit<>("Sievert", SIEVERT)
    }),

    RADIOACTIVE_ACTIVITY("Radioactive Activity", new PredefinedUnit<?>[]{
            new PredefinedUnit<>("Becquerel", BECQUEREL)
    }),

    TEMPERATURE("Temperature", new PredefinedUnit<?>[]{
            new PredefinedUnit<>("Kelvin", KELVIN),
            new PredefinedUnit<>("Degree Celsius", CELSIUS),
            new PredefinedUnit<>("Fahrenheit", NonSI.FAHRENHEIT)
    }),

    TORQUE("Torque", new PredefinedUnit<?>[]{
            new PredefinedUnit<>("Newton meter", Torque.UNIT)
    }),

    VELOCITY("Velocity", new PredefinedUnit<?>[]{
            new PredefinedUnit<>("Meter per second", METERS_PER_SECOND),
            new PredefinedUnit<>("Kilometer per second", NonSI.KILOMETERS_PER_HOUR),
    }),

    VOLUME("Volume", new PredefinedUnit<?>[]{
            new PredefinedUnit<>("Cubic meter", CUBIC_METRE),
            new PredefinedUnit<>("Liter", NonSI.LITER),
            new PredefinedUnit<>("Milliliter", MILLI(NonSI.LITER))
    }),

    VOLUMETRIC_DENSITY("Volumetric Density", new PredefinedUnit<?>[]{
            new PredefinedUnit<>("Kilogram per cubic meter", VolumetricDensity.UNIT)
    }),

    VOLUMETRIC_FLOW_RATE("Volumetric Flow Rate", new PredefinedUnit<?>[]{
            new PredefinedUnit<>("Cubic meter per second", VolumetricFlowRate.UNIT)
    });

    //Quantity name
    @ApiModelProperty(notes = "The name of the quantity", example = "Acceleration")
    private String name;

    //List of units for the quantity
    @ApiModelProperty(notes = "List of units that are available for the quantity")
    private PredefinedUnit<? extends Quantity>[] units;

    /**
     * Creates a new predefined quantity that holds predefined units which are related to it.
     *
     * @param name  The name of the quantity
     * @param units Array of predefined units that are related to the quantity
     */
    PredefinedQuantity(String name, PredefinedUnit<? extends Quantity>[] units) {
        this.name = name;
        this.units = units;
    }

    /**
     * Returns a list of predefined quantities, holding predefined units which are compatible to a given unit.
     *
     * @param compatibleUnit A string specifying the unit for which compatible units are supposed to be retrieved
     * @return A list of predefined quantities holding predefined units
     */
    public static List<PredefinedQuantity> getCompatibleQuantities(String compatibleUnit) {
        //Create new empty list for quantities
        List<PredefinedQuantity> quantitiesList = new ArrayList<>();

        //Try to get unit object from given unit string
        Unit<? extends Quantity> givenUnit = Unit.valueOf(compatibleUnit);

        //Iterate over all quantities and filter the ones that are compatible
        for (PredefinedQuantity quantity : PredefinedQuantity.values()) {
            //Skip quantity if it has no units
            if ((quantity.units == null) || (quantity.units.length < 1)) {
                continue;
            }

            //Get first unit of this quantity
            PredefinedUnit<? extends Quantity> firstUnit = quantity.units[0];

            //Check for compatibility
            if (firstUnit.getUnit().isCompatible(givenUnit)) {
                quantitiesList.add(quantity);
            }
        }
        return quantitiesList;
    }

    /**
     * Returns the name of the quantity.
     *
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the quantity.
     *
     * @param name The name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns a array of predefined units that are related to the quantity.
     *
     * @return The predefined units
     */
    public PredefinedUnit<? extends Quantity>[] getUnits() {
        return units;
    }

    /**
     * Sets a array of predefined units that are related to the quantity.
     *
     * @param units The predefined units to set
     */
    public void setUnits(PredefinedUnit<? extends Quantity>[] units) {
        this.units = units;
    }
}
