package de.ipvs.as.mbp.domain.data_model;

import de.ipvs.as.mbp.service.cep.engine.core.events.CEPPrimitiveDataTypes;
import org.apache.commons.lang3.RandomStringUtils;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/**
 * An enum for storing all available tree node types for a data model tree together with
 * their string value representation for interfering types from strings.
 */
public enum DataModelDataType implements Comparable<DataModelDataType> {

    DOUBLE("double", CEPPrimitiveDataTypes.DOUBLE, Double.class,
            () -> Math.round(ThreadLocalRandom.current().nextDouble(0, 100) * 1.0e2) / 1.0e2),
    STRING("string", CEPPrimitiveDataTypes.STRING, String.class,
            () -> RandomStringUtils.randomAlphabetic(ThreadLocalRandom.current().nextInt(30))),
    BINARY("binary", null, Byte[].class,
            () -> "QmFzZTY0QmluYXJ5U3RyaW5n"),
    BOOLEAN("boolean", CEPPrimitiveDataTypes.BOOLEAN, Boolean.class,
            () -> ThreadLocalRandom.current().nextBoolean()),
    DATE("date", null, Date.class,
            () -> new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date())),
    INT("int", CEPPrimitiveDataTypes.INTEGER, Integer.class,
            () -> ThreadLocalRandom.current().nextInt(100)),
    LONG("long", CEPPrimitiveDataTypes.LONG, Long.class,
            () -> ThreadLocalRandom.current().nextInt(100)),
    DECIMAL128("decimal128", CEPPrimitiveDataTypes.BIG_DECIMAL, BigDecimal.class,
            () -> ThreadLocalRandom.current().nextBoolean()),
    OBJECT("object", null, null,
            () -> null),
    ARRAY("array", null, null,
            () -> null);

    private final String name;
    private final CEPPrimitiveDataTypes cepType;
    private final Class<?> referenceClass;
    private final Supplier<Object> exampleSupplier;


    DataModelDataType(String name, CEPPrimitiveDataTypes cepType, Class<?> referenceClass, Supplier<Object> exampleSupplier) {
        this.name = name;
        this.cepType = cepType;
        this.referenceClass = referenceClass;
        this.exampleSupplier = exampleSupplier;
    }

    public Class<?> getReferenceClass() {
        return referenceClass;
    }

    public String getName() {
        return name;
    }

    /**
     * @return The analog {@link CEPPrimitiveDataTypes} of this {@link DataModelDataType} or null
     * if no analog type exists.
     */
    public CEPPrimitiveDataTypes getCepType() {
        return cepType;
    }

    /**
     * Generates and returns random example data for the {@link DataModelDataType}.
     *
     * @return The example data
     */
    public Object getExample() {
        //Check for null
        if (this.exampleSupplier == null) return null;

        //Generate and return example data using the supplier
        return this.exampleSupplier.get();
    }

    public static boolean hasCepPrimitiveDataType(DataModelDataType type) {
        if (type.getCepType() != null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns whether a given data type is primitve (no array and no object).
     *
     * @param type the {@link DataModelDataType} to check.
     * @return true if the type is primitive, false if not
     */
    public static boolean isPrimitive(DataModelDataType type) {
        return type != ARRAY && type != OBJECT;
    }

    /**
     * Checks if the given string matches a value of one of the enum data type
     * values. If yes, it returns the enum type, otherwise it returns null.
     *
     * @param value the string to match to an enum type
     * @return An {@link DataModelDataType} enum value, or null if no match found
     */
    public static DataModelDataType getDataTypeWithValue(String value) {
        DataModelDataType[] allDataModelDataTypes = DataModelDataType.values();
        for (DataModelDataType allIoTDataType : allDataModelDataTypes) {
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
        DataModelDataType[] allDataModelDataTypes = DataModelDataType.values();
        for (DataModelDataType allIoTDataType : allDataModelDataTypes) {
            if (allIoTDataType.getName().equals(testString)) {
                return true;
            }
        }
        return false;
    }
}
