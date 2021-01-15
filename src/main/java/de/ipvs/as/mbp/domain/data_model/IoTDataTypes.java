package de.ipvs.as.mbp.domain.data_model;

/**
 * An enum for storing all available tree node types for a data model tree together with
 * their string value representation for interfering types from strings.
 */
public enum IoTDataTypes {

    DOUBLE("double"),
    STRING("string"),
    BINARY("binary"),
    BOOLEAN("boolean"),
    DATE("date"),
    INT("int"),
    LONG("long"),
    DECIMAL128("decimal128"),
    OBJECT("object"),
    ARRAY("array");

    private String value;

    private IoTDataTypes(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Returns whether a given data type is primitve (no array and no object).
     * @param type the {@link IoTDataTypes} to check.
     * @return true if the type is primitive, false if not
     */
    public static boolean isPrimitive(IoTDataTypes type) {
        if (type == ARRAY || type == OBJECT) {
            return false;
        }
        return true;
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
        for (int i = 0; i < allIoTDataTypes.length; i++) {
            if (value.equals(allIoTDataTypes[i].getValue())) {
                return allIoTDataTypes[i];
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
