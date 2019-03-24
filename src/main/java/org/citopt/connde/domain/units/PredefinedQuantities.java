package org.citopt.connde.domain.units;

import com.fasterxml.jackson.annotation.JsonFormat;

import javax.measure.quantity.*;
import javax.measure.unit.*;

import static javax.measure.unit.SI.*;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum PredefinedQuantities {

    DIMENSIONLESS("Dimensionless", new PredefinedUnit[]{
            new PredefinedUnit<>("No Unit", Unit.ONE),
            new PredefinedUnit<>("Percent", NonSI.PERCENT),
            new PredefinedUnit<>("Decibel", NonSI.DECIBEL)
    }),

    ACCELERATION("Acceleration", new PredefinedUnit[]{
            new PredefinedUnit<>("Meters per square second", SI.METERS_PER_SQUARE_SECOND)
    }),

    ANGLE("Angle", new PredefinedUnit[]{
            new PredefinedUnit<>("Radian", SI.RADIAN), new PredefinedUnit<>("Degree", NonSI.DEGREE_ANGLE)
    }),

    ANGULAR_ACCELERATION("Angular Acceleration", new PredefinedUnit[]{
            new PredefinedUnit<>("Radian per square second", AngularAcceleration.UNIT),
            new PredefinedUnit<>("Degree per square second", NonSI.DEGREE_ANGLE.divide(SI.SECOND.pow(2)))
    }),

    ANGULAR_VELOCITY("Angular Velocity", new PredefinedUnit[]{
            new PredefinedUnit<>("Radian per second", AngularVelocity.UNIT),
            new PredefinedUnit<>("Degree per second", NonSI.DEGREE_ANGLE.divide(SI.SECOND))
    }),

    AREA("Area", new PredefinedUnit[]{
            new PredefinedUnit<>("Square millimeters", MILLI(SI.METER).pow(2)),
            new PredefinedUnit<>("Square meters", SI.SQUARE_METRE),
            new PredefinedUnit<>("Hectare", NonSI.HECTARE),
            new PredefinedUnit<>("Are", NonSI.ARE)
    }),

    CATALYTIC_ACTIVITY("Catalytic activity", new PredefinedUnit[]{
            new PredefinedUnit<>("Katal", SI.KATAL)
    }),

    DATA_AMOUNT("Data amount", new PredefinedUnit[]{
            new PredefinedUnit<>("Bit", SI.BIT),
            new PredefinedUnit<>("Byte", NonSI.BYTE),
            new PredefinedUnit<>("Kilobyte", KILO(NonSI.BYTE)),
            new PredefinedUnit<>("Megabyte", MEGA(NonSI.BYTE)),
            new PredefinedUnit<>("Gigabyte", GIGA(NonSI.BYTE))
    }),

    DATA_RATE("Data rate", new PredefinedUnit[]{
            new PredefinedUnit<>("Bit per second", DataRate.UNIT),
            new PredefinedUnit<>("Byte per second", NonSI.BYTE.divide(SI.SECOND))
    }),

    DURATION("Duration", new PredefinedUnit[]{
            new PredefinedUnit<>("Second", SECOND),
            new PredefinedUnit<>("Minute", NonSI.MINUTE),
            new PredefinedUnit<>("Hour", NonSI.HOUR),
            new PredefinedUnit<>("Day", NonSI.DAY),
            new PredefinedUnit<>("Month", NonSI.MONTH),
            new PredefinedUnit<>("Year", NonSI.YEAR)
    }),

    ELECTRIC_CAPACITANCE("Electric capacitance", new PredefinedUnit[]{
            new PredefinedUnit<>("Farad", FARAD)
    }),

    ELECTRIC_CHARGE("Electric charge", new PredefinedUnit[]{
            new PredefinedUnit<>("Coulomb", COULOMB)
    }),

    ELECTRIC_CONDUCTANCE("Electric conductance", new PredefinedUnit[]{
            new PredefinedUnit<>("Siemens", SIEMENS)
    }),

    ELECTRIC_CURRENT("Electric current", new PredefinedUnit[]{
            new PredefinedUnit<>("Ampere", AMPERE)
    }),

    ELECTRIC_INDUCTANCE("Electric inductance", new PredefinedUnit[]{
            new PredefinedUnit<>("Henry", HENRY)
    }),

    ELECTRIC_POTENTIAL("Electric potential", new PredefinedUnit[]{
            new PredefinedUnit<>("Volt", VOLT)
    }),

    ELECTRIC_RESISTANCE("Electric resistance", new PredefinedUnit[]{
            new PredefinedUnit<>("Ohm", OHM)
    }),

    ENERGY("Energy", new PredefinedUnit[]{
            new PredefinedUnit<>("Joule", JOULE)
    }),

    FORCE("Force", new PredefinedUnit[]{
            new PredefinedUnit<>("Newton", NEWTON)
    }),

    FREQUENCY("Frequency", new PredefinedUnit[]{
            new PredefinedUnit<>("Hertz", HERTZ)
    }),

    ILLUMINANCE("Illuminance", new PredefinedUnit[]{
            new PredefinedUnit<>("Lux", LUX)
    }),

    LENGTH("Length", new PredefinedUnit[]{
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

    LUMINOUS_FLUX("Luminous Flux", new PredefinedUnit[]{
            new PredefinedUnit<>("Lumen", LUMEN)
    }),

    LUMINOUS_INTENSITY("Luminous Intensity", new PredefinedUnit[]{
            new PredefinedUnit<>("Candela", CANDELA)
    }),

    MAGNETIC_FLUX("Magnetic Flux", new PredefinedUnit[]{
            new PredefinedUnit<>("Weber", WEBER)
    }),

    MAGNETIC_FLUX_DENSITY("Magnetic Flux Density", new PredefinedUnit[]{
            new PredefinedUnit<>("Tesla", TESLA)
    }),

    MASS("Mass", new PredefinedUnit[]{
            new PredefinedUnit<>("Milligram", MILLI(GRAM)),
            new PredefinedUnit<>("Gram", GRAM),
            new PredefinedUnit<>("Kilogram", KILOGRAM),
            new PredefinedUnit<>("Pound", NonSI.POUND),
            new PredefinedUnit<>("Ton", NonSI.METRIC_TON)
    }),

    MASS_FLOW_RATE("Mass Flow Rate", new PredefinedUnit[]{
            new PredefinedUnit<>("Kilogram per second", MassFlowRate.UNIT)
    }),

    POWER("Power", new PredefinedUnit[]{
            new PredefinedUnit<>("Watt", WATT)
    }),

    PRESSURE("Pressure", new PredefinedUnit[]{
            new PredefinedUnit<>("Pascal", PASCAL),
            new PredefinedUnit<>("Bar", NonSI.BAR)
    }),

    RADIATION_DOSE_ABSORBED("Radiation Dose Absorbed", new PredefinedUnit[]{
            new PredefinedUnit<>("Gray", GRAY)
    }),

    RADIATION_DOSE_EFFECTIVE("Radiation Dose Effective", new PredefinedUnit[]{
            new PredefinedUnit<>("Sievert", SIEVERT)
    }),

    RADIOACTIVE_ACTIVITY("Radioactive Activity", new PredefinedUnit[]{
            new PredefinedUnit<>("Becquerel", BECQUEREL)
    }),

    TEMPERATURE("Temperature", new PredefinedUnit[]{
            new PredefinedUnit<>("Kelvin", KELVIN),
            new PredefinedUnit<>("Degree Celsius", CELSIUS),
            new PredefinedUnit<>("Fahrenheit", NonSI.FAHRENHEIT)
    }),

    TORQUE("Torque", new PredefinedUnit[]{
            new PredefinedUnit<>("Newton meter", Torque.UNIT)
    }),

    VELOCITY("Velocity", new PredefinedUnit[]{
            new PredefinedUnit<>("Meter per second", METERS_PER_SECOND),
            new PredefinedUnit<>("Kilometer per second", NonSI.KILOMETERS_PER_HOUR),
    }),

    VOLUME("Volume", new PredefinedUnit[]{
            new PredefinedUnit<>("Cubic meter", CUBIC_METRE),
            new PredefinedUnit<>("Liter", NonSI.LITER),
            new PredefinedUnit<>("Milliliter", MILLI(NonSI.LITER))
    }),

    VOLUMETRIC_DENSITY("Volumetric Density", new PredefinedUnit[]{
            new PredefinedUnit<>("Kilogram per cubic meter", VolumetricDensity.UNIT)
    }),

    VOLUMETRIC_FLOW_RATE("Volumetric Flow Rate", new PredefinedUnit[]{
            new PredefinedUnit<>("Cubic meter per second", VolumetricFlowRate.UNIT)
    });

    private String name;

    private PredefinedUnit[] units;

    PredefinedQuantities(String name, PredefinedUnit[] units) {
        this.name = name;
        this.units = units;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PredefinedUnit[] getUnits() {
        return units;
    }

    public void setUnits(PredefinedUnit[] units) {
        this.units = units;
    }
}
