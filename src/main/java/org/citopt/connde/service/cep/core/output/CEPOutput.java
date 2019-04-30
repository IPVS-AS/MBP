package org.citopt.connde.service.cep.core.output;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Objects of this class represent the output of CEP queries and consist out of a
 * set of columns that hold the output field values.
 */
public class CEPOutput {
    //Map (column name --> column) of output columns
    private Map<String, CEPOutputColumn> outputColumns;

    /**
     * Creates a new query output object by passing a map (column name --> map (field name --> field value))
     * of output columns and the their field values.
     *
     * @param outputColumns The map of output columns
     */
    public CEPOutput(Map<String, Map<String, Object>> outputColumns) {
        //Sanity check
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

    /**
     * Returns the number of columns that are part of the output.
     *
     * @return The number of columns
     */
    public int numberOfColumns() {
        return outputColumns.size();
    }

    /**
     * Checks whether the output contains a column with a certain name.
     *
     * @param columnName The name of the column of question
     * @return True, if the output contains a column of this name; false otherwise
     */
    public boolean containsColumn(String columnName) {
        //Sanity check
        if ((columnName == null) || (columnName.isEmpty())) {
            throw new IllegalArgumentException("Column name must not be null or empty.");
        }

        return outputColumns.containsKey(columnName);
    }

    /**
     * Returns a column of a certain name that is part of the output.
     *
     * @param columnName The name of the column to return
     * @return The column
     */
    public CEPOutputColumn getColumn(String columnName) {
        //Sanity checks
        if ((columnName == null) || (columnName.isEmpty())) {
            throw new IllegalArgumentException("Column name must not be null or empty.");
        } else if (!outputColumns.containsKey(columnName)) {
            throw new IllegalArgumentException("A column with this name does not exist in this output.");
        }

        return outputColumns.get(columnName);
    }

    /**
     * Returns a set of column names that are part of the output.
     *
     * @return The set of names
     */
    public Set<String> getColumnNames() {
        return outputColumns.keySet();
    }
}
