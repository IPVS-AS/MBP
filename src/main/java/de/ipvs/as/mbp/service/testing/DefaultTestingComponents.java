package de.ipvs.as.mbp.service.testing;

import de.ipvs.as.mbp.domain.component.Actuator;
import de.ipvs.as.mbp.domain.component.ComponentCreateValidator;
import de.ipvs.as.mbp.domain.component.Sensor;
import de.ipvs.as.mbp.domain.device.Device;
import de.ipvs.as.mbp.domain.device.DeviceCreateValidator;
import de.ipvs.as.mbp.domain.operator.Code;
import de.ipvs.as.mbp.domain.operator.Operator;
import de.ipvs.as.mbp.domain.operator.parameters.Parameter;
import de.ipvs.as.mbp.domain.operator.parameters.ParameterType;
import de.ipvs.as.mbp.repository.ActuatorRepository;
import de.ipvs.as.mbp.repository.DeviceRepository;
import de.ipvs.as.mbp.repository.OperatorRepository;
import de.ipvs.as.mbp.repository.SensorRepository;
import de.ipvs.as.mbp.web.rest.helper.DeploymentWrapper;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;


@Service
public class DefaultTestingComponents {

    @Autowired
    private List<String> defaultTestComponentsWhiteList;
    @Autowired
    private ServletContext servletContext;

    @Autowired
    private OperatorRepository operatorRepository;

    @Autowired
    private ActuatorRepository actuatorRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private SensorRepository sensorRepository;

    @Autowired
    private DeviceCreateValidator deviceCreateValidator;

    @Autowired
    private PropertiesService propertiesService;

    @Autowired
    private ComponentCreateValidator componentCreateValidator;

    @Autowired
    private DeploymentWrapper deploymentWrapper;

    @Autowired
    private TestEngine testEngine;


    private final String TEST_DEVICE;
    private final String TEST_DEVICE_IP;
    private final String TEST_DEVICE_USERNAME;
    private final String TEST_DEVICE_PASSWORD;
    private final String ACTUATOR_NAME;
    private final List<String> SENSOR_SIMULATORS = Arrays.asList("TESTING_TemperatureSensor", "TESTING_TemperatureSensorPl", "TESTING_HumiditySensor", "TESTING_HumiditySensorPl");


    public DefaultTestingComponents(List<String> defaultTestComponentsWhiteList, ServletContext servletContext, OperatorRepository operatorRepository, DeviceRepository deviceRepository, DeviceCreateValidator deviceCreateValidator, ActuatorRepository actuatorRepository, ComponentCreateValidator componentCreateValidator, SensorRepository sensorRepository, DeploymentWrapper deploymentWrapper) throws IOException {
        propertiesService = new PropertiesService();
        TEST_DEVICE = propertiesService.getPropertiesString("testingTool.testDeviceName");
        TEST_DEVICE_IP = propertiesService.getPropertiesString("testingTool.ipAddressTestDevice");
        TEST_DEVICE_USERNAME = propertiesService.getPropertiesString("testingTool.testDeviceUserName");
        TEST_DEVICE_PASSWORD = propertiesService.getPropertiesString("testingTool.testDevicePassword");
        ACTUATOR_NAME = propertiesService.getPropertiesString("testingTool.actuatorName");
        this.defaultTestComponentsWhiteList = defaultTestComponentsWhiteList;
        this.servletContext = servletContext;
        this.operatorRepository = operatorRepository;
        this.actuatorRepository = actuatorRepository;
        this.deviceRepository = deviceRepository;
        this.sensorRepository = sensorRepository;
        this.deviceCreateValidator = deviceCreateValidator;
        this.componentCreateValidator = componentCreateValidator;
        this.deploymentWrapper = deploymentWrapper;

        deleteAllComponents();
        addAllComponents();

    }

    /**
     * Registers the wished sensor simulator if the corresponding adapter is already registered
     *
     * @return ResponseEntity if the registration was successful or not
     */
    public ResponseEntity addSensorSimulator() {
        ResponseEntity<String> responseEntity;

        try {
            for (String sensorName : SENSOR_SIMULATORS) {
                if (!sensorRepository.existsByName(sensorName)) {
                    Sensor sensorSimulator = new Sensor();

                    //Enrich actuator for details
                    sensorSimulator.setName(sensorName);
                    sensorSimulator.setOwner(null);

                    if (sensorName.contains("Temperature")) {
                        sensorSimulator.setComponentType("Temperature");
                    } else if (sensorName.contains("Humidity")) {
                        sensorSimulator.setComponentType("Humidity");
                    }

                    sensorSimulator.setOperator(operatorRepository.findByName(sensorName).get());
                    sensorSimulator.setDevice(deviceRepository.findByName(TEST_DEVICE).get());

                    componentCreateValidator.validateCreatable(sensorSimulator);
                    sensorRepository.insert(sensorSimulator);

                }
            }
            responseEntity = new ResponseEntity<>(HttpStatus.OK);


        } catch (Exception e) {
            responseEntity = new ResponseEntity<>(HttpStatus.CONFLICT);

        }

        return responseEntity;
    }


    private ResponseEntity addDevice() {
        ResponseEntity responseEntity = null;
        Device testDevice = null;


        try {
            // Check if device with this name is already registered
            if (!deviceRepository.existsByName(TEST_DEVICE)) {

                //Enrich device for details
                testDevice = new Device();
                testDevice.setName(TEST_DEVICE);
                testDevice.setComponentType("Computer");
                testDevice.setIpAddress(TEST_DEVICE_IP);
                testDevice.setUsername(TEST_DEVICE_USERNAME);
                testDevice.setPassword(TEST_DEVICE_PASSWORD);


                // Insert the new testing device into the device repository
                deviceCreateValidator.validateCreatable(testDevice);
                deviceRepository.insert(testDevice);


                responseEntity = new ResponseEntity<>(HttpStatus.OK);
            }

        } catch (Exception exception) {
            responseEntity = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return responseEntity;
    }


    /**
     * Registers the Testing Actuator which is used for testing purposes and does't make any real actions.
     *
     * @return response entity if insertion was successful or not
     */
    public ResponseEntity addActuatorSimulator() {
        ResponseEntity responseEntity = null;
        Actuator testingActuator;


        try {

            // Check if Actuator is already existing
            if (!actuatorRepository.existsByName(ACTUATOR_NAME)) {
                // Check if the corresponding adapter is registered
                if (operatorRepository.existsByName(ACTUATOR_NAME)) {

                    //Enrich actuator for details
                    testingActuator = new Actuator();
                    testingActuator.setName(ACTUATOR_NAME);
                    testingActuator.setOwner(null);
                    testingActuator.setDevice(deviceRepository.findByName(TEST_DEVICE).get());
                    testingActuator.setOperator(operatorRepository.findByName(ACTUATOR_NAME).get());
                    testingActuator.setComponentType("Buzzer");

                    componentCreateValidator.validateCreatable(testingActuator);
                    actuatorRepository.insert(testingActuator);

                    responseEntity = new ResponseEntity(HttpStatus.CREATED);

                }
            } else {
                responseEntity = new ResponseEntity(HttpStatus.BAD_REQUEST);

            }


        } catch (Exception e) {
            responseEntity = new ResponseEntity(HttpStatus.CONFLICT);

        }
        return responseEntity;
    }

    private static final String DESCRIPTOR_FILE = "operator.json";


    /**
     * Loads default operators from the resources directory and adds them to the operator repository so that they
     * can be used in actuators and sensors by all users.
     */
    public ResponseEntity addSimulatorOperators() {
        ResponseEntity responseEntity = null;

        //Iterate over all default operator paths
        for (String operatorPath : defaultTestComponentsWhiteList) {
            //Create new operator object to add it later to the repository
            Operator newOperator = new Operator();

            //New operator is not owned by anyone
            newOperator.setOwner(null);

            //Get content of the operator directory
            Set<String> operatorContent = servletContext.getResourcePaths(operatorPath);

            //Build path of descriptor
            String descriptorPath = operatorPath + "/" + DESCRIPTOR_FILE;

            //Check if there is a descriptor file, otherwise skip the operator
            if (!operatorContent.contains(descriptorPath)) {
                continue;
            }

            try {
                //Read descriptor file
                InputStream stream = servletContext.getResourceAsStream(descriptorPath);
                String descriptorContent = IOUtils.toString(stream, StandardCharsets.UTF_8);
                JSONObject descriptorJSON = new JSONObject(descriptorContent);
                if (!operatorRepository.existsByName(descriptorJSON.optString("name"))) {
                    //Set operator properties from the descriptor
                    newOperator.setName(descriptorJSON.optString("name"));
                    newOperator.setDescription(descriptorJSON.optString("description"));
                    newOperator.setUnit(descriptorJSON.optString("unit"));

                    //Get parameters
                    JSONArray parameterArray = descriptorJSON.optJSONArray("parameters");

                    //Check if there are parameters
                    if (parameterArray != null) {

                        //Create new list for parameters
                        List<Parameter> parameterList = new ArrayList<>();

                        //Iterate over all parameters
                        for (int i = 0; i < parameterArray.length(); i++) {
                            //Get parameter JSON object
                            JSONObject parameterObject = parameterArray.getJSONObject(i);

                            //Create new parameter object
                            Parameter newParameter = new Parameter();
                            newParameter.setName(parameterObject.optString("name"));
                            newParameter.setType(ParameterType.create(parameterObject.optString("type")));
                            newParameter.setUnit(parameterObject.optString("unit"));
                            newParameter.setMandatory(parameterObject.optBoolean("mandatory", false));

                            //Add parameter to list
                            parameterList.add(newParameter);
                        }

                        //Add parameter list to operator
                        newOperator.setParameters(parameterList);
                    }

                    //Get files
                    JSONArray fileArray = descriptorJSON.optJSONArray("files");

                    //Skip operator if no files are associated with it
                    if ((fileArray == null) || (fileArray.length() < 1)) {
                        continue;
                    }

                    //Iterate over all files
                    for (int i = 0; i < fileArray.length(); i++) {
                        //Get current file path and create a file object
                        String operatorFilePath = operatorPath + "/" + fileArray.getString(i);
                        File operatorFile = new File(servletContext.getRealPath(operatorFilePath));

                        //Determine mime type of the file
                        String operatorFileMime = servletContext.getMimeType(operatorFilePath);

                        if ((operatorFileMime == null) || (operatorFileMime.isEmpty())) {
                            operatorFileMime = "application/octet-stream";
                        }

                        //Try to read the file
                        InputStream operatorFileStream = servletContext.getResourceAsStream(operatorFilePath);
                        byte[] operatorFileBytes = IOUtils.toByteArray(operatorFileStream);

                        //Convert file content to base64 with mime type prefix
                        String base64String = Base64.getEncoder().encodeToString(operatorFileBytes);
                        base64String = "data:" + operatorFileMime + ";base64," + base64String;
                        //data:text/x-sh;base64,

                        //Create new code object for this file
                        Code newCode = new Code();
                        newCode.setName(operatorFile.getName());
                        newCode.setContent(base64String);

                        //Add code to operator
                        newOperator.addRoutine(newCode);
                    }

                    //Insert new operator into repository
                    operatorRepository.insert(newOperator);
                    responseEntity = new ResponseEntity(HttpStatus.OK);
                }


            } catch (Exception e) {
                e.printStackTrace();
                responseEntity = new ResponseEntity(HttpStatus.CONFLICT);
            }
        }
        return responseEntity;
    }

    public ResponseEntity deleteAllComponents() {

        try {
            Device testingDevice = deviceRepository.findByName(TEST_DEVICE).get();
            Actuator testingActuator = actuatorRepository.findByName(ACTUATOR_NAME).get();
            List<Operator> allOperators = operatorRepository.findAll();
            List<Sensor> allSensors = sensorRepository.findAll();

            // Delete the testing-device
            deviceRepository.delete(testingDevice);

            // Delete the testing-device
            actuatorRepository.delete(testingActuator);

            // Delete all sensor simulators via the prefix "TESTING_" that all testing components contains
            for (Sensor sensor : allSensors) {
                if (sensor.getName().contains("TESTING_")) {
                    sensorRepository.delete(sensor);
                }
            }

            // Delete all operators needed for the simulators via the prefix "TESTING_" that all testing components contains
            for (Operator operator : allOperators) {
                if (operator.getName().contains("TESTING_")) {
                    operatorRepository.delete(operator);
                }
            }

            return new ResponseEntity(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(HttpStatus.CONFLICT);
        }
    }

    public ResponseEntity addAllComponents() {
        try {
            addSimulatorOperators();
            addDevice();
            addActuatorSimulator();
            addSensorSimulator();

            return new ResponseEntity(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(HttpStatus.CONFLICT);
        }
    }

    public ResponseEntity redeployComponents(){
        Actuator testingActuator = actuatorRepository.findByName(ACTUATOR_NAME).get();
        List<Sensor> allSensors = sensorRepository.findAll();

        try{
            // Check if Actuator is currently running
            if(deploymentWrapper.isComponentRunning(testingActuator)){
                deploymentWrapper.undeployComponent(testingActuator);
            }
            deploymentWrapper.deployComponent(testingActuator);

            // Delete all sensor simulators via the prefix "TESTING_" that all testing components contains
            for (Sensor sensor : allSensors) {
                if (sensor.getName().contains("TESTING_")) {
                    if(deploymentWrapper.isComponentRunning(sensor)){
                        deploymentWrapper.undeployComponent(sensor);
                    }
                    deploymentWrapper.deployComponent(sensor);
                }
            }

            return new ResponseEntity(HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity(HttpStatus.CONFLICT);
        }




    }


}
