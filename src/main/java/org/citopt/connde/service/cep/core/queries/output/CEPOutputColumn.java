package org.citopt.connde.service.cep.core.queries.output;

import java.util.Map;
import java.util.Set;

/**
 * Objects of this class represent columns that are part of the output of CEP queries.
 */
class CEPOutputColumn {
    //Map (field name --> field value) of column fields
    private Map<String, Object> columnFields;

    /**
     * Creates a new output column by passing a map (field name --> field value) of column fields.
     *
     * @param columnFields The map of column fields
     */
    CEPOutputColumn(Map<String, Object> columnFields) {
        //Sanity check
        if (columnFields == null) {
            throw new IllegalArgumentException("Column fields must not be null.");
        }
        this.columnFields = columnFields;
    }

    /**
     * Returns the number of fields that are part of the column
     *
     * @return The number of fields
     */
    public int numberOfFields() {
        return columnFields.size();
    }

    /**
     * Checks whether the column contains a field with a certain name.
     *
     * @param fieldName The name of the field to check
     * @return True, if the column contains a field with this name; false otherwise
     */
    public boolean containsField(String fieldName) {
        //Sanity check
        if ((fieldName == null) || (fieldName.isEmpty())) {
            throw new IllegalArgumentException("Field name must not be null or empty.");
        }
        return columnFields.containsKey(fieldName);
    }

    /**
     * Returns the value of a field of a certain name that is part of the column.
     *
     * @param fieldName The name of the field to return
     * @return The value of the field
     */
    public Object getFieldValue(String fieldName) {
        //Sanity checks
        if ((fieldName == null) || (fieldName.isEmpty())) {
            throw new IllegalArgumentException("Field name must not be null or empty.");
        } else if (!columnFields.containsKey(fieldName)) {
            throw new IllegalArgumentException("A field with this name does not exist in this column.");
        }
        return columnFields.get(fieldName);
    }

    /**
     * Returns a set of field names that are part of the column.
     *
     * @return The set of names
     */
    public Set<String> getFieldNames() {
        return columnFields.keySet();
    }
}