package de.ipvs.as.mbp.domain.visualization;

import de.ipvs.as.mbp.domain.data_model.IoTDataTypes;

import java.util.HashMap;
import java.util.Map;

public class VisualizationCollection {

    public final static Map<String, Visualization> visIdMapping  = new HashMap<>();

    static {
        visIdMapping.put(
                "stringVis1",
                new Visualization("stringVis1").addNewPrimitiveType("stringVal", IoTDataTypes.STRING)
        );

        visIdMapping.put(
                "arrLong",
                new Visualization("arrLong").addNewArray("arrVal", IoTDataTypes.LONG)
        );
    }

}
