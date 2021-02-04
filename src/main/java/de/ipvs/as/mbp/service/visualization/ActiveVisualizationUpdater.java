package de.ipvs.as.mbp.service.visualization;

import com.jayway.jsonpath.JsonPath;
import de.ipvs.as.mbp.domain.component.Actuator;
import de.ipvs.as.mbp.domain.component.Component;
import de.ipvs.as.mbp.domain.component.Sensor;
import de.ipvs.as.mbp.domain.visualization.Visualization;
import de.ipvs.as.mbp.domain.visualization.VisualizationFields;
import de.ipvs.as.mbp.domain.visualization.repo.ActiveVisualization;
import de.ipvs.as.mbp.domain.visualization.VisualizationCollection;
import de.ipvs.as.mbp.domain.visualization.repo.PathUnitPair;
import de.ipvs.as.mbp.error.MBPException;
import de.ipvs.as.mbp.repository.ActuatorRepository;
import de.ipvs.as.mbp.repository.ComponentRepository;
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

    @Autowired
    private ActuatorRepository actuatorRepository;

    /**
     * Updates the currently active visualization settings for one component persistent to the database.
     * If the {@link ActiveVisualization#getInstanceId()} is already used for the sensor, the
     * visualization settings for this visualization will be updated. Else, a new ActiveComponentVisualization
     * entry will be added to the sensor with a new uniquely generated id.
     *
     * @param componentToUpdate      The sensor of which the visualization fields should be updated.
     * @param visToCreateOrUpdate The {@link ActiveVisualization} which should be edited or created new.
     * @return The sensor which was updated.
     */
    public Component updateOrCreateActiveVisualization(Component componentToUpdate, ActiveVisualization visToCreateOrUpdate) {

        // Validate the input TODO remove comment
        // this.validateVisualizationSettings(visToCreateOrUpdate, sensorToUpdate);

        List<ActiveVisualization> currActiveVisualizationsOfSensor = componentToUpdate.getActiveVisualizations();

        ActiveVisualization toEdit = null;
        if (componentToUpdate.getActiveVisualizations() != null) {
            for (ActiveVisualization vis : currActiveVisualizationsOfSensor) {
                if (vis.getInstanceId().equals(visToCreateOrUpdate.getInstanceId())) {
                    toEdit = vis;
                }
            }
        }

        if (toEdit != null) {
            // Entity does exist already --> modify it
            toEdit.setVisFieldToPathMapping(visToCreateOrUpdate.getVisFieldToPathMapping());
            toEdit.setFieldCollectionId(visToCreateOrUpdate.getFieldCollectionId());
        } else {
            // Entity does not exist --> create a new one
            componentToUpdate.addActiveVisualization(new ActiveVisualization()
                    .setVisId(visToCreateOrUpdate.getVisId())
                    .setVisFieldToPathMapping(visToCreateOrUpdate.getVisFieldToPathMapping())
                    .setFieldCollectionId(visToCreateOrUpdate.getFieldCollectionId()));
        }

        // Apply the changes to the respective database (either the on for actuators or sensors)
        if (componentToUpdate instanceof Sensor) {
            sensorRepository.save((Sensor) componentToUpdate);
        } else if (componentToUpdate instanceof Actuator){
            actuatorRepository.save((Actuator) componentToUpdate);
        } else {
            System.err.println("Error applying visualization changes to sensor or actuator repository.");
        }
        return componentToUpdate;
    }

    /**
     * Deletes a visual component of a sensor by the sensor reference and the visual
     * component id. Applies the changes to the database.
     *
     * @param component
     * @param visualComponentId
     */
    public ResponseEntity deleteVisualComponent(Component component, String visualComponentId) {
        for (ActiveVisualization vis : component.getActiveVisualizations()) {
            if (vis.getInstanceId().equals(visualComponentId)) {
                component.removeActiveVisualization(visualComponentId);

                if (component instanceof Sensor) {
                    sensorRepository.save((Sensor) component);
                } else if (component instanceof Actuator){
                    actuatorRepository.save((Actuator) component);
                } else {
                    System.err.println("Error applying visualization changes to sensor or actuator repository.");
                }

                return ResponseEntity.ok().build();
            }
        }

        throw new MBPException(HttpStatus.NOT_FOUND, "No visual component with the id " + visualComponentId + " found!");
    }

    private void validateVisualizationSettings(ActiveVisualization visToValidate, Component component) throws MBPException {

        // 0) Check if a id is given
        if (Validation.isNullOrEmpty(visToValidate.getVisId())) {
            throw new MBPException(HttpStatus.NOT_FOUND, "An id of the visualization component is missing.");
        }

        // 0) Check if a field collection id is given
        if (Validation.isNullOrEmpty(visToValidate.getFieldCollectionId())) {
            throw new MBPException(HttpStatus.NOT_FOUND, "An id of the visualization field collection is missing.");
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
        Visualization vis = VisualizationCollection.visIdMapping.get(visToValidate.getVisId());
        boolean hasValid = false;
        for (VisualizationFields fields : vis.getFieldsToVisualize()) {
            if (!visToValidate.getVisFieldToPathMapping().keySet().equals(
                    VisualizationCollection.visIdMapping.get(fields.getFieldsToVisualize().keySet())))
            {
                hasValid = true;
            }
        }
        if (!hasValid) {
            throw new MBPException(HttpStatus.NOT_FOUND,
                    "For visual component with the id " + visToValidate.getVisId() + ", invalid visualization" +
                            "parameters were provided.");
        }

        // 4) Check if all value fields of the visual component map are valid json paths
        String jsonDataModelExample = component.getOperator().getDataModel().getJSONExample();
        try {
            JSONObject exampleObj = new JSONObject(jsonDataModelExample);
            for (PathUnitPair pathToValidate : visToValidate.getVisFieldToPathMapping().values()) {
                JsonPath testPath = JsonPath.compile(pathToValidate.getPath());
                testPath.read(exampleObj.getJSONObject("value").toString());
            }
        } catch (Exception e) {
            throw new MBPException(HttpStatus.NOT_ACCEPTABLE, "Invalid json path provided.");
        }

    }
}
