package de.ipvs.as.mbp.web.rest;

import de.ipvs.as.mbp.RestConfiguration;
import de.ipvs.as.mbp.domain.access_control.ACAccessRequest;
import de.ipvs.as.mbp.domain.access_control.ACAccessType;
import de.ipvs.as.mbp.domain.component.Actuator;
import de.ipvs.as.mbp.domain.component.Component;
import de.ipvs.as.mbp.domain.component.Sensor;
import de.ipvs.as.mbp.domain.visualization.repo.ActiveVisualization;
import de.ipvs.as.mbp.error.EntityNotFoundException;
import de.ipvs.as.mbp.error.MBPException;
import de.ipvs.as.mbp.error.MissingPermissionException;
import de.ipvs.as.mbp.repository.ActuatorRepository;
import de.ipvs.as.mbp.repository.ComponentRepository;
import de.ipvs.as.mbp.repository.SensorRepository;
import de.ipvs.as.mbp.service.user.UserEntityService;
import de.ipvs.as.mbp.service.visualization.ActiveVisualizationUpdater;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing the visualization views of
 * components.
 * <p>
 * TODO enable the same functionality for other components (other than sensors).
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH + "/component-vis")
@Api(tags = {"Visualization"})
public class RestVisualizationController {

    @Autowired
    private SensorRepository sensorRepository;

    @Autowired
    private ActuatorRepository actuatorRepository;

    @Autowired
    private UserEntityService userEntityService;

    @Autowired
    private ActiveVisualizationUpdater activeVisualizationUpdater;

    @PutMapping(value = "/{componentId}", produces = "application/hal+json")
    @ApiOperation(value = "Updates or creates the existing visualizations of a component.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Sensors active visualizations successfully updated!"),
            @ApiResponse(code = 401, message = "Not authorized to access the sensor!"),
            @ApiResponse(code = 404, message = "Sensor to update or requesting user not found!")})
    public ResponseEntity<EntityModel<Component>> update(
            @PathVariable("componentId")
            @ApiParam(value = "The id of the sensor", example = "5f218c7822424828a8275037") String componentId,
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @RequestBody @ApiParam(value = "An ActiveComponentVisualization instance", required = true) ActiveVisualization visSettings)
            throws EntityNotFoundException, MissingPermissionException {

        Sensor sensor = null;
        Actuator actuator = null;
        try {
            // Check permission and whether sensor with this component id exists
            userEntityService.requirePermission(sensorRepository, componentId, ACAccessType.UPDATE, ACAccessRequest.valueOf(accessRequestHeader));
            sensor = sensorRepository.findById(componentId).orElse(null);
        } catch (EntityNotFoundException e) {
            // Catch this exception as these cases are already handled seperately in the following code
        }

        try {
            // Check permission and whether actuator with this component id exists
            userEntityService.requirePermission(actuatorRepository, componentId, ACAccessType.UPDATE, ACAccessRequest.valueOf(accessRequestHeader));
            actuator = actuatorRepository.findById(componentId).orElse(null);
        } catch (EntityNotFoundException e) {
            // Catch this exception as these cases are handled seperately in the following code
        }

        Component componentToUpdate = null;
        if (sensor != null) {
            componentToUpdate = sensor;
        } else if (actuator != null) {
            componentToUpdate = actuator;
        } else {
            throw new MBPException(HttpStatus.NOT_FOUND,
                    "Component with the id" + componentId + "' does not exist!");
        }

        // Return the updated sensor
        return ResponseEntity.ok(
                userEntityService.entityToEntityModel(
                        activeVisualizationUpdater.updateOrCreateActiveVisualization(componentToUpdate, visSettings)
                ));
    }

    @DeleteMapping(path = "/{componentId}/{activeVisualComponentId}")
    @ApiOperation(value = "Deletes an existing visual component entity identified by its sensor and own id if " +
            "it's available for the requesting entity.")
    @ApiResponses({@ApiResponse(code = 204, message = "Success!"),
            @ApiResponse(code = 401, message = "Not authorized to delete the visual component!")})
    public ResponseEntity<Void> delete(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @PathVariable("componentId") String componentId,
            @PathVariable("activeVisualComponentId") String visualId) throws EntityNotFoundException, MissingPermissionException {
        // Delete the data model (includes access-control)
        Sensor sensor = null;
        Actuator actuator = null;
        try {
            // Check permission and whether sensor with this component id exists
            userEntityService.requirePermission(sensorRepository, componentId, ACAccessType.UPDATE, ACAccessRequest.valueOf(accessRequestHeader));
            sensor = sensorRepository.findById(componentId).orElse(null);
        } catch (EntityNotFoundException e) {
            // Catch this exception as these cases are handled seperately in the following code
        }

        try {
            // Check permission and whether actuator with this component id exists
            userEntityService.requirePermission(actuatorRepository, componentId, ACAccessType.UPDATE, ACAccessRequest.valueOf(accessRequestHeader));
            actuator = actuatorRepository.findById(componentId).orElse(null);
        } catch (EntityNotFoundException e) {
            // Catch this exception as these cases are handled seperately in the following code
        }

        Component componentToUpdate = null;
        if (sensor != null) {
            componentToUpdate = sensor;
        } else if (actuator != null) {
            componentToUpdate = actuator;
        } else {
            throw new MBPException(HttpStatus.NOT_FOUND,
                    "Component with the id" + componentId + "' does not exist!");
        }

        ResponseEntity response = activeVisualizationUpdater.deleteVisualComponent(componentToUpdate, visualId);

        if (response != null) {
            return response;
        }
        return ResponseEntity.noContent().build();
    }
}
