package org.citopt.connde.web.rest;

import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.device.Device;
import org.citopt.connde.domain.monitoring.MonitoringAdapter;
import org.citopt.connde.repository.DeviceRepository;
import org.citopt.connde.repository.MonitoringAdapterRepository;
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
 * REST Controller that exposes methods for monitoring devices.
 *
 * @author Jan
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH)
public class RestMonitoringController {

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private MonitoringAdapterRepository monitoringAdapterRepository;

    /**
     * Returns a list of monitoring adapters that are compatible with a certain device.
     *
     * @param deviceId The id of the device for which the adapters are supposed to be retrieved
     * @return A list of all monitoring adapters that are compatible with the device
     */
    @GetMapping("/monitoring-adapters/by-device/{id}")
    public ResponseEntity<List<MonitoringAdapter>> getCompatibleMonitoringAdaptersForDevice(@PathVariable(value = "id") String deviceId) {
        //Validity check
        if((deviceId == null) || deviceId.isEmpty()){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        //Fetch device object
        Device device = deviceRepository.findOne(deviceId);

        //Get device type
        String deviceTyp = device.getComponentType();

        //Get all monitoring adapters
        List<MonitoringAdapter> allAdapters = monitoringAdapterRepository.findAll();

        //Create a list for all compatible adapters
        List<MonitoringAdapter> compatibleAdapterList = new ArrayList<>();

        //Iterate over all adapters and check for compatibility
        for(MonitoringAdapter adapter : allAdapters){
            if(adapter.isCompatibleWith(deviceTyp)){
                compatibleAdapterList.add(adapter);
            }
        }

        return new ResponseEntity<>(compatibleAdapterList, HttpStatus.OK);
    }
}
