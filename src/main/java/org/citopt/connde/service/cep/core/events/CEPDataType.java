package org.citopt.connde.service.cep.core.events;

import java.math.BigDecimal;
import java.math.BigInteger;

public enum CEPDataType {
    BOOLEAN("boolean", Boolean.class), BYTE("byte", Byte.class),
    SHORT("short", Short.class), INTEGER("int", Integer.class), LONG("long", Long.class),
    FLOAT("float", Float.class), DOUBLE("double", Double.class),
    BIG_INTEGER("BigInteger", BigInteger.class), BIG_DECIMAL("BigDecimal", BigDecimal.class),
    STRING("string", String.class);

    private String typeName;
    private Class referenceClass;

    CEPDataType(String typeName, Class referenceClass) {
        setTypeName(typeName);
        setReferenceClass(referenceClass);
    }

    public String getTypeName() {
        return typeName;
    }

    private void setTypeName(String typeName) {
        if ((typeName == null) || (typeName.isEmpty())) {
            throw new IllegalArgumentException("Type name must not be null or empty.");
        }
        this.typeName = typeName;
    }

    public Class getReferenceClass() {
        return referenceClass;
    }

    private void setReferenceClass(Class referenceClass) {
        if (referenceClass == null) {
            throw new IllegalArgumentException("Reference class must not be null.");
        }
        this.referenceClass = referenceClass;
    }
}
