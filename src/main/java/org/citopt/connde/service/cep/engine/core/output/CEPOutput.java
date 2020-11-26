package org.citopt.connde.service.cep.engine.core.output;

import java.util.HashMap;
import java.util.Map;

import com.espertech.esper.event.map.MapEventBean;

/**
 * Objects of this class represent the result of CEP queries and consist out of
 * the output map that was returned by a CEP query.
 */
public class CEPOutput {

    private Map<Object, Object> outputMap;

    /**
     * Creates a new CEP result object that holds an empty CEP query result.
     */
    public CEPOutput() {
        setOutputMap(new HashMap<>());
    }

    /**
     * Creates a new CEP result object by passing a map which is the result of a CEP query.
     *
     * @param outputMap The CEP query output map
     */
    public CEPOutput(Map<Object, Object> outputMap) {
        setOutputMap(outputMap);
    }

    /**
     * Returns the result of the CEP query as output map.
     *
     * @return The output map
     */
    public Map<Object, Object> getOutputMap() {
        return outputMap;
    }

    /**
     * Sets the result of the CEP query as output map.
     *
     * @param outputMap The output map to set
     */
    public void setOutputMap(Map<Object, Object> outputMap) {
        //Sanity check
        if (outputMap == null) {
            this.outputMap = new HashMap<>();
            return;
        }

        //Iterate over output map and get rid of irrelevant objects
        for (Object key : outputMap.keySet()) {
            Object value = outputMap.get(key);

            //Check if MapEventBean object
            if (value instanceof MapEventBean) {
                //Replace MapEventBean object with its properties map
                MapEventBean mapEventBean = (MapEventBean) value;
                Map<String, Object> propertiesMap = mapEventBean.getProperties();
                outputMap.put(key, propertiesMap);
            }
        }

        this.outputMap = outputMap;
    }
}
