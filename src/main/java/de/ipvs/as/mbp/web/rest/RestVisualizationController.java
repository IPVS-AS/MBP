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
import de.ipvs.as.mbp.repository.discovery.DynamicDeploymentRepository;
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
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH + "/component-vis")
@Api(tags = {"Visualization"})
public class RestVisualizationController {

    @Autowired
    private ActuatorRepository actuatorRepository;

    @Autowired
    private SensorRepository sensorRepository;

    @Autowired
    private DynamicDeploymentRepository dynamicDeploymentRepository;

    @Autowired
    private UserEntityService userEntityService;

    @Autowired
    private ActiveVisualizationUpdater activeVisualizationUpdater;

    @PutMapping(value = "/{componentId}", produces = "application/hal+json")
    @ApiOperation(value = "Updates or creates the existing visualizations of a component.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Active visualizations successfully updated!"),
            @ApiResponse(code = 401, message = "Not authorized to access the sensor!"),
            @ApiResponse(code = 404, message = "Component or requesting user not found!")})
    public ResponseEntity<EntityModel<Component>> update(
            @PathVariable("componentId")
            @ApiParam(value = "The id of the sensor", example = "5f218c7822424828a8275037") String componentId,
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @RequestBody @ApiParam(value = "An ActiveVisualization instance", required = true) ActiveVisualization visSettings)
            throws EntityNotFoundException, MissingPermissionException {
        //Retrieve access request
        ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);

        //The component to update
        Component componentToUpdate;

        //Check repositories for a component with the given ID
        if (actuatorRepository.existsById(componentId)) {
            componentToUpdate = userEntityService.getForIdWithAccessControlCheck(actuatorRepository, componentId, ACAccessType.UPDATE, accessRequest);
        } else if (sensorRepository.existsById(componentId)) {
            componentToUpdate = userEntityService.getForIdWithAccessControlCheck(sensorRepository, componentId, ACAccessType.UPDATE, accessRequest);
        } else if (dynamicDeploymentRepository.existsById(componentId)) {
            componentToUpdate = userEntityService.getForIdWithAccessControlCheck(dynamicDeploymentRepository, componentId, ACAccessType.UPDATE, accessRequest);
        } else {
            throw new EntityNotFoundException("Component", componentId);
        }

        // Return the updated component
        return ResponseEntity.ok(
                userEntityService.entityToEntityModel(
                        activeVisualizationUpdater.updateOrCreateActiveVisualization(componentToUpdate, visSettings)
                ));
    }

    @DeleteMapping(path = "/{componentId}/{activeVisualComponentId}")
    @ApiOperation(value = "Deletes an existing visual component entity identified by its component and own id if " +
            "available for the requesting entity.")
    @ApiResponses({@ApiResponse(code = 204, message = "Success!"),
            @ApiResponse(code = 401, message = "Not authorized to delete the visual component!")})
    public ResponseEntity<Void> delete(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @PathVariable("componentId") String componentId,
            @PathVariable("activeVisualComponentId") String visualId) throws EntityNotFoundException, MissingPermissionException {
        //Retrieve access request
        ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);

        //The component to update
        Component componentToUpdate;

        //Check repositories for a component with the given ID
        if (actuatorRepository.existsById(componentId)) {
            componentToUpdate = userEntityService.getForIdWithAccessControlCheck(actuatorRepository, componentId, ACAccessType.UPDATE, accessRequest);
        } else if (sensorRepository.existsById(componentId)) {
            componentToUpdate = userEntityService.getForIdWithAccessControlCheck(sensorRepository, componentId, ACAccessType.UPDATE, accessRequest);
        } else if (dynamicDeploymentRepository.existsById(componentId)) {
            componentToUpdate = userEntityService.getForIdWithAccessControlCheck(dynamicDeploymentRepository, componentId, ACAccessType.UPDATE, accessRequest);
        } else {
            throw new EntityNotFoundException("Component", componentId);
        }

        //Delete visualization
        return activeVisualizationUpdater.deleteVisualComponent(componentToUpdate, visualId);
    }
}
