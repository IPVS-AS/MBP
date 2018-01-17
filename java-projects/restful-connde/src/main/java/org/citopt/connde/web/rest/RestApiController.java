package org.citopt.connde.web.rest;

import java.io.IOException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.component.Actuator;
import org.citopt.connde.domain.component.Sensor;
import org.citopt.connde.domain.device.Device;
import org.citopt.connde.domain.type.Type;
import org.citopt.connde.repository.ActuatorRepository;
import org.citopt.connde.repository.DeviceRepository;
import org.citopt.connde.repository.SensorRepository;
import org.citopt.connde.service.NetworkService;
import org.citopt.connde.service.SSHDeployer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryLinksResource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author rafaelkperes
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH)
public class RestApiController implements ResourceProcessor<RepositoryLinksResource> {

    //private static final Logger LOGGER = Logger.getLogger(RestApiController.class.getName());

    @Autowired
    private NetworkService networkService;

    @Autowired
    private SSHDeployer sshDeployer;

    @Autowired
    private SensorRepository sensorRepository;

    @Autowired
    private ActuatorRepository actuatorRepository;

    @Autowired
    private DeviceRepository addressRepository;

    @Override
    public RepositoryLinksResource process(RepositoryLinksResource resource) {
//        resource.add(ControllerLinkBuilder
//                .linkTo(ControllerLinkBuilder.
//                        methodOn(RestApiController.class)
//                        .serverDatetime())
//                .withRel("time"));

        return resource;
    }

    @RequestMapping(value = "/deploy/sensor/{id}", method = RequestMethod.GET)
    public ResponseEntity<Boolean> isRunningSensor(@PathVariable(value = "id") String id) {
        Sensor sensor = sensorRepository.findOne(id);

        if (sensor == null) {
            // Sensor not found
            return new ResponseEntity<Boolean>(HttpStatus.NOT_FOUND);
        }

        Device device = sensor.getDevice();

        if (device == null) {
            // Device not setted
            return new ResponseEntity<Boolean>(HttpStatus.BAD_REQUEST);
        }
        
        Device deviceInRepo = addressRepository.findByMacAddress(device.getMacAddress());
        if (deviceInRepo == null) {
            // Device not setted
            return new ResponseEntity<Boolean>(HttpStatus.BAD_REQUEST);
        }        
        String deviceIp = deviceInRepo.getIpAddress();

        Boolean result;
        try {
            result = sshDeployer.isRunning(id,
                    // url, port, user, key
                    deviceIp, SSHDeployer.SSH_PORT, SSHDeployer.DEFAULT_USER, SSHDeployer.KEY);
        } catch (IOException e) {
            // couldn't deploy - device not found or error during remote instructions
            return new ResponseEntity<Boolean>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<Boolean>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/deploy/sensor/{id}", method = RequestMethod.POST, params = {})
    public ResponseEntity<String> deploySensor(@PathVariable(value = "id") String id) {
        return deploySensor(id, "", "SENSOR");
    }

    @RequestMapping(value = "/deploy/sensor/{id}", method = RequestMethod.POST, params = {"component", "pinset"})
    public ResponseEntity<String> deploySensor(
            @PathVariable(value = "id") String id,
            @RequestParam String pinset, @RequestParam(name = "component") String component) {
        System.out.println("deploy");

        if (component == null) {
            return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
        }

        Sensor sensor = sensorRepository.findOne(id);

        if (sensor == null) {
            // Sensor not found
            return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
        }

        Device device = sensor.getDevice();
        Type type = sensor.getType();

        if (device == null || type == null) {
            // Device or type not setted
            return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
        }
        
        Device deviceInRepo = addressRepository.findByMacAddress(device.getMacAddress());
        if (deviceInRepo == null) {
            // Device not setted
            return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
        }        
        String deviceIp = deviceInRepo.getIpAddress();

        String serverIp;
        serverIp = networkService.getMQTTBrokerIP();
		//serverIp = networkService.getPublicIP();
		System.out.println("MBP IP: " + serverIp);

        try {
            sshDeployer.deploy(id,
                    // url, port, user, key
                    deviceIp, SSHDeployer.SSH_PORT, SSHDeployer.DEFAULT_USER, SSHDeployer.KEY,
                    // mqttIp, type, component, pinset
                    serverIp, type, component, pinset);
        } catch (IOException e) {
            // couldn't deploy - device not found or error during remote instructions
            System.out.println("ERROR ON DEPLOYMENT");
            return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<String>(HttpStatus.CREATED);
    }

    @RequestMapping(value = "/deploy/sensor/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<String> undeploySensor(
            @PathVariable(value = "id") String id) {
        Sensor sensor = sensorRepository.findOne(id);

        if (sensor == null) {
            // Sensor not found
            return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
        }

        Device device = sensor.getDevice();

        if (device == null) {
            // Device not setted
            return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
        }
        Device deviceInRepo = addressRepository.findByMacAddress(device.getMacAddress());
        if (deviceInRepo == null) {
            // Device not setted
            return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
        }        
        String deviceIp = deviceInRepo.getIpAddress();

        try {
            sshDeployer.undeploy(id,
                    // url, port, user, key
                    deviceIp, SSHDeployer.SSH_PORT, SSHDeployer.DEFAULT_USER, SSHDeployer.KEY);
        } catch (IOException e) {
            // couldn't deploy - device not found or error during remote instructions
            return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<String>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = "/deploy/actuator/{id}", method = RequestMethod.GET)
    public ResponseEntity<Boolean> isRunningActuator(@PathVariable(value = "id") String id) {
        Actuator actuator = actuatorRepository.findOne(id);

        if (actuator == null) {
            // Sensor not found
            return new ResponseEntity<Boolean>(HttpStatus.NOT_FOUND);
        }

        Device device = actuator.getDevice();

        if (device == null) {
            // Device not setted
            return new ResponseEntity<Boolean>(HttpStatus.BAD_REQUEST);
        }
        Device deviceInRepo = addressRepository.findByMacAddress(device.getMacAddress());
        if (deviceInRepo == null) {
            // Device not setted
            return new ResponseEntity<Boolean>(HttpStatus.BAD_REQUEST);
        }        
        String deviceIp = deviceInRepo.getIpAddress();

        Boolean result;
        try {
            result = sshDeployer.isRunning(id,
                    // url, port, user, key
                    deviceIp, SSHDeployer.SSH_PORT, SSHDeployer.DEFAULT_USER, SSHDeployer.KEY);
        } catch (IOException e) {
            // couldn't deploy - device not found or error during remote instructions
            return new ResponseEntity<Boolean>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<Boolean>(result, HttpStatus.OK);
    }
    
    @RequestMapping(value = "/deploy/actuator/{id}", method = RequestMethod.POST, params = {})
    public ResponseEntity<String> deployActuator(@PathVariable(value = "id") String id) {
        return deployActuator(id, "", "ACTUATOR");
    }

    @RequestMapping(value = "/deploy/actuator/{id}", method = RequestMethod.POST, params = {"component", "pinset"})
    public ResponseEntity<String> deployActuator(@PathVariable(value = "id") String id, @RequestParam String pinset, @RequestParam(name = "component") String component) {
        System.out.println("deploy");

        if (component == null) {
            return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
        }

        Actuator actuator = actuatorRepository.findOne(id);

        if (actuator == null) {
            // Actuator not found
            return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
        }

        Device device = actuator.getDevice();
        Type type = actuator.getType();

        if (device == null || type == null) {
            // Device or type not setted
            return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
        }
        Device deviceInRepo = addressRepository.findByMacAddress(device.getMacAddress());
        if (deviceInRepo == null) {
            // Device not setted
            return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
        }        
        String deviceIp = deviceInRepo.getIpAddress();

        String serverIp;
        serverIp = networkService.getMQTTBrokerIP();
		//serverIp = networkService.getPublicIP();
		System.out.println("MBP IP: " + serverIp);

        try {
            sshDeployer.deploy(id,
                    // url, port, user, key
                    deviceIp, SSHDeployer.SSH_PORT, SSHDeployer.DEFAULT_USER, SSHDeployer.KEY,
                    // mqttIp, type, component, pinset
                    serverIp, type, component, pinset);
        } catch (IOException e) {
            // couldn't deploy - device not found or error during remote instructions
            System.out.println("ERROR ON DEPLOYMENT");
            return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<String>(HttpStatus.CREATED);
    }
    
    @Deprecated
    @RequestMapping(value = "/autodeploy", method = RequestMethod.POST)
    public ResponseEntity<String> autodeploy(@RequestBody Device address) {
        Device actualAddr = addressRepository.findByMacAddress(address.getMacAddress());

        if (actualAddr == null) {
            return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
        }

        String serverIp;
        try {
            serverIp = networkService.getSelfIp();
        } catch (UnknownHostException e) {
            // Couldn't get own IP
            System.out.println("COULDN`T GET OWN IP");
            return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        try {
            sshDeployer.autodeploy(
                    // url, port, user, key
                    actualAddr, SSHDeployer.SSH_PORT, SSHDeployer.DEFAULT_USER, SSHDeployer.KEY,
                    // mqttIp
                    serverIp
            );
        } catch (Exception e) {
            // Couldn't get own IP
            System.out.println("AUTODEPLOY ERROR");
            return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(HttpStatus.CREATED);
        
    }

    @RequestMapping("/time")
    public ResponseEntity<String> serverDatetime() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return new ResponseEntity<>(strDate, HttpStatus.OK);
    }

}
