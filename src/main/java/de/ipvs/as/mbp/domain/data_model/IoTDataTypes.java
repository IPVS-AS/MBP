package de.ipvs.as.mbp.domain.data_model;

import de.ipvs.as.mbp.service.cep.engine.core.events.CEPPrimitiveDataTypes;

/**
 * An enum for storing all available tree node types for a data model tree together with
 * their string value representation for interfering types from strings.
 */
public enum IoTDataTypes implements Comparable<IoTDataTypes> {

    DOUBLE("double", CEPPrimitiveDataTypes.DOUBLE),
    STRING("string", CEPPrimitiveDataTypes.STRING),
    BINARY("binary", null),
    BOOLEAN("boolean", CEPPrimitiveDataTypes.BOOLEAN),
    DATE("date", null),
    INT("int", CEPPrimitiveDataTypes.INTEGER),
    LONG("long", CEPPrimitiveDataTypes.LONG),
    DECIMAL128("decimal128", CEPPrimitiveDataTypes.BIG_DECIMAL),
    OBJECT("object", null),
    ARRAY("array", null);

    private final String value;
    private final CEPPrimitiveDataTypes cepType;

    IoTDataTypes(String value, CEPPrimitiveDataTypes cepType) {
        this.value = value;
        this.cepType = cepType;
    }

    public String getValue() {
        return value;
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
            if (value.equals(allIoTDataType.getValue())) {
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
            if (allIoTDataType.getValue().equals(testString)) {
                return true;
            }
        }
        return false;
    }
}
