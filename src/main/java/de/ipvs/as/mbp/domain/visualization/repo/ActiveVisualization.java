package de.ipvs.as.mbp.domain.visualization.repo;

import org.bson.types.ObjectId;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.HashMap;
import java.util.Map;

/**
 * Data object to store all data which is needed to store one currently
 * active visualization for one component together with its active
 * JsonPath value mapping.
 */
public class ActiveVisualization {

    @Id
    @GeneratedValue
    private String instanceId;

    private String visId;

    private String fieldCollectionId;

    private Map<String, String> visFieldToPathMapping;

    public ActiveVisualization() {
        this.instanceId = new ObjectId().toString();
        visFieldToPathMapping = new HashMap<>();
        visFieldToPathMapping.put("test", "test");
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

    public Map<String, String> getVisFieldToPathMapping() {
        return visFieldToPathMapping;
    }

    public ActiveVisualization setVisFieldToPathMapping(Map<String, String> visFieldToPathMapping) {
        this.visFieldToPathMapping = visFieldToPathMapping;
        return this;
    }

    public ActiveVisualization addFieldToPathMapping(String field, String path) {
        this.visFieldToPathMapping.put(field, path);
        return this;
    }

    public String getInstanceId() {
        return instanceId;
    }

    /*
    public ActiveComponentVisualization setInstanceId(String instanceId) {
        this.instanceId = instanceId;
        return this;
    }
    */

    @Override
    public boolean equals(Object obj) {
        ActiveVisualization inferredObj = (ActiveVisualization) obj;
        return this.instanceId.equals(inferredObj.getInstanceId()) && this.visId.equals(inferredObj.getVisId()) &&
                this.visFieldToPathMapping.equals(inferredObj.getVisFieldToPathMapping());
    }
}
