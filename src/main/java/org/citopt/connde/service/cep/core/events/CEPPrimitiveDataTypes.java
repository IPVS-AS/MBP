package org.citopt.connde.service.cep.core.events;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Enumeration of all primitive data types that are available for fields of CEP events and CEP queries.
 */
public enum CEPPrimitiveDataTypes {
    BOOLEAN("boolean", Boolean.class), BYTE("byte", Byte.class),
    SHORT("short", Short.class), INTEGER("int", Integer.class), LONG("long", Long.class),
    FLOAT("float", Float.class), DOUBLE("double", Double.class),
    BIG_INTEGER("BigInteger", BigInteger.class), BIG_DECIMAL("BigDecimal", BigDecimal.class),
    STRING("string", String.class);

    //Name of the data type and reference class for type-checking
    private String name;
    private Class referenceClass;

    /**
     * Creates a new primitive data type as part of the enumeration.
     *
     * @param name           The name of the data type
     * @param referenceClass A reference class of the data type for type-checking
     */
    CEPPrimitiveDataTypes(String name, Class referenceClass) {
        setName(name);
        setReferenceClass(referenceClass);
    }

    /**
     * Returns the name of the primitive data type.
     *
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the primitive data type.
     *
     * @param name The name to set
     */
    private void setName(String name) {
        //Sanity check
        if ((name == null) || (name.isEmpty())) {
            throw new IllegalArgumentException("Type name must not be null or empty.");
        }
        this.name = name;
    }

    /**
     * Returns the reference class of the primitive data type.
     *
     * @return The reference class
     */
    public Class getReferenceClass() {
        return referenceClass;
    }

    /**
     * Sets the reference class of the primitive data type that is used for type-checking.
     *
     * @param referenceClass The reference class
     */
    private void setReferenceClass(Class referenceClass) {
        //Sanity check
        if (referenceClass == null) {
            throw new IllegalArgumentException("Reference class must not be null.");
        }
        this.referenceClass = referenceClass;
    }
}
