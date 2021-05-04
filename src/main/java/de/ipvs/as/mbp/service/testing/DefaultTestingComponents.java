package de.ipvs.as.mbp.service.testing;

import de.ipvs.as.mbp.DynamicBeanProvider;
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
import de.ipvs.as.mbp.domain.rules.RuleTrigger;
import de.ipvs.as.mbp.domain.testing.TestDetails;
import de.ipvs.as.mbp.domain.user_entity.MBPEntity;
import de.ipvs.as.mbp.repository.*;
import de.ipvs.as.mbp.repository.projection.ComponentExcerpt;
import de.ipvs.as.mbp.service.UserEntityService;
import de.ipvs.as.mbp.service.event_handler.ICreateEventHandler;
import de.ipvs.as.mbp.service.testing.analyzer.TestAnalyzer;
import de.ipvs.as.mbp.service.validation.ICreateValidator;
import de.ipvs.as.mbp.web.rest.helper.DeploymentWrapper;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
public class DefaultTestingComponents {

    @Autowired
    private final List<String> defaultTestComponentsWhiteList;

    @Autowired
    private final TestDetailsRepository testDetailsRepository;

    @Autowired
    private final ServletContext servletContext;

    @Autowired
    private final OperatorRepository operatorRepository;

    @Autowired
    private final ActuatorRepository actuatorRepository;

    @Autowired
    private final DeviceRepository deviceRepository;

    @Autowired
    private final SensorRepository sensorRepository;

    @Autowired
    private final DeviceCreateValidator deviceCreateValidator;

    @Autowired
    private final PropertiesService propertiesService;

    @Autowired
    private final ComponentCreateValidator componentCreateValidator;

    @Autowired
    private final ComponentCreateEventHandler componentCreateEventHandler;

    @Autowired
    private final DeviceCreateEventHandler deviceCreateEventHandler;

    @Autowired
    private DeploymentWrapper deploymentWrapper;

    @Autowired
    private final RuleRepository ruleRepository;

    @Autowired
    private final TestAnalyzer testAnalyzer;

    @Autowired
    RuleTriggerRepository ruleTriggerRepository;

    @Autowired
    private UserEntityService userEntityService;

    private static final String DESCRIPTOR_FILE = "operator.json";

    private final String TEST_DEVICE;
    private final String TEST_DEVICE_IP;
    private final String TEST_DEVICE_USERNAME;
    private final String TEST_DEVICE_PASSWORD;
    private final String ACTUATOR_NAME;
    private final String TEST_PREFIX;

    private final List<String> SENSOR_SIMULATORS = Arrays.asList("TESTING_TemperatureSensor", "TESTING_TemperatureSensorPl", "TESTING_HumiditySensor", "TESTING_HumiditySensorPl");


    public DefaultTestingComponents(List<String> defaultTestComponentsWhiteList, ServletContext servletContext, OperatorRepository operatorRepository, DeviceRepository deviceRepository, DeviceCreateValidator deviceCreateValidator, ActuatorRepository actuatorRepository, ComponentCreateValidator componentCreateValidator, ComponentCreateEventHandler componentCreateEventHandler, DeviceCreateEventHandler deviceCreateEventHandler,
                                    SensorRepository sensorRepository, TestDetailsRepository testDetailsRepository, RuleRepository ruleRepository, RuleTriggerRepository ruleTriggerRepository, TestAnalyzer testAnalyzer, UserEntityService userEntityService) throws IOException {
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
        this.userEntityService = userEntityService;

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
    public ResponseEntity<String> addSensorSimulator(String simulatorName) {
        ResponseEntity<String> responseEntity;

        try {
            if (!sensorRepository.findByName(simulatorName).isPresent()) {
                Sensor sensorSimulator = new Sensor();

                //Enrich actuator for details
                sensorSimulator.setName(simulatorName);
                sensorSimulator.setOwner(null);
                sensorSimulator.setComponentType(componentType(simulatorName));
                if (operatorRepository.findByName(simulatorName).isPresent() && deviceRepository.findByName(TEST_DEVICE).isPresent()) {
                    sensorSimulator.setOperator(operatorRepository.findByName(simulatorName).get());
                    sensorSimulator.setDevice(deviceRepository.findByName(TEST_DEVICE).get());
                }

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
     * Returns the type of the sensor simulator that can be "Temperature" or "Humidity".
     *
     * @param simulatorName name of the senor simulator to be registered
     * @return type of sensor
     */
    public String componentType(String simulatorName) {
        if (simulatorName.contains("Temperature")) {
            return "Temperature";
        } else {
            return "Humidity";
        }
    }


    /**
     * Registers the testing device which is used for testing purposes.
     */
    private void addDevice() {
        Device testDevice;

        try {
            // Check if device with this name is already registered
            if (!deviceRepository.existsByName(TEST_DEVICE)) {

                //Enrich device for details
                testDevice = new Device();
                testDevice.setName(TEST_DEVICE);
                testDevice.setComponentType("Computer");
                testDevice.setIpAddress(TEST_DEVICE_IP);
    			testDevice.setDate(LocalDateTime.now().toString());
                testDevice.setUsername(TEST_DEVICE_USERNAME);
                testDevice.setPassword(TEST_DEVICE_PASSWORD);

                // Validate, insert and create a new event handler for the new testing device
                deviceCreateValidator.validateCreatable(testDevice);



                // Save device in the database
                deviceRepository.save(testDevice);
                deviceCreateEventHandler.onCreate(testDevice);
            }

        } catch (Exception exception) {
            exception.printStackTrace();
        }

    }


    /**
     * Registers the Testing Actuator which is used for testing purposes and doesn't make any real actions.
     */
    public void addActuatorSimulator() {
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
                    testingActuator.setComponentType("Buzzer");
                    if (deviceRepository.findByName(TEST_DEVICE).isPresent() && operatorRepository.findByName(ACTUATOR_NAME).isPresent()) {
                        testingActuator.setDevice(deviceRepository.findByName(TEST_DEVICE).get());
                        testingActuator.setOperator(operatorRepository.findByName(ACTUATOR_NAME).get());
                    }

                    // Validate, insert and add event handler for the new actuator
                    componentCreateValidator.validateCreatable(testingActuator);
                    actuatorRepository.insert(testingActuator);
                    componentCreateEventHandler.onCreate(testingActuator);

                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();

        }
    }


    /**
     * Loads default operators from the resources directory and adds them to the operator repository so that they
     * can be used in actuators and sensors by all users.
     */
    public void addSimulatorOperators() {

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
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Replaces the installed sensor simulator with a new one, replaces the old test device
     * and replaces the sensor simulators in the tests and rules that uses this simulator.
     *
     * @return if the replacement was successful or not.
     */
    public ResponseEntity replaceSensorSimulators() {
        try {

            // Replace each sensor simulator and their occurrences in the tests and rules one after the other.
            for (String sensorName : SENSOR_SIMULATORS) {
                List<TestDetails> affectedTestDetails = null;
                String oldSensorId = null;

                // Get a list of the tests which uses the specific sensor simulator to be replaced
                if (sensorRepository.findByName(sensorName).isPresent()) {
                    oldSensorId = sensorRepository.findByName(sensorName).get().getId();
                    affectedTestDetails = testDetailsRepository.findAllBySensorId(oldSensorId);
                }

                // Delete the sensor simulator
                if (sensorRepository.existsByName(sensorName)) {
                    sensorRepository.delete(sensorRepository.findByName(sensorName).get());
                }

                // Install the sensor simulator
                addSensorSimulator(sensorName);

                // Replace the reinstalled sensor simulator in the affected tests
                if (affectedTestDetails != null && affectedTestDetails.size() >= 1) {
                    replaceSimulatorInTest(affectedTestDetails);
                }

                // Replace the reinstalled sensor simulator in the affected rules.
                replaceSimulatorInRule(oldSensorId, sensorName);
            }

        } catch (Exception exception) {
            exception.printStackTrace();
            return new ResponseEntity(HttpStatus.CONFLICT);
        }

        return new ResponseEntity(HttpStatus.OK);
    }


    /**
     * Replaces the reinstalled sensor simulator in the rules in which the sensor simulator is used in the conditions.
     *
     * @param oldSensorId Old Id of the reinstalled sensor, to find the rule conditions which still uses the old sensor simulator
     * @param sensorName  Name of the reinstalled sensor
     */
    private void replaceSimulatorInRule(String oldSensorId, String sensorName) {
        List<RuleTrigger> triggerList = ruleTriggerRepository.findAll();
        for (RuleTrigger ruleTrigger : triggerList) {
            if (ruleTrigger.getQuery().contains(oldSensorId)) {

                // adjust trigger query of the sensor of the test
                String triggerQuery = ruleTrigger.getQuery();

                // Regex to get out the sensor ID
                Pattern pattern = Pattern.compile("(?<=sensor_)([0-9a-zA-Z]*)");
                Matcher matcher = pattern.matcher(triggerQuery);
                while (matcher.find()) {
                    String sensorID = matcher.group();
                    if (sensorID.contains(oldSensorId)) {
                        if (sensorRepository.findByName(sensorName).isPresent()) {
                            Sensor updatedSensor = sensorRepository.findByName(sensorName).get();
                            // replace the sensor id in the trigger query with the rerun sensor id
                            triggerQuery = triggerQuery.replace(oldSensorId, updatedSensor.getId());
                            ruleTrigger.setQuery(triggerQuery);
                            ruleTriggerRepository.save(ruleTrigger);
                        }

                    }
                }
            }
        }
    }

    /**
     * Replaces the reinstalled sensor simulator in the tests in which the sensor simulator is used.
     *
     * @param affectedTests List of the tests affected by the sensor simulator reinstallation
     */
    public void replaceSimulatorInTest(List<TestDetails> affectedTests) {
        for (TestDetails test : affectedTests) {
            List<Sensor> sensorList = test.getSensor();
            for (Sensor sensor : sensorList) {
                if (sensorRepository.findByName(sensor.getName()).isPresent()) {
                    // Get the index of the reinstalled sensor and replace it with the new one
                    int index = sensorList.indexOf(sensor);
                    Sensor replacedSensor = sensorRepository.findByName(sensor.getName()).get();
                    sensorList.set(index, replacedSensor);
                }

            }
            // Save the modified test
            test.setSensor(sensorList);
            testDetailsRepository.save(test);
        }
    }

    /**
     * Replaces the test device used for test purposes and replaces the device in the rerun sensors.
     *
     * @return if the replacement was successful or not.
     */
    public ResponseEntity replaceTestDevice() {
        try {
            String oldDeviceId = null;
            // Delete the testing-device if exists
            if (deviceRepository.findByName(TEST_DEVICE).isPresent()) {
                oldDeviceId = deviceRepository.findByName(TEST_DEVICE).get().getId();
                deviceRepository.delete(deviceRepository.findByName(TEST_DEVICE).get());
            }
            addDevice();
            replaceDeviceInRerun(oldDeviceId);
            // Add the new testing-device
        } catch (Exception e) {
            return new ResponseEntity(HttpStatus.CONFLICT);
        }

        return new ResponseEntity(HttpStatus.OK);
    }

    /**
     * Replaces the reinstalled test device in the rerun sensors.
     *
     * @param oldDeviceId old device id to find the rerun sensors using the replaced test device
     */
    public void replaceDeviceInRerun(String oldDeviceId) {
        List<ComponentExcerpt> sensorList = sensorRepository.findAllByDeviceId(oldDeviceId);
        for (ComponentExcerpt senor : sensorList) {
            if (senor.getName().contains("RERUN_")) {
                if (sensorRepository.findByName(senor.getName()).isPresent() && deviceRepository.findByName(TEST_DEVICE).isPresent()) {
                    Sensor sensorUpdate = sensorRepository.findByName(senor.getName()).get();
                    sensorUpdate.setDevice(deviceRepository.findByName(TEST_DEVICE).get());
                    sensorRepository.save(sensorUpdate);

                }

            }
        }

    }

    /**
     * Reinstals the operators of the default testing components.
     */
    public void replaceOperators() {
        try {
            List<Operator> allOperators = operatorRepository.findAll();
            // Delete all operators needed for the simulators via the prefix "TESTING_" that all testing components contains
            for (Operator operator : allOperators) {
                if (operator.getName().contains(TEST_PREFIX)) {
                    operatorRepository.delete(operator);
                }
            }
            // Install all operators needed for the testing simulators
            addSimulatorOperators();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }


    /**
     * Replaces the testing actuator with a new one.
     *
     * @return if the replacement was successful or not.
     */
    public ResponseEntity replaceTestingActuator() {
        try {
            // Delete the testing-device if existing
            if (actuatorRepository.findByName(ACTUATOR_NAME).isPresent()) {
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
        Actuator testingActuator = null;
        try {
            if (actuatorRepository.findByName(ACTUATOR_NAME).isPresent()) {
                testingActuator = actuatorRepository.findByName(ACTUATOR_NAME).get();
            }

            List<Sensor> allSensors = sensorRepository.findAll();

            // Check if Actuator is currently running and redeploy it
            if (testingActuator != null) {
                if (deploymentWrapper.isComponentRunning(testingActuator)) {
                    deploymentWrapper.undeployComponent(testingActuator);
                }
                deploymentWrapper.deployComponent(testingActuator);
            }

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
