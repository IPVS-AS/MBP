package org.citopt.connde.service.cep.core.queries.output;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CEPOutput {

    private Map<String, CEPOutputColumn> outputColumns;

    public CEPOutput(Map<String, Map<String, Object>> outputColumns) {
        setOutputColumns(outputColumns);
    }

    private void setOutputColumns(Map<String, Map<String, Object>> outputColumns) {
        if (outputColumns == null) {
            throw new IllegalArgumentException("Output columns must not be null.");
        }

        //Initialize internal column map (column name -> column)
        this.outputColumns = new HashMap<>();

        //Iterate over all column names
        Set<String> columnNames = outputColumns.keySet();
        for (String columnName : columnNames) {
            //Get field map for current column
            Map<String, Object> columnFields = outputColumns.get(columnName);

            //Create column object from fields map
            CEPOutputColumn column = new CEPOutputColumn(columnFields);

            //Add to internal columns map
            this.outputColumns.put(columnName, column);
        }
    }

    public int numberOfColumns() {
        return outputColumns.size();
    }

    public boolean containsColumn(String columnName) {
        if ((columnName == null) || (columnName.isEmpty())) {
            throw new IllegalArgumentException("Column name must not be null or empty.");
        }
        return outputColumns.containsKey(columnName);
    }

    public CEPOutputColumn getColumn(String columnName) {
        if ((columnName == null) || (columnName.isEmpty())) {
            throw new IllegalArgumentException("Column name must not be null or empty.");
        } else if (!outputColumns.containsKey(columnName)) {
            throw new IllegalArgumentException("A column with this name does not exist in this output.");
        }
        return outputColumns.get(columnName);
    }

    public Set<String> getColumnNames() {
        return outputColumns.keySet();
    }
}
