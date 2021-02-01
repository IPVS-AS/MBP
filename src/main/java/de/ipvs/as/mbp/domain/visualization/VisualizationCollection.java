package de.ipvs.as.mbp.domain.visualization;

import de.ipvs.as.mbp.domain.data_model.IoTDataTypes;
import de.ipvs.as.mbp.domain.data_model.treelogic.DataModelTreeNode;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores all data models of all {@link Visualization}s of the MBP frontend.
 */
public class VisualizationCollection {

    public final static Map<String, Visualization> visIdMapping = new HashMap<>();

    static {

        visIdMapping.put(
                "stringVis1",
                new Visualization("stringVis1").addFieldsToVisualize(
                        new VisualizationFields("default").addNewPrimitiveType("stringVal", IoTDataTypes.STRING))
        );

        visIdMapping.put(
                "arrLong",
                new Visualization("arrLong").addFieldsToVisualize(
                        new VisualizationFields("default").addNewPrimitiveType("arrVal", IoTDataTypes.LONG))

        );

        visIdMapping.put(
                "doubleVis",
                new Visualization("doubleVis")
                        .addFieldsToVisualize(new VisualizationFields("default").addNewPrimitiveType("value", IoTDataTypes.DOUBLE))
                        .addFieldsToVisualize(new VisualizationFields("arrVal").addNewArray("arrVal", IoTDataTypes.DOUBLE))
        );

        // Map
        visIdMapping.put(
                "geoMap",
                new Visualization("geoMap")
                        .addFieldsToVisualize(new VisualizationFields("default").addNewPrimitiveType("latitude", IoTDataTypes.DOUBLE)
                                .addNewPrimitiveType("longitude", IoTDataTypes.DOUBLE))
        );
    }

}
