package de.ipvs.as.mbp.web.rest;

import de.ipvs.as.mbp.RestConfiguration;
import de.ipvs.as.mbp.domain.access_control.ACAccessRequest;
import de.ipvs.as.mbp.domain.access_control.ACAccessType;
import de.ipvs.as.mbp.domain.component.Sensor;
import de.ipvs.as.mbp.domain.visualization.ActiveVisualization;
import de.ipvs.as.mbp.error.EntityNotFoundException;
import de.ipvs.as.mbp.error.MBPException;
import de.ipvs.as.mbp.error.MissingPermissionException;
import de.ipvs.as.mbp.repository.SensorRepository;
import de.ipvs.as.mbp.service.UserEntityService;
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
    private UserEntityService userEntityService;

    @Autowired
    private ActiveVisualizationUpdater activeVisualizationUpdater;

    @PutMapping(value = "/{sensorId}", produces = "application/hal+json")
    @ApiOperation(value = "Updates or creates the existing visualizations of a sensor.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Sensors active visualizations successfully updated!"),
            @ApiResponse(code = 401, message = "Not authorized to access the sensor!"),
            @ApiResponse(code = 404, message = "Sensor to update or requesting user not found!")})
    public ResponseEntity<EntityModel<Sensor>> update(
            @PathVariable("sensorId")
            @ApiParam(value = "The id of the sensor", example = "5f218c7822424828a8275037") String sensorId,
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @RequestBody @ApiParam(value = "An ActiveComponentVisualization instance", required = true) ActiveVisualization visSettings)
            throws EntityNotFoundException, MissingPermissionException {

        System.out.println("Put test");

        // Check permission and whether sensor exists
        userEntityService.requirePermission(sensorRepository, sensorId, ACAccessType.UPDATE, ACAccessRequest.valueOf(accessRequestHeader));
        Sensor sensor = sensorRepository.findById(sensorId).orElse(null);

        if (sensor == null) {
            throw new MBPException(HttpStatus.NOT_FOUND,
                    "Sensor with the id" + sensorId + "' does not exist!");
        }

        // Return the updated sensor
        return ResponseEntity.ok(
                userEntityService.entityToEntityModel(
                        activeVisualizationUpdater.updateOrCreateActiveVisualization(sensor, visSettings)
                ));
    }

    @DeleteMapping(path = "/{sensorId}/{activeVisualComponentId}")
    @ApiOperation(value = "Deletes an existing visual component entity identified by its sensor and own id if " +
            "it's available for the requesting entity.")
    @ApiResponses({@ApiResponse(code = 204, message = "Success!"),
            @ApiResponse(code = 401, message = "Not authorized to delete the visual component!")})
    public ResponseEntity<Void> delete(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @PathVariable("sensorId") String sensorId,
            @PathVariable("activeVisualComponentId") String visualId) throws EntityNotFoundException, MissingPermissionException {
        // Delete the data model (includes access-control)
        // Check permission and whether sensor exists
        userEntityService.requirePermission(sensorRepository, sensorId, ACAccessType.UPDATE, ACAccessRequest.valueOf(accessRequestHeader));
        Sensor sensor = sensorRepository.findById(sensorId).orElse(null);

        if (sensor == null) {
            throw new MBPException(HttpStatus.NOT_FOUND,
                    "Sensor with the id" + sensorId + "' does not exist!");
        }

        ResponseEntity response = activeVisualizationUpdater.deleteVisualComponent(sensor, visualId);

        if (response != null) {
            return response;
        }
        return ResponseEntity.noContent().build();
    }


}
