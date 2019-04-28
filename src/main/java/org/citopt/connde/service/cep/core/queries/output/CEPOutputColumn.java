package org.citopt.connde.service.cep.core.queries.output;

import java.util.Map;
import java.util.Set;

class CEPOutputColumn {
    private Map<String, Object> columnFields;

    CEPOutputColumn(Map<String, Object> columnFields) {
        setColumnFields(columnFields);
    }

    private void setColumnFields(Map<String, Object> columnFields) {
        if (columnFields == null) {
            throw new IllegalArgumentException("Column fields must not be null.");
        }
        this.columnFields = columnFields;
    }

    public int numberOfFields() {
        return columnFields.size();
    }

    public boolean containsField(String fieldName) {
        if ((fieldName == null) || (fieldName.isEmpty())) {
            throw new IllegalArgumentException("Field name must not be null or empty.");
        }
        return columnFields.containsKey(fieldName);
    }

    public Object getFieldValue(String fieldName) {
        if ((fieldName == null) || (fieldName.isEmpty())) {
            throw new IllegalArgumentException("Field name must not be null or empty.");
        } else if (!columnFields.containsKey(fieldName)) {
            throw new IllegalArgumentException("A field with this name does not exist in this column.");
        }
        return columnFields.get(fieldName);
    }

    public Set<String> getFieldNames() {
        return columnFields.keySet();
    }
}