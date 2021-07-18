package de.ipvs.as.mbp.web.rest;

import de.ipvs.as.mbp.RestConfiguration;
import de.ipvs.as.mbp.domain.discovery.peripheral.DynamicPeripheral;
import de.ipvs.as.mbp.repository.discovery.DynamicPeripheralRepository;
import de.ipvs.as.mbp.service.discovery.engine.DiscoveryEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * REST Controller for debugging.
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH)
@ApiIgnore("Controller exists only for debugging purposes")
public class RestDebugController {

    @Autowired
    private DynamicPeripheralRepository dynamicPeripheralRepository;

    @Autowired
    private DiscoveryEngine discoveryEngine;

    /**
     * REST interface for debugging purposes. Feel free to implement your own debugging and testing stuff here,
     * but please clean up before committing.
     *
     * @return Debugging output specified by the developer
     */
    @RequestMapping(value = "/debug", method = RequestMethod.GET)
    public ResponseEntity<String> debug() throws ExecutionException, InterruptedException {
        List<DynamicPeripheral> dynamicPeripheralList = dynamicPeripheralRepository.findAll();

        if (dynamicPeripheralList.isEmpty()) return new ResponseEntity<>("debug", HttpStatus.OK);


        DynamicPeripheral peripheral = dynamicPeripheralList.get(0);

        peripheral.setEnablingIntended(false);
        dynamicPeripheralRepository.save(peripheral);

        discoveryEngine.activateDynamicPeripheral(peripheral.getId());

        return new ResponseEntity<>("debug", HttpStatus.OK);
    }
}
