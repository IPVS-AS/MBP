package de.ipvs.as.mbp.web.rest;

import de.ipvs.as.mbp.RestConfiguration;
import de.ipvs.as.mbp.domain.discovery.device.DeviceTemplate;
import de.ipvs.as.mbp.domain.discovery.device.requirements.location.LocationRequirement;
import de.ipvs.as.mbp.domain.discovery.device.requirements.location.LocationRequirementOperator;
import de.ipvs.as.mbp.domain.discovery.location.LocationTemplate;
import de.ipvs.as.mbp.repository.discovery.DeviceTemplateRepository;
import de.ipvs.as.mbp.repository.discovery.LocationTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Arrays;
import java.util.List;

/**
 * REST Controller for debugging.
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH)
@ApiIgnore("Controller exists only for debugging purposes")
public class RestDebugController {

    @Autowired
    private LocationTemplateRepository locationTemplateRepository;

    @Autowired
    private DeviceTemplateRepository deviceTemplateRepository;

    /**
     * REST interface for debugging purposes. Feel free to implement your own debugging and testing stuff here,
     * but please clean up before committing.
     *
     * @return Debugging output specified by the developer
     */
    @RequestMapping(value = "/debug", method = RequestMethod.GET)
    public ResponseEntity<String> debug() {
        return new ResponseEntity<>("debug", HttpStatus.OK);
    }
}
