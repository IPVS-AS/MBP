package de.ipvs.as.mbp.service.testing;

import de.ipvs.as.mbp.domain.access_control.ACAccessRequest;
import de.ipvs.as.mbp.domain.access_control.ACAccessType;
import de.ipvs.as.mbp.domain.component.Actuator;
import de.ipvs.as.mbp.domain.component.ComponentCreateEventHandler;
import de.ipvs.as.mbp.domain.component.ComponentCreateValidator;
import de.ipvs.as.mbp.domain.component.Sensor;
import de.ipvs.as.mbp.domain.device.Device;
import de.ipvs.as.mbp.domain.device.DeviceCreateEventHandler;
import de.ipvs.as.mbp.domain.device.DeviceCreateValidator;
import de.ipvs.as.mbp.domain.operator.Code;
import de.ipvs.as.mbp.domain.operator.Operator;
import de.ipvs.as.mbp.domain.operator.parameters.Parameter;
import de.ipvs.as.mbp.domain.operator.parameters.ParameterType;
import de.ipvs.as.mbp.domain.rules.Rule;
import de.ipvs.as.mbp.domain.rules.RuleTrigger;
import de.ipvs.as.mbp.domain.testing.TestDetails;
import de.ipvs.as.mbp.repository.*;
import de.ipvs.as.mbp.service.UserEntityService;
import de.ipvs.as.mbp.service.testing.analyzer.TestAnalyzer;
import de.ipvs.as.mbp.web.rest.helper.DeploymentWrapper;
import net.sf.cglib.asm.$ByteVector;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
public class DefaultTestingComponents {

    @Autowired
    private List<String> defaultTestComponentsWhiteList;

    @Autowired
    private TestDetailsRepository testDetailsRepository;

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
    private ComponentCreateEventHandler componentCreateEventHandler;

    @Autowired
    private DeviceCreateEventHandler deviceCreateEventHandler;

    @Autowired
    private DeploymentWrapper deploymentWrapper;

    @Autowired
    private RuleRepository ruleRepository;

    @Autowired
    private TestAnalyzer testAnalyzer;

    @Autowired
    RuleTriggerRepository ruleTriggerRepository;

    private final String TEST_DEVICE;
    private final String TEST_DEVICE_IP;
    private final String TEST_DEVICE_USERNAME;
    private final String TEST_DEVICE_PASSWORD;
    private final String ACTUATOR_NAME;
    private final String TEST_PREFIX;
    private final List<String> SENSOR_SIMULATORS = Arrays.asList("TESTING_TemperatureSensor", "TESTING_TemperatureSensorPl", "TESTING_HumiditySensor", "TESTING_HumiditySensorPl");


    public DefaultTestingComponents(List<String> defaultTestComponentsWhiteList, ServletContext servletContext, OperatorRepository operatorRepository, DeviceRepository deviceRepository, DeviceCreateValidator deviceCreateValidator, ActuatorRepository actuatorRepository, ComponentCreateValidator componentCreateValidator, ComponentCreateEventHandler componentCreateEventHandler, DeviceCreateEventHandler deviceCreateEventHandler,
                                    SensorRepository sensorRepository, TestDetailsRepository testDetailsRepository, RuleRepository ruleRepository, RuleTriggerRepository ruleTriggerRepository, TestAnalyzer testAnalyzer) throws IOException {
        // Get needed Strings out of the properties to create the testing components
        propertiesService = new PropertiesService();
        TEST_DEVICE = propertiesService.getPropertiesString("testingTool.testDeviceName");
        TEST_DEVICE_IP = propertiesService.getPropertiesString("testingTool.ipAddressTestDevice");
        TEST_DEVICE_USERNAME = propertiesService.getPropertiesString("testingTool.testDeviceUserName");
        TEST_DEVICE_PASSWORD = propertiesService.getPropertiesString("testingTool.testDevicePassword");
        ACTUATOR_NAME = propertiesService.getPropertiesString("testingTool.actuatorName");
        TEST_PREFIX = propertiesService.getPropertiesString("testingTool.testComponentIdentifier");


        this.defaultTestComponentsWhiteList = defaultTestComponentsWhiteList;
        this.servletContext = servletContext;
        this.operatorRepository = operatorRepository;
        this.actuatorRepository = actuatorRepository;
        this.deviceRepository = deviceRepository;
        this.sensorRepository = sensorRepository;
        this.deviceCreateValidator = deviceCreateValidator;
        this.componentCreateValidator = componentCreateValidator;
        this.componentCreateEventHandler = componentCreateEventHandler;
        this.deviceCreateEventHandler = deviceCreateEventHandler;
        this.ruleRepository = ruleRepository;
        this.ruleTriggerRepository = ruleTriggerRepository;
        this.testDetailsRepository = testDetailsRepository;
        this.testAnalyzer = testAnalyzer;

        replaceTestDevice();
        replaceOperators();
        replaceTestingActuator();
        replaceSensorSimulators();

    }

    /**
     * Registers the wished sensor simulator if the corresponding adapter is already registered
     *
     * @return ResponseEntity if the registration was successful or not
     */
    public ResponseEntity addSensorSimulator(String simulatorName) {
        ResponseEntity<String> responseEntity;

        try {
            if (!sensorRepository.existsByName(simulatorName)) {
                Sensor sensorSimulator = new Sensor();

                //Enrich actuator for details
                sensorSimulator.setName(simulatorName);
                sensorSimulator.setOwner(null);

                if (simulatorName.contains("Temperature")) {
                    sensorSimulator.setComponentType("Temperature");
                } else if (simulatorName.contains("Humidity")) {
                    sensorSimulator.setComponentType("Humidity");
                }

                sensorSimulator.setOperator(operatorRepository.findByName(simulatorName).get());
                sensorSimulator.setDevice(deviceRepository.findByName(TEST_DEVICE).get());

                // Validate, insert and create a new event handler for the new sensor simulator
                componentCreateValidator.validateCreatable(sensorSimulator);
                sensorRepository.insert(sensorSimulator);
                componentCreateEventHandler.onCreate(sensorSimulator);

            }

            responseEntity = new ResponseEntity<>(HttpStatus.OK);


        } catch (Exception e) {
            responseEntity = new ResponseEntity<>(HttpStatus.CONFLICT);

        }

        return responseEntity;
    }


    /**
     * Registers the testing device which is used for testing purposes.
     *
     * @return http status whether it was successful or not
     */
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


                // Validate, insert and create a new event handler for the new testing device
                deviceCreateValidator.validateCreatable(testDevice);
                deviceRepository.insert(testDevice);
                deviceCreateEventHandler.onCreate(testDevice);


                responseEntity = new ResponseEntity<>(HttpStatus.OK);
            }

        } catch (Exception exception) {
            responseEntity = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return responseEntity;
    }


    /**
     * Registers the Testing Actuator which is used for testing purposes and doesn't make any real actions.
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

                    // Validate, insert and add event handler for the new actuator
                    componentCreateValidator.validateCreatable(testingActuator);
                    actuatorRepository.insert(testingActuator);
                    componentCreateEventHandler.onCreate(testingActuator);

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
     *
     * @return http status whether it was successful or not
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

    public ResponseEntity replaceSensorSimulators() {
        try {

            for (String sensorName : SENSOR_SIMULATORS) {

                List<TestDetails> affectedTestDetails = null;
                String sensorId = null;
                if (sensorRepository.findByName(sensorName).isPresent()) {
                    sensorId = sensorRepository.findByName(sensorName).get().getId();
                    affectedTestDetails = testDetailsRepository.findAllBySensorId(sensorId);

                }

                if (sensorRepository.existsByName(sensorName)) {
                    sensorRepository.delete(sensorRepository.findByName(sensorName).get());
                }
                addSensorSimulator(sensorName);
                if (affectedTestDetails != null && affectedTestDetails.size() >= 1) {
                    replaceSimulatorInTest(affectedTestDetails);
                    replaceSimulatorInRule(affectedTestDetails, sensorId, sensorName);
                }

            }

        } catch (Exception e) {

        }

        return new ResponseEntity(HttpStatus.OK);
    }

    private void replaceSimulatorInRule(List<TestDetails> affectedTests, String oldId, String sensorName) {
        for (TestDetails test : affectedTests) {
            List<Rule> affectedRules = testAnalyzer.getCorrespondingRules(test);

            for (Rule rule : affectedRules) {
                if (ruleRepository.findByName(rule.getName()).isPresent()) {
                    // create new rule
                    Rule updatedRule = ruleRepository.findByName(rule.getName()).get();

                    // adjust trigger querey
                    if (ruleTriggerRepository.findByName(rule.getTrigger().getName()).isPresent()) {
                        // create new trigger
                        RuleTrigger newTrigger = ruleTriggerRepository.findByName(rule.getTrigger().getName()).get();

                        // adjust trigger query of the sensor of the test
                        String triggerQuery = rule.getTrigger().getQuery();
                        // Regex to get out the sensor ID
                            Pattern pattern = Pattern.compile("(?<=sensor_)([0-9a-zA-Z]*)");
                        Matcher matcher = pattern.matcher(triggerQuery);
                        while (matcher.find()) {
                            String sensorID = matcher.group();
                            if (sensorID.contains(oldId)) {
                                Sensor updatedSensor = sensorRepository.findByName(sensorName).get();
                                if (updatedSensor != null) {
                                    // replace the sensor id in the trigger query with the rerun sensor id
                                    triggerQuery = triggerQuery.replace(oldId, updatedSensor.getId());
                                    newTrigger.setQuery(triggerQuery);
                                    ruleTriggerRepository.save(newTrigger);
                                }

                            }
                        }
                        // set the created trigger of the rerun rule
                        updatedRule.setTrigger(newTrigger);
                        ruleRepository.save(updatedRule);
                        List<Rule> ruleList = test.getRules();
                        int index = ruleList.indexOf(updatedRule);
                        Rule replacedRule= ruleRepository.findByName(updatedRule.getName()).get();
                        ruleList.set(index, replacedRule);
                        test.setRules(ruleList);
                        testDetailsRepository.save(test);
                    }




                }
            }


        }
    }


    public void replaceSimulatorInTest(List<TestDetails> affectedTests) {
        for (TestDetails test : affectedTests) {
            List<Sensor> sensorList = test.getSensor();
            for (Sensor sensor : sensorList) {
                int index = sensorList.indexOf(sensor);
                Sensor replacedSensor = sensorRepository.findByName(sensor.getName()).get();

                sensorList.set(index, replacedSensor);

            }
            test.setSensor(sensorList);
            testDetailsRepository.save(test);

        }


    }


    /**
     * Deletes all default components needed by the testing-tool automatically created by starting the mbp.
     *
     * @return http status whether it was successful or not
     */
    public ResponseEntity deleteAllComponents() {

        try {
            List<Operator> allOperators = operatorRepository.findAll();
            List<Sensor> allSensors = sensorRepository.findAll();

            // Delete the testing-device if exists
            if (deviceRepository.existsByName(TEST_DEVICE)) {
                deviceRepository.delete(deviceRepository.findByName(TEST_DEVICE).get());
            }

            // Delete the testing-device if existing
            if (actuatorRepository.existsByName(ACTUATOR_NAME)) {
                actuatorRepository.delete(actuatorRepository.findByName(ACTUATOR_NAME).get());
            }

            // Delete all sensor simulators via the prefix "TESTING_" that all testing components contains
            for (Sensor sensor : allSensors) {
                if (sensor.getName().contains(TEST_PREFIX)) {
                    sensorRepository.delete(sensor);
                }
            }

            // Delete all operators needed for the simulators via the prefix "TESTING_" that all testing components contains
            for (Operator operator : allOperators) {
                if (operator.getName().contains(TEST_PREFIX)) {
                    operatorRepository.delete(operator);
                }
            }

            return new ResponseEntity(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(HttpStatus.CONFLICT);
        }
    }

    public ResponseEntity replaceTestDevice() {
        try {
            // Delete the testing-device if exists
            if (deviceRepository.existsByName(TEST_DEVICE)) {
                deviceRepository.delete(deviceRepository.findByName(TEST_DEVICE).get());
            }
            addDevice();

            // Add the new testing-device
        } catch (Exception e) {
            return new ResponseEntity(HttpStatus.CONFLICT);
        }

        return new ResponseEntity(HttpStatus.OK);
    }

    public void replaceOperators() {
        try {
            List<Operator> allOperators = operatorRepository.findAll();
            // Delete all operators needed for the simulators via the prefix "TESTING_" that all testing components contains
            for (Operator operator : allOperators) {
                if (operator.getName().contains(TEST_PREFIX)) {
                    operatorRepository.delete(operator);
                }
            }
            addSimulatorOperators();
        } catch (Exception e) {

        }


    }


    public ResponseEntity replaceTestingActuator() {
        try {
            // Delete the testing-device if existing
            if (actuatorRepository.existsByName(ACTUATOR_NAME)) {
                actuatorRepository.delete(actuatorRepository.findByName(ACTUATOR_NAME).get());
            }

            addActuatorSimulator();
        } catch (Exception e) {
            return new ResponseEntity(HttpStatus.CONFLICT);
        }
        return new ResponseEntity(HttpStatus.CONFLICT);
    }


    /**
     * Redeploys all default components needed by the testing-tool.
     *
     * @return http status whether it was successful or not
     */
    public ResponseEntity redeployComponents() {
        Actuator testingActuator = actuatorRepository.findByName(ACTUATOR_NAME).get();
        List<Sensor> allSensors = sensorRepository.findAll();

        try {
            // Check if Actuator is currently running
            if (deploymentWrapper.isComponentRunning(testingActuator)) {
                deploymentWrapper.undeployComponent(testingActuator);
            }
            deploymentWrapper.deployComponent(testingActuator);

            // Delete all sensor simulators via the prefix "TESTING_" that all testing components contains
            for (Sensor sensor : allSensors) {
                if (sensor.getName().contains(TEST_PREFIX)) {
                    if (deploymentWrapper.isComponentRunning(sensor)) {
                        deploymentWrapper.undeployComponent(sensor);
                    }
                    deploymentWrapper.deployComponent(sensor);
                }
            }

            return new ResponseEntity(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(HttpStatus.CONFLICT);
        }
    }
}