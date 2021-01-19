package de.ipvs.as.mbp.domain.visualization;

import de.ipvs.as.mbp.domain.valueLog.ValueLog;

import java.util.Map;

/**
 * Contains settings for one visualization of a component.
 */
public class VisualComponentEntity {

    /**
     * Maps the json keys of the visualization data to the keys
     * of the value stored in {@link ValueLog#getValue()}s.
     * <p>Key --> Visualization json key<br>
     * Value --> ValueLog json key</p>
     */
    private Map<String, String> nameMappings;

    /**
     * Path to the subtree of a {@link ValueLog#getValue()} which can be used by this {@link VisualComponentEntity}.
     */
    private String jsonPathStr;


    /**
     * The visualization
     */
    private Visualization visual;
}
