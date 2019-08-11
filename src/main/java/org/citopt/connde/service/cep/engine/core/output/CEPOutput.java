package org.citopt.connde.service.cep.engine.core.output;

import java.util.Map;

/**
 * Objects of this class represent the result of CEP queries and consist out of
 * the output map that was returned by a CEP query.
 */
public class CEPOutput {

    private Map outputMap;

    /**
     * Creates a new CEP result object by passing a map which is the result of a CEP query.
     *
     * @param outputMap The CEP query output map
     */
    public CEPOutput(Map outputMap) {
        this.outputMap = outputMap;
    }

    /**
     * Returns the result of the CEP query as output map.
     *
     * @return The output map
     */
    public Map getOutputMap() {
        return outputMap;
    }
}
