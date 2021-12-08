package de.ipvs.as.mbp.domain.visualization.repo;

import org.bson.types.ObjectId;

import java.util.HashMap;
import java.util.Map;

/**
 * Data object to store all data which is needed to store one currently
 * active visualization for one component together with its active
 * JsonPath value mapping.
 */
public class ActiveVisualization {

    private String instanceId;

    private String visId;

    private String fieldCollectionId;

    private Map<String, ValueLogPathObject> visFieldToPathMapping;

    public ActiveVisualization() {
        this.instanceId = new ObjectId().toString();
        visFieldToPathMapping = new HashMap<>();
    }

    public String getFieldCollectionId() {
        return fieldCollectionId;
    }

    public ActiveVisualization setFieldCollectionId(String fieldCollectionId) {
        this.fieldCollectionId = fieldCollectionId;
        return this;
    }



    public String getVisId() {
        return visId;
    }

    public ActiveVisualization setVisId(String visId) {
        this.visId = visId;
        return this;
    }

    public Map<String, ValueLogPathObject> getVisFieldToPathMapping() {
        return visFieldToPathMapping;
    }

    public ActiveVisualization setVisFieldToPathMapping(Map<String, ValueLogPathObject> visFieldToPathMapping) {
        this.visFieldToPathMapping = visFieldToPathMapping;
        return this;
    }

    public ActiveVisualization addFieldToPathMapping(String field, ValueLogPathObject path) {
        this.visFieldToPathMapping.put(field, path);
        return this;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public ActiveVisualization setInstanceId(String instanceId) {
        this.instanceId = instanceId;
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        ActiveVisualization inferredObj = (ActiveVisualization) obj;
        return this.instanceId.equals(inferredObj.getInstanceId()) && this.visId.equals(inferredObj.getVisId()) &&
                this.visFieldToPathMapping.equals(inferredObj.getVisFieldToPathMapping());
    }
}
