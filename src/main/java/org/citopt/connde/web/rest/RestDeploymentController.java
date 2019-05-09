package org.citopt.connde.web.rest;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.adapter.Adapter;
import org.citopt.connde.domain.adapter.parameters.Parameter;
import org.citopt.connde.domain.adapter.parameters.ParameterInstance;
import org.citopt.connde.domain.adapter.parameters.ParameterTypes;
import org.citopt.connde.domain.component.Component;
import org.citopt.connde.repository.ActuatorRepository;
import org.citopt.connde.repository.ComponentRepository;
import org.citopt.connde.repository.SensorRepository;
import org.citopt.connde.service.deploy.SSHDeployer;
import org.citopt.connde.web.rest.response.ActionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryLinksResource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for deployment related REST requests.
 *
 * @author rafaelkperes, Jan
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH)
public class RestDeploymentController implements ResourceProcessor<RepositoryLinksResource> {

    @Autowired
    private ComponentDeploymentWrapper deploymentWrapper;

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

    @RequestMapping(value = "/deploy/actuator/{id}", method = RequestMethod.POST)
    public ResponseEntity<ActionResponse> deployActuator(@PathVariable(value = "id") String id,
                                                         @RequestBody List<ParameterInstance> parameters) {
        return deployComponent(id, actuatorRepository, parameters);
    }

    @RequestMapping(value = "/deploy/sensor/{id}", method = RequestMethod.POST)
    public ResponseEntity<ActionResponse> deploySensor(@PathVariable(value = "id") String id,
                                                       @RequestBody List<ParameterInstance> parameters) {
        return deployComponent(id, sensorRepository, parameters);
    }

    @RequestMapping(value = "/deploy/actuator/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<ActionResponse> undeployActuator(@PathVariable(value = "id") String id) {
        return undeployComponent(id, actuatorRepository);
    }

    @RequestMapping(value = "/deploy/sensor/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<ActionResponse> undeploySensor(@PathVariable(value = "id") String id) {
        return undeployComponent(id, sensorRepository);
    }

    private ResponseEntity isRunningComponent(String id, ComponentRepository repository) {
        //Retrieve component from repository
        Component component = (Component) repository.findOne(id);

        //Check if running
        return deploymentWrapper.isRunningComponent(component);
    }

    private ResponseEntity<ActionResponse> deployComponent(String id, ComponentRepository repository, List<ParameterInstance> parameterInstances) {
        //Retrieve component from repository
        Component component = (Component) repository.findOne(id);

        //Do deployment
        return deploymentWrapper.deployComponent(component, parameterInstances);
    }

    private ResponseEntity<ActionResponse> undeployComponent(String id, ComponentRepository repository) {
        //Retrieve component from repository
        Component component = (Component) repository.findOne(id);

        //Do undeployment
        return deploymentWrapper.undeployComponent(component);
    }

    @RequestMapping(value = "/adapter/parameter-types", method = RequestMethod.GET)
    public ResponseEntity<List<ParameterTypes>> getAllParameterTypes() {
        //Get all enum objects as list
        List<ParameterTypes> parameterList = Arrays.asList(ParameterTypes.values());
        return new ResponseEntity<>(parameterList, HttpStatus.OK);
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
