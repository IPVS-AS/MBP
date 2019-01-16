package org.citopt.connde.web.rest;

import org.citopt.connde.RestConfiguration;
import org.citopt.connde.service.NetworkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for debugging.
 *
 * @author Jan
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH)
public class RestDebugController {

    @Autowired
    private NetworkService networkService;

    @RequestMapping(value = "/debug/ip", method = RequestMethod.GET)
    public ResponseEntity<String> getSettings() {
        String brokerIP = null;
        try {
            brokerIP = networkService.getOwnIPAddress();
        } catch (Exception e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<String>(brokerIP, HttpStatus.OK);
    }
}
