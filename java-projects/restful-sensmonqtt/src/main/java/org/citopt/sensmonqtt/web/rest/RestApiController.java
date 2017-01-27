package org.citopt.sensmonqtt.web.rest;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;
import org.citopt.sensmonqtt.RestConfiguration;
import org.citopt.sensmonqtt.domain.component.ActuatorValidator;
import org.citopt.sensmonqtt.domain.component.Sensor;
import org.citopt.sensmonqtt.domain.device.Device;
import org.citopt.sensmonqtt.domain.type.Type;
import org.citopt.sensmonqtt.repository.SensorRepository;
import org.citopt.sensmonqtt.service.ARPReader;
import org.citopt.sensmonqtt.service.ARPResult;
import org.citopt.sensmonqtt.service.NetworkService;
import org.citopt.sensmonqtt.service.SSHDeployer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryLinksResource;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author rafaelkperes
 */
@RestController
@ExposesResourceFor(ARPReader.class)
@RequestMapping(RestConfiguration.BASE_PATH)
public class RestApiController implements
        ResourceProcessor<RepositoryLinksResource> {

    @Autowired
    private NetworkService networkService;

    @Autowired
    private ARPReader arpReader;

    @Autowired
    private SSHDeployer sshDeployer;

    @Autowired
    private SensorRepository sensorRepository;

    @Autowired
    private ActuatorValidator actuatorRepository;

    @Override
    public RepositoryLinksResource process(RepositoryLinksResource resource) {
        resource.add(ControllerLinkBuilder
                .linkTo(ControllerLinkBuilder.
                        methodOn(RestApiController.class)
                        .arpTable())
                .withRel("arp"));

        return resource;
    }

    @RequestMapping(value = "/deploy/sensor/{id}", method = RequestMethod.GET)
    public ResponseEntity<Boolean> isRunningSensor(
            @PathVariable(value = "id") String id) {
        Sensor sensor = sensorRepository.findOne(id);

        if (sensor == null) {
            // Sensor not found
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }

        Device device = sensor.getDevice();

        if (device == null) {
            // Device not setted
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        String deviceIp = arpReader.getIp(device.getMacAddress());

        Boolean result;
        try {
            result = sshDeployer.isRunning(id,
                    // url, port, user, key
                    deviceIp, 22, SSHDeployer.DEFAULT_USER, SSHDeployer.KEY);
        } catch (IOException e) {
            // couldn't deploy - device not found or error during remote instructions
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity(result, HttpStatus.OK);
    }
    
    @RequestMapping(value = "/deploy/sensor/{id}", method = RequestMethod.POST, params = {})
    public ResponseEntity<String> deploySensor(
            @PathVariable(value = "id") String id) {
        return deploySensor(id, "", "");
    }

    @RequestMapping(value = "/deploy/sensor/{id}", method = RequestMethod.POST, params = {"component", "pinset"})
    public ResponseEntity<String> deploySensor(
            @PathVariable(value = "id") String id,
            @RequestParam String pinset, @RequestParam(name = "component") String component) {
        System.out.println("deploy");
        
        if(component == null) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        
        System.out.println(component + " " + pinset);
        
        Sensor sensor = sensorRepository.findOne(id);

        if (sensor == null) {
            // Sensor not found
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }

        Device device = sensor.getDevice();
        Type type = sensor.getType();

        if (device == null || type == null) {
            // Device or type not setted
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        String deviceIp = arpReader.getIp(device.getMacAddress());

        String serverIp;
        try {
            serverIp = networkService.getSelfIp();
        } catch (UnknownHostException e) {
            // Couldn't get own IP
            System.out.println("COULDN`T GET OWN IP");
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        try {
            sshDeployer.deploy(id,
                    // url, port, user, key
                    deviceIp, 22, SSHDeployer.DEFAULT_USER, SSHDeployer.KEY,
                    // mqttIp, type, component, pinset
                    serverIp, type, component, pinset);
        } catch (IOException e) {
            // couldn't deploy - device not found or error during remote instructions
            System.out.println("ERROR ON DEPLOY ACTUALLY");
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity(HttpStatus.CREATED);
    }

    @RequestMapping(value = "/deploy/sensor/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<String> undeploySensor(
            @PathVariable(value = "id") String id) {
        Sensor sensor = sensorRepository.findOne(id);

        if (sensor == null) {
            // Sensor not found
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }

        Device device = sensor.getDevice();

        if (device == null) {
            // Device not setted
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        String deviceIp = arpReader.getIp(device.getMacAddress());

        try {
            sshDeployer.undeploy(id,
                    // url, port, user, key
                    deviceIp, 22, SSHDeployer.DEFAULT_USER, SSHDeployer.KEY);
        } catch (IOException e) {
            // couldn't deploy - device not found or error during remote instructions
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @RequestMapping("/deploy/actuator/{id}")
    public ResponseEntity<String> deployActuator(
            @PathVariable(value = "id") String id
    ) {
        return new ResponseEntity(HttpStatus.CREATED);
    }

    @RequestMapping("/arp")
    public List<ARPResult> arpTable() {
        return arpReader.getTable();
    }

}
