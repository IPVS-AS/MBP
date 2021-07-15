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
                "doubleVis",
                new Visualization("doubleVis")
                        .addFieldsToVisualize(new VisualizationFields("default")
                                .addNewPrimitiveType("value", IoTDataTypes.DECIMAL128)
                                .addNewPrimitiveType("value", IoTDataTypes.DOUBLE)
                                .addNewPrimitiveType("value", IoTDataTypes.LONG)
                                .addNewPrimitiveType("value", IoTDataTypes.INT)
                        )
                        .addFieldsToVisualize(new VisualizationFields("arrVal")
                                .addNewArray("arrVal", IoTDataTypes.DECIMAL128, 1)
                                .addNewArray("arrVal", IoTDataTypes.DOUBLE,1)
                                .addNewArray("arrVal", IoTDataTypes.LONG, 1)
                                .addNewArray("arrVal", IoTDataTypes.INT, 1)
                        )
        );

        // Map
        visIdMapping.put(
                "geoMap",
                new Visualization("geoMap")
                        .addFieldsToVisualize(new VisualizationFields("default")
                                .addNewPrimitiveType("latitude", IoTDataTypes.DECIMAL128)
                                .addNewPrimitiveType("latitude", IoTDataTypes.DOUBLE)
                                .addNewPrimitiveType("latitude", IoTDataTypes.LONG)
                                .addNewPrimitiveType("latitude", IoTDataTypes.INT)
                                .addNewPrimitiveType("longitude", IoTDataTypes.DECIMAL128)
                                .addNewPrimitiveType("longitude", IoTDataTypes.DOUBLE)
                                .addNewPrimitiveType("longitude", IoTDataTypes.LONG)
                                .addNewPrimitiveType("longitude", IoTDataTypes.INT))
        );
    }

}
