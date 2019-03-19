package org.citopt.connde.web.rest;

import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.adapter.parameters.ParameterInstance;
import org.citopt.connde.domain.device.Device;
import org.citopt.connde.domain.monitoring.MonitoringAdapter;
import org.citopt.connde.domain.monitoring.MonitoringComponent;
import org.citopt.connde.repository.DeviceRepository;
import org.citopt.connde.repository.MonitoringAdapterRepository;
import org.citopt.connde.repository.projection.MonitoringAdapterListProjection;
import org.citopt.connde.web.rest.response.ActionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * REST Controller that exposes methods for the purpose of device monitoring.
 *
 * @author Jan
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH)
public class RestMonitoringController {

    @Autowired
    ComponentDeploymentWrapper deploymentWrapper;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private MonitoringAdapterRepository monitoringAdapterRepository;

    @GetMapping(value = "/monitoring/{deviceId}")
    public ResponseEntity<Boolean> isMonitoringActive(@PathVariable(value = "deviceId") String deviceId,
                                                      @RequestParam("adapter") String monitoringAdapterId) {
        //Validity check
        if ((deviceId == null) || deviceId.isEmpty() || (monitoringAdapterId == null) || monitoringAdapterId.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        //Create new monitoring component
        MonitoringComponent monitoringComponent = createComponent(deviceId, monitoringAdapterId);

        //Validity check
        if (monitoringComponent == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        //Do check
        return deploymentWrapper.isRunningComponent(monitoringComponent);
    }

    @PostMapping(value = "/monitoring/{deviceId}")
    public ResponseEntity<ActionResponse> enableMonitoring(@PathVariable(value = "deviceId") String deviceId,
                                                           @RequestParam("adapter") String monitoringAdapterId,
                                                           @RequestBody List<ParameterInstance> parameters) {
        //Validity check
        if ((deviceId == null) || deviceId.isEmpty() || (monitoringAdapterId == null) || monitoringAdapterId.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        //Create new monitoring component
        MonitoringComponent monitoringComponent = createComponent(deviceId, monitoringAdapterId);

        //Validity check
        if (monitoringComponent == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        //Deploy
        return deploymentWrapper.deployComponent(monitoringComponent, parameters);
    }

    @DeleteMapping(value = "/monitoring/{deviceId}")
    public ResponseEntity<ActionResponse> disableMonitoring(@PathVariable(value = "deviceId") String deviceId,
                                                            @RequestParam("adapter") String monitoringAdapterId) {
        //Validity check
        if ((deviceId == null) || deviceId.isEmpty() || (monitoringAdapterId == null) || monitoringAdapterId.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        //Create new monitoring component
        MonitoringComponent monitoringComponent = createComponent(deviceId, monitoringAdapterId);

        //Validity check
        if (monitoringComponent == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        //Undeploy
        return deploymentWrapper.undeployComponent(monitoringComponent);
    }

    private MonitoringComponent createComponent(String deviceId, String monitoringAdapterId) {
        //Retrieve corresponding device and adapter from their repositories
        Device device = deviceRepository.findOne(deviceId);
        MonitoringAdapter monitoringAdapter = monitoringAdapterRepository.findOne(monitoringAdapterId);

        //Check if both objects were found
        if ((device == null) || (monitoringAdapter == null)) {
            return null;
        }

        //Create new monitoring component (wrapper)
        MonitoringComponent monitoringComponent = new MonitoringComponent(monitoringAdapter, device);

        return monitoringComponent;
    }

    /**
     * Returns a list of monitoring adapters that are compatible with a certain device.
     *
     * @param deviceId The id of the device for which the adapters are supposed to be retrieved
     * @return A list of all monitoring adapters that are compatible with the device
     */
    @GetMapping("/monitoring-adapters/by-device/{id}")
    public ResponseEntity<List<MonitoringAdapterListProjection>> getCompatibleMonitoringAdaptersForDevice(@PathVariable(value = "id") String deviceId) {
        //Validity check
        if ((deviceId == null) || deviceId.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        //Fetch device object
        Device device = deviceRepository.findOne(deviceId);

        //Get device type
        String deviceTyp = device.getComponentType();

        //Get all monitoring adapters
        List<MonitoringAdapter> allAdapters = monitoringAdapterRepository.findAll();

        //Create a list for all compatible adapters
        List<MonitoringAdapterListProjection> compatibleAdapterList = new ArrayList<>();

        //Iterate over all adapters and check for compatibility
        for (MonitoringAdapter adapter : allAdapters) {
            if (adapter.isCompatibleWith(deviceTyp)) {
                //Get projection for this matching adapter and add it to the list
                MonitoringAdapterListProjection projection = monitoringAdapterRepository.findById(adapter.getId());
                compatibleAdapterList.add(projection);
            }
        }

        return new ResponseEntity<>(compatibleAdapterList, HttpStatus.OK);
    }
}
