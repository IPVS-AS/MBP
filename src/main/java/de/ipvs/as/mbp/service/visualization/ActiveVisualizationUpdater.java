package de.ipvs.as.mbp.service.visualization;

import de.ipvs.as.mbp.domain.component.Actuator;
import de.ipvs.as.mbp.domain.component.Component;
import de.ipvs.as.mbp.domain.component.Sensor;
import de.ipvs.as.mbp.domain.discovery.deployment.DynamicDeployment;
import de.ipvs.as.mbp.domain.visualization.repo.ActiveVisualization;
import de.ipvs.as.mbp.error.MBPException;
import de.ipvs.as.mbp.repository.ActuatorRepository;
import de.ipvs.as.mbp.repository.SensorRepository;
import de.ipvs.as.mbp.repository.discovery.DynamicDeploymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Provides access operations to the database for
 * managing the visualization views for single components.
 */
@Service
public class ActiveVisualizationUpdater {

    @Autowired
    private ActuatorRepository actuatorRepository;

    @Autowired
    private SensorRepository sensorRepository;

    @Autowired
    private DynamicDeploymentRepository dynamicDeploymentRepository;


    /**
     * Updates the currently active visualization settings for one component persistent to the database.
     * If the {@link ActiveVisualization#getInstanceId()} is already used for the sensor, the
     * visualization settings for this visualization will be updated. Else, a new ActiveComponentVisualization
     * entry will be added to the sensor with a new uniquely generated id.
     *
     * @param componentToUpdate   The sensor of which the visualization fields should be updated.
     * @param visToCreateOrUpdate The {@link ActiveVisualization} which should be edited or created new.
     * @return The sensor which was updated.
     */
    public Component updateOrCreateActiveVisualization(Component componentToUpdate, ActiveVisualization visToCreateOrUpdate) {

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

        // Apply the changes to the respective repository
        if (componentToUpdate instanceof Actuator) {
            actuatorRepository.save((Actuator) componentToUpdate);
        } else if (componentToUpdate instanceof Sensor) {
            sensorRepository.save((Sensor) componentToUpdate);
        } else if (componentToUpdate instanceof DynamicDeployment) {
            dynamicDeploymentRepository.save((DynamicDeployment) componentToUpdate);
        } else {
            System.err.println("Error while saving visualization changes to repository.");
        }
        return componentToUpdate;
    }

    /**
     * Deletes a visual component by the affected component and the visual
     * component id. Applies the changes to the database.
     *
     * @param component
     * @param visualComponentId
     */
    public ResponseEntity<Void> deleteVisualComponent(Component component, String visualComponentId) {
        for (ActiveVisualization vis : component.getActiveVisualizations()) {
            if (vis.getInstanceId().equals(visualComponentId)) {
                component.removeActiveVisualization(visualComponentId);

                // Apply the changes to the respective repository
                if (component instanceof Actuator) {
                    actuatorRepository.save((Actuator) component);
                } else if (component instanceof Sensor) {
                    sensorRepository.save((Sensor) component);
                } else if (component instanceof DynamicDeployment) {
                    dynamicDeploymentRepository.save((DynamicDeployment) component);
                } else {
                    System.err.println("Error while saving visualization changes to repository.");
                }
                return ResponseEntity.ok().build();
            }
        }

        throw new MBPException(HttpStatus.NOT_FOUND, "No visual component with the id " + visualComponentId + " found!");
    }

}
