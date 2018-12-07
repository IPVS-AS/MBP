package org.citopt.connde.web.rest;

import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.component.Component;
import org.citopt.connde.repository.ActuatorRepository;
import org.citopt.connde.repository.ComponentRepository;
import org.citopt.connde.repository.SensorRepository;
import org.citopt.connde.service.deploy.SSHDeployer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryLinksResource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Controller for the REST interface of the MBP that invokes deployment methods for the corresponding REST requests.
 *
 * @author rafaelkperes
 *         <p>
 *         Refactored by Jan on 03.12.2018.
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH)
public class RestApiController implements ResourceProcessor<RepositoryLinksResource> {

    @Autowired
    private SSHDeployer sshDeployer;

    @Autowired
    private SensorRepository sensorRepository;

    @Autowired
    private ActuatorRepository actuatorRepository;


    @RequestMapping(value = "/deploy/actuator/{id}", method = RequestMethod.GET)
    public ResponseEntity<Boolean> isRunningActuator(@PathVariable(value = "id") String id) {
        return isRunningComponent(id, actuatorRepository);
    }

    @RequestMapping(value = "/deploy/sensor/{id}", method = RequestMethod.GET)
    public ResponseEntity<Boolean> isRunningSensor(@PathVariable(value = "id") String id) {
        return isRunningComponent(id, sensorRepository);
    }

    @RequestMapping(value = "/deploy/actuator/{id}", method = RequestMethod.POST, params = {})
    public ResponseEntity<String> deployActuator(@PathVariable(value = "id") String id) {
        return deployComponent(id, actuatorRepository);
    }

    @RequestMapping(value = "/deploy/sensor/{id}", method = RequestMethod.POST, params = {})
    public ResponseEntity<String> deploySensor(@PathVariable(value = "id") String id) {
        return deployComponent(id, sensorRepository);
    }

    @RequestMapping(value = "/deploy/actuator/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<String> undeployActuator(@PathVariable(value = "id") String id) {
        return undeployComponent(id, actuatorRepository);
    }

    @RequestMapping(value = "/deploy/sensor/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<String> undeploySensor(@PathVariable(value = "id") String id) {
        return undeployComponent(id, sensorRepository);
    }

    private ResponseEntity isRunningComponent(String id, ComponentRepository repository) {
        //Retrieve component from repository
        Component component = (Component) repository.findOne(id);

        //Component not found?
        if (component == null) {
            return new ResponseEntity<Boolean>(HttpStatus.NOT_FOUND);
        }

        //Determine component status
        Boolean result;
        try {
            result = sshDeployer.isComponentRunning(component);
        } catch (IOException e) {
            return new ResponseEntity<Boolean>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<Boolean>(result, HttpStatus.OK);
    }

    private ResponseEntity<String> deployComponent(String id, ComponentRepository repository) {
        //Retrieve component from repository
        Component component = (Component) repository.findOne(id);

        //Component not found?
        if (component == null) {
            return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
        }

        //Deploy component
        try {
            sshDeployer.deployComponent(component);
        } catch (IOException e) {
            return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<String>(HttpStatus.CREATED);
    }

    private ResponseEntity<String> undeployComponent(String id, ComponentRepository repository) {
        //Retrieve component from repository
        Component component = (Component) repository.findOne(id);

        //Component not found?
        if (component == null) {
            return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
        }

        //Undeploy component
        try {
            sshDeployer.undeployComponent(component);
        } catch (IOException e) {
            return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<String>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping("/time")
    public ResponseEntity<String> serverDatetime() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return new ResponseEntity<>(strDate, HttpStatus.OK);
    }

    @Override
    public RepositoryLinksResource process(RepositoryLinksResource resource) {
        return resource;
    }
}
