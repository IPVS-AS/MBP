package de.ipvs.as.mbp.domain.data_model;

import de.ipvs.as.mbp.service.cep.engine.core.events.CEPPrimitiveDataTypes;

import java.math.BigDecimal;
import java.util.Date;

/**
 * An enum for storing all available tree node types for a data model tree together with
 * their string value representation for interfering types from strings.
 */
public enum IoTDataTypes implements Comparable<IoTDataTypes> {

    DOUBLE("double",
            CEPPrimitiveDataTypes.DOUBLE,
            Double.class),
    STRING("string",
            CEPPrimitiveDataTypes.STRING,
            String.class),
    BINARY("binary",
            null,
            Byte[].class),
    BOOLEAN("boolean",
            CEPPrimitiveDataTypes.BOOLEAN,
            Boolean.class),
    DATE("date",
            null,
            Date.class),
    INT("int",
            CEPPrimitiveDataTypes.INTEGER,
            Integer.class),
    LONG("long",
            CEPPrimitiveDataTypes.LONG,
            Long.class),
    DECIMAL128("decimal128",
            CEPPrimitiveDataTypes.BIG_DECIMAL,
            BigDecimal.class),
    OBJECT("object",
            null,
            null),
    ARRAY("array",
            null,
            null);

    private final String name;
    private final CEPPrimitiveDataTypes cepType;
    private Class<?> referenceClass;


    IoTDataTypes(String name, CEPPrimitiveDataTypes cepType, Class<?> referenceClass) {
        this.name = name;
        this.cepType = cepType;
        this.referenceClass = referenceClass;
    }

    public Class<?> getReferenceClass() {
        return referenceClass;
    }

    public String getName() {
        return name;
    }

    /**
     * @return The analog {@link CEPPrimitiveDataTypes} of this {@link IoTDataTypes} or null
     * if no analog type exists.
     */
    public CEPPrimitiveDataTypes getCepType() {
        return cepType;
    }

    public static boolean hasCepPrimitiveDataType(IoTDataTypes type) {
        if (type.getCepType() != null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns whether a given data type is primitve (no array and no object).
     *
     * @param type the {@link IoTDataTypes} to check.
     * @return true if the type is primitive, false if not
     */
    public static boolean isPrimitive(IoTDataTypes type) {
        return type != ARRAY && type != OBJECT;
    }

    /**
     * Checks if the given string matches a value of one of the enum data type
     * values. If yes, it returns the enum type, otherwise it returns null.
     *
     * @param value the string to match to an enum type
     * @return An {@link IoTDataTypes} enum value, or null if no match found
     */
    public static IoTDataTypes getDataTypeWithValue(String value) {
        IoTDataTypes[] allIoTDataTypes = IoTDataTypes.values();
        for (IoTDataTypes allIoTDataType : allIoTDataTypes) {
            if (value.equals(allIoTDataType.getName())) {
                return allIoTDataType;
            }
        }
        return null;
    }

    /**
     * Checks if a lower cased string matches the string value of one of the primitive data types.
     *
     * @param typeString the string to check for containment in the enum
     * @return true if the data type is known, false if not
     */
    public static boolean containsTypeString(String typeString) {
        String testString = typeString.toLowerCase();
        IoTDataTypes[] allIoTDataTypes = IoTDataTypes.values();
        for (IoTDataTypes allIoTDataType : allIoTDataTypes) {
            if (allIoTDataType.getName().equals(testString)) {
                return true;
            }
        }
        return false;
    }
}
