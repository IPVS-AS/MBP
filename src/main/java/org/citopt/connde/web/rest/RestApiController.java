package org.citopt.connde.web.rest;

import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.component.Actuator;
import org.citopt.connde.domain.component.Sensor;
import org.citopt.connde.repository.ActuatorRepository;
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

    @Override
    public RepositoryLinksResource process(RepositoryLinksResource resource) {
        return resource;
    }

    @RequestMapping(value = "/deploy/sensor/{id}", method = RequestMethod.GET)
    public ResponseEntity<Boolean> isRunningSensor(@PathVariable(value = "id") String id) {
        //Retrieve sensor from repository
        Sensor sensor = sensorRepository.findOne(id);

        // Sensor not found?
        if (sensor == null) {
            return new ResponseEntity<Boolean>(HttpStatus.NOT_FOUND);
        }

        //Determine component status
        Boolean result;
        try {
            result = sshDeployer.isComponentRunning(sensor);
        } catch (IOException e) {
            return new ResponseEntity<Boolean>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<Boolean>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/deploy/sensor/{id}", method = RequestMethod.POST, params = {})
    public ResponseEntity<String> deploySensor(
            @PathVariable(value = "id") String id) {
        //Retrieve sensor from repository
        Sensor sensor = sensorRepository.findOne(id);

        // Sensor not found?
        if (sensor == null) {
            return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
        }

        //Deploy component
        try {
            sshDeployer.deployComponent(sensor);
        } catch (IOException e) {
            return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<String>(HttpStatus.CREATED);
    }

    @RequestMapping(value = "/deploy/sensor/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<String> undeploySensor(
            @PathVariable(value = "id") String id) {
        //Retrieve sensor from repository
        Sensor sensor = sensorRepository.findOne(id);

        //Sensor not found?
        if (sensor == null) {
            return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
        }

        //Undeploy sensor
        try {
            sshDeployer.undeployComponent(sensor);
        } catch (IOException e) {
            return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<String>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = "/deploy/actuator/{id}", method = RequestMethod.GET)
    public ResponseEntity<Boolean> isRunningActuator(@PathVariable(value = "id") String id) {
        //Retrieve actuator from repository
        Actuator actuator = actuatorRepository.findOne(id);

        //Actuator not found?
        if (actuator == null) {
            return new ResponseEntity<Boolean>(HttpStatus.NOT_FOUND);
        }

        //Determine component status
        Boolean result;
        try {
            result = sshDeployer.isComponentRunning(actuator);
        } catch (IOException e) {
            return new ResponseEntity<Boolean>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<Boolean>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/deploy/actuator/{id}", method = RequestMethod.POST, params = {})
    public ResponseEntity<String> deployActuator(@PathVariable(value = "id") String id) {
        //Retrieve actuator from repository
        Actuator actuator = actuatorRepository.findOne(id);

        //Actuator not found?
        if (actuator == null) {
            return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
        }

        //Deploy actuator
        try {
            sshDeployer.deployComponent(actuator);
        } catch (IOException e) {
            return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<String>(HttpStatus.CREATED);
    }

    @RequestMapping(value = "/deploy/actuator/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<String> undeployActuator(
            @PathVariable(value = "id") String id) {
        //Retrieve actuator from repository
        Actuator actuator = actuatorRepository.findOne(id);

        //Actuator not found?
        if (actuator == null) {
            return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
        }

        //Undeploy actuator
        try {
            sshDeployer.undeployComponent(actuator);
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

}
