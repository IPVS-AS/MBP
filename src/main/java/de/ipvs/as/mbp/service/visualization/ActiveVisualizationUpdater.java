package de.ipvs.as.mbp.service.visualization;

import com.jayway.jsonpath.JsonPath;
import de.ipvs.as.mbp.domain.component.Sensor;
import de.ipvs.as.mbp.domain.visualization.ActiveVisualization;
import de.ipvs.as.mbp.domain.visualization.VisualizationCollection;
import de.ipvs.as.mbp.error.MBPException;
import de.ipvs.as.mbp.repository.SensorRepository;
import de.ipvs.as.mbp.util.Validation;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Provides access operations to the database for
 * managing the visualization views.
 */
@Service
public class ActiveVisualizationUpdater {

    @Autowired
    private SensorRepository sensorRepository;

    /**
     * Updates the currently active visualization settings for one sensor persistent to the database.
     * If the {@link ActiveVisualization#getInstanceId()} is already used for the sensor, the
     * visualization settings for this visualization will be updated. Else, a new ActiveComponentVisualization
     * entry will be added to the sensor with a new uniquely generated id.
     *
     * @param sensorToUpdate      The sensor of which the visualization fields should be updated.
     * @param visToCreateOrUpdate The {@link ActiveVisualization} which should be edited or created new.
     * @return The sensor which was updated.
     */
    public Sensor updateOrCreateActiveVisualization(Sensor sensorToUpdate, ActiveVisualization visToCreateOrUpdate) {

        // Validate the input TODO remove comment
        // this.validateVisualizationSettings(visToCreateOrUpdate, sensorToUpdate);

        List<ActiveVisualization> currActiveVisualizationsOfSensor = sensorToUpdate.getActiveVisualizations();

        ActiveVisualization toEdit = null;
        if (sensorToUpdate.getActiveVisualizations() != null) {
            for (ActiveVisualization vis : currActiveVisualizationsOfSensor) {
                if (vis.getInstanceId().equals(visToCreateOrUpdate.getInstanceId())) {
                    toEdit = vis;
                }
            }
        }

        if (toEdit != null) {
            // Entity does exist already --> modify it
            toEdit.setVisFieldToPathMapping(visToCreateOrUpdate.getVisFieldToPathMapping());
        } else {
            // Entity does not exist --> create a new one
            sensorToUpdate.addActiveVisualization(new ActiveVisualization()
                    .setVisId(visToCreateOrUpdate.getVisId())
                    .setVisFieldToPathMapping(visToCreateOrUpdate.getVisFieldToPathMapping())
            );
        }

        // Apply the changes to the database
        sensorRepository.save(sensorToUpdate);
        return sensorToUpdate;
    }

    /**
     * Deletes a visual component of a sensor by the sensor reference and the visual
     * component id. Applies the changes to the database.
     *
     * @param sensor
     * @param visualComponentId
     */
    public  ResponseEntity deleteVisualComponent(Sensor sensor, String visualComponentId) {
        for (ActiveVisualization vis : sensor.getActiveVisualizations()) {
            if (vis.getInstanceId().equals(visualComponentId)) {
                sensor.removeActiveVisualization(visualComponentId);
                sensorRepository.save(sensor);
                return ResponseEntity.ok().build();
            }
        }

        throw new MBPException(HttpStatus.NOT_FOUND, "No visual component with the id " + visualComponentId + " found!");
    }

    private void validateVisualizationSettings(ActiveVisualization visToValidate, Sensor sensor) throws MBPException {

        // 0) Check if a id is given
        if (Validation.isNullOrEmpty(visToValidate.getVisId())) {
            throw new MBPException(HttpStatus.NOT_FOUND, "An id of the visualization component is missing.");
        }

        // 1) Check if a json key to json path mapping exists
        if (visToValidate.getVisFieldToPathMapping() == null || visToValidate.getVisFieldToPathMapping().size() <= 0) {
            throw new MBPException(HttpStatus.NOT_FOUND, "Visualization settings map is missing!");
        }

        // 2) Check if the id matches an existing visual component
        if (!VisualizationCollection.visIdMapping.containsKey(visToValidate.getVisId())) {
            throw new MBPException(HttpStatus.NOT_FOUND,
                    "The visual component with the id " + visToValidate.getVisId() + " does not exist.");
        }

        // 3) Check if the key fields of the json key to json path map are all valid and complete
        if (!visToValidate.getVisFieldToPathMapping().keySet().equals(
                VisualizationCollection.visIdMapping.get(visToValidate.getVisId()).getFieldsToVisualize().keySet())) {
            throw new MBPException(HttpStatus.NOT_FOUND,
                    "For visual component with the id " + visToValidate.getVisId() + ", invalid visualization" +
                            "parameters were provided.");
        }

        // 4) Check if all value fields of the visual component map are valid json paths
        String jsonDataModelExample = sensor.getOperator().getDataModel().getJSONExample();
        try {
            JSONObject exampleObj = new JSONObject(jsonDataModelExample);
            for (String pathToValidate : visToValidate.getVisFieldToPathMapping().values()) {
                JsonPath testPath = JsonPath.compile(pathToValidate);
                testPath.read(exampleObj.getJSONObject("value").toString());
            }
        } catch (Exception e) {
            throw new MBPException(HttpStatus.NOT_ACCEPTABLE, "Invalid json path provided.");
        }

    }
}
