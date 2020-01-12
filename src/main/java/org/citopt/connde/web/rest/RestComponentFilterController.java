package org.citopt.connde.web.rest;

import io.swagger.annotations.*;
import org.citopt.connde.RestConfiguration;
import org.citopt.connde.repository.ActuatorRepository;
import org.citopt.connde.repository.SensorRepository;
import org.citopt.connde.repository.projection.ComponentExcerpt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * REST Controller that exposes methods that allow the filtering for certain components, e.g. by adapter/device id.
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH)
@Api(tags = {"Component filter"}, description = "Retrieval of components that match given criteria")
public class RestComponentFilterController {

    @Autowired
    private ActuatorRepository actuatorRepository;

    @Autowired
    private SensorRepository sensorRepository;

    /**
     * Returns a list of components that make use of a certain adapter.
     *
     * @param adapterId The id of the adapter for which using components should be found
     * @return A list of all components that make use of the adapter
     */
    @GetMapping("/components/by-adapter/{id}")
    @ApiOperation(value = "Retrieves the components which make use of a certain adapter and for which the user is authorized", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success")})
    public ResponseEntity<List<ComponentExcerpt>> getComponentsByAdapterID(@PathVariable(value = "id") @ApiParam(value = "ID of the adapter", example = "5c97dc2583aeb6078c5ab672", required = true) String adapterId) {
        //Retrieve actuator and sensor excerpts by adapter id
        List<ComponentExcerpt> actuatorExcerpts = actuatorRepository.findAllByAdapterId(adapterId);
        List<ComponentExcerpt> sensorExcerpts = sensorRepository.findAllByAdapterId(adapterId);

        //Merge both lists and filter by read permission
        List<ComponentExcerpt> componentList = filterComponentExcerpts(actuatorExcerpts, sensorExcerpts);

        return new ResponseEntity<>(componentList, HttpStatus.OK);
    }

    /**
     * Returns a list of components that make use of a certain device.
     *
     * @param deviceId The id of the device for which using components should be found
     * @return A list of all components that make use of the device
     */
    @GetMapping("/components/by-device/{id}")
    @ApiOperation(value = "Retrieves the components which make use of a certain device and for which the user is authorized", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success")})
    public ResponseEntity<List<ComponentExcerpt>> getComponentsByDeviceID(@PathVariable(value = "id") @ApiParam(value = "ID of the device", example = "5c97dc2583aeb6078c5ab672", required = true) String deviceId) {
        //Retrieve actuator and sensor excerpts by device id
        List<ComponentExcerpt> actuatorExcerpts = actuatorRepository.findAllByDeviceId(deviceId);
        List<ComponentExcerpt> sensorExcerpts = sensorRepository.findAllByDeviceId(deviceId);

        //Merge both lists and filter by read permission
        List<ComponentExcerpt> componentList = filterComponentExcerpts(actuatorExcerpts, sensorExcerpts);

        return new ResponseEntity<>(componentList, HttpStatus.OK);
    }

    /**
     * Takes a list of actuator excerpts and a list of sensor excerpts and returns a merged list that only contains
     * the component excerpts for which the user has the read permission.
     *
     * @param actuatorExcerpts The list of actuator excerpts
     * @param sensorExcerpts   The list of sensor excerpts
     * @return The merged and filtered list of component excerpts
     */
    private List<ComponentExcerpt> filterComponentExcerpts(List<ComponentExcerpt> actuatorExcerpts, List<ComponentExcerpt> sensorExcerpts) {
        //Create empty return list
        List<ComponentExcerpt> componentList = new ArrayList<>();

        //Filter actuator excerpts
        for (ComponentExcerpt actuatorExcerpt : actuatorExcerpts) {
            //Perform permission check
            if (actuatorRepository.get(actuatorExcerpt.getId()).isReadable()) {
                componentList.add(actuatorExcerpt);
            }
        }

        //Filter sensor excerpts
        for (ComponentExcerpt sensorExcerpt : sensorExcerpts) {
            //Perform permission check
            if (sensorRepository.get(sensorExcerpt.getId()).isReadable()) {
                componentList.add(sensorExcerpt);
            }
        }

        return componentList;
    }
}
