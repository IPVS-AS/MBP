package de.ipvs.as.mbp.web.rest;

import de.ipvs.as.mbp.RestConfiguration;
import de.ipvs.as.mbp.domain.discovery.deployment.DynamicDeployment;
import de.ipvs.as.mbp.repository.discovery.DynamicDeploymentRepository;
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
    private DynamicDeploymentRepository dynamicDeploymentRepository;

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
        List<DynamicDeployment> dynamicDeploymentList = dynamicDeploymentRepository.findAll();

        if (dynamicDeploymentList.isEmpty()) return new ResponseEntity<>("debug", HttpStatus.OK);


        DynamicDeployment deployment = dynamicDeploymentList.get(0);

        discoveryEngine.activateDynamicDeployment(deployment.getId());

        return new ResponseEntity<>("debug", HttpStatus.OK);
    }
}
