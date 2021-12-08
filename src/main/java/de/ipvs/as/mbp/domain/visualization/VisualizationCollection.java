package de.ipvs.as.mbp.domain.visualization;

import de.ipvs.as.mbp.domain.data_model.DataModelDataType;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores all data models of all {@link Visualization}s of the MBP frontend.
 */
public class VisualizationCollection {

    public final static Map<String, Visualization> visNameMapping = new HashMap<>();

    static {

        visNameMapping.put(
                "doubleVis",
                new Visualization("doubleVis")
                        .addFieldsToVisualize(new VisualizationFields("default")
                                .addNewPrimitiveType("value", DataModelDataType.DECIMAL128)
                                .addNewPrimitiveType("value", DataModelDataType.DOUBLE)
                                .addNewPrimitiveType("value", DataModelDataType.LONG)
                                .addNewPrimitiveType("value", DataModelDataType.INT)
                        )
                        .addFieldsToVisualize(new VisualizationFields("arrVal")
                                .addNewArray("arrVal", DataModelDataType.DECIMAL128, 1)
                                .addNewArray("arrVal", DataModelDataType.DOUBLE,1)
                                .addNewArray("arrVal", DataModelDataType.LONG, 1)
                                .addNewArray("arrVal", DataModelDataType.INT, 1)
                        )
        );

        // Map
        visNameMapping.put(
                "geoMap",
                new Visualization("geoMap")
                        .addFieldsToVisualize(new VisualizationFields("default")
                                .addNewPrimitiveType("latitude", DataModelDataType.DECIMAL128)
                                .addNewPrimitiveType("latitude", DataModelDataType.DOUBLE)
                                .addNewPrimitiveType("latitude", DataModelDataType.LONG)
                                .addNewPrimitiveType("latitude", DataModelDataType.INT)
                                .addNewPrimitiveType("longitude", DataModelDataType.DECIMAL128)
                                .addNewPrimitiveType("longitude", DataModelDataType.DOUBLE)
                                .addNewPrimitiveType("longitude", DataModelDataType.LONG)
                                .addNewPrimitiveType("longitude", DataModelDataType.INT))
        );
    }

}
