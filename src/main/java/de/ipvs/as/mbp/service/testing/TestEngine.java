package de.ipvs.as.mbp.service.testing;


import de.ipvs.as.mbp.domain.component.Actuator;
import de.ipvs.as.mbp.domain.component.ComponentCreateEventHandler;
import de.ipvs.as.mbp.domain.component.ComponentCreateValidator;
import de.ipvs.as.mbp.domain.component.Sensor;
import de.ipvs.as.mbp.domain.device.Device;
import de.ipvs.as.mbp.domain.device.DeviceCreateEventHandler;
import de.ipvs.as.mbp.domain.device.DeviceCreateValidator;
import de.ipvs.as.mbp.domain.operator.Operator;
import de.ipvs.as.mbp.domain.operator.parameters.ParameterInstance;
import de.ipvs.as.mbp.domain.rules.Rule;
import de.ipvs.as.mbp.domain.testing.TestReport;
import de.ipvs.as.mbp.repository.*;
import de.ipvs.as.mbp.domain.testing.TestDetails;
import de.ipvs.as.mbp.repository.SensorRepository;
import io.swagger.models.auth.In;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;


import java.io.*;
import java.lang.reflect.Array;
import java.util.*;

@Component
public class TestEngine {

    @Autowired
    private TestDetailsRepository testDetailsRepository;

    @Autowired
    private RuleRepository ruleRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private PropertiesService propertiesService;

    @Autowired
    ActuatorRepository actuatorRepository;

    @Autowired
    OperatorRepository operatorRepository;

    @Autowired
    private SensorRepository sensorRepository;

    @Autowired
    private ComponentCreateValidator componentCreateValidator;

    @Autowired
    ComponentCreateEventHandler componentCreateEventHandler;

    @Autowired
    private DeviceCreateValidator deviceCreateValidator;

    @Autowired
    DeviceCreateEventHandler deviceCreateEventHandler;

    @Autowired
    private TestReportRepository testReportRepository;


    @Value("#{'${testingTool.threeDimensionalSensor}'.split(',')}")
    List<String> THREE_DIM_SIMULATOR_LIST;

    @Value("#{'${testingTool.sensorSimulators}'.split(',')}")
    List<String> SIMULATOR_LIST;


    //To resolve ${} in @Value
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    private final String TEST_DEVICE;
    private final String TEST_DEVICE_IP;
    private final String TEST_DEVICE_USERNAME;
    private final String TEST_DEVICE_PASSWORD;
    private final String ACTUATOR_NAME;


    public TestEngine() throws IOException {
        propertiesService = new PropertiesService();
        TEST_DEVICE = propertiesService.getPropertiesString("testingTool.testDeviceName");
        TEST_DEVICE_IP = propertiesService.getPropertiesString("testingTool.ipAddressTestDevice");
        TEST_DEVICE_USERNAME = propertiesService.getPropertiesString("testingTool.testDeviceUserName");
        TEST_DEVICE_PASSWORD = propertiesService.getPropertiesString("testingTool.testDevicePassword");
        ACTUATOR_NAME = propertiesService.getPropertiesString("testingTool.actuatorName");
    }


    /**
     * Returns a HashMap with date and path to of all Test Reports regarding to a specific test.
     *
     * @param testId ID of the test from which all reports are to be found
     * @return hashMap with the date and path to every report regarding to the specific test
     */
    public ResponseEntity<Map<Long, TestReport>> getPDFList(String testId) {
        ResponseEntity<Map<Long, TestReport>> pdfList = null;
        Map<Long, TestReport> nullList = new TreeMap<>();
        TestDetails testDetails = testDetailsRepository.findById(testId).get();
        try {
            if (testReportRepository.existsByName(testDetails.getName())) {
                for (TestReport testReport : testReportRepository.findAllByName(testDetails.getName())) {
                    nullList.put(Long.valueOf(testReport.getStartTimeUnix()), testReport);
                }

            }

            pdfList = new ResponseEntity<>(nullList, HttpStatus.OK);
        } catch (Exception e) {
            pdfList = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return pdfList;
    }

    public Map<String, ArrayList> getSimulationValues(String reportId) {
        Map<String, ArrayList> simulationValues = new HashMap();

        if (testReportRepository.findById(reportId).isPresent()) {
            TestReport testReport = testReportRepository.findById(reportId).get();

            Map<String, LinkedHashMap<Long, Double>> simulationList = testReport.getSimulationList();

            for (Map.Entry<String, LinkedHashMap<Long, Double>> entry : simulationList.entrySet()) {
                ArrayList tupelList = new ArrayList();


                String key = entry.getKey();
                LinkedHashMap<Long, Double> valueList = entry.getValue();
                for (Map.Entry<Long, Double> list : valueList.entrySet()) {
                    ArrayList timeValueTupel = new ArrayList();
                    timeValueTupel.add(list.getKey());
                    timeValueTupel.add(list.getValue());
                    tupelList.add(timeValueTupel);
                }

                simulationValues.put(key, tupelList);

            }



        }
        return  simulationValues;
    }

    /**
     * Update the test configurations redefined by the user.
     *
     * @param testID  Id of the test to be modified
     * @param changes to be included
     * @return if update was successful or not
     */
    public ResponseEntity<Boolean> editTestConfig(String testID, String changes) {
        try {
            TestDetails testToUpdate = testDetailsRepository.findById(testID).get();

            // Clear the configuration and rules field of the specific test
            testToUpdate.getConfig().clear();
            testToUpdate.getRules().clear();
            testToUpdate.getRuleNames().clear();

            // convert the string of the request body to a JSONObject in order to continue working with it
            JSONObject updateInfos = new JSONObject(changes);

            List<List<ParameterInstance>> newConfig = updateSenorConfig(updateInfos.get("config"));
            // Update the rules to be observed in the test
            List<Rule> newRuleList = updateRuleInformation(updateInfos);
            List<String> newRuleNames = updateRuleNames(newRuleList);

            testToUpdate.setConfig(newConfig);
            testToUpdate.setRules(newRuleList);
            testToUpdate.setRuleNames(newRuleNames);
            // Update the information if the selected rules be triggered during the test or not
            testToUpdate.setTriggerRules(updateInfos.getBoolean("triggerRules"));


            testDetailsRepository.save(testToUpdate);

            return ResponseEntity.status(HttpStatus.OK).body(true);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(false);
        }
    }

    /**
     * Creates a list of the updated rule Names that should be observed within the test.
     *
     * @param newRuleList List of rules to be observed within the test
     * @return List of the new rule names
     */
    private List<String> updateRuleNames(List<Rule> newRuleList) {
        List<String> ruleNames = new ArrayList<>();
        for (Rule rule : newRuleList) {
            ruleNames.add(rule.getName());
        }

        return ruleNames;
    }

    /**
     * Creates a list of the updated rules that should be observed within the test.
     *
     * @param updateInfos update/editUseNewData information for the rules and the trigger rules
     * @return List of updated rules
     */
    private List<Rule> updateRuleInformation(JSONObject updateInfos) throws JSONException {
        JSONArray rules = (JSONArray) updateInfos.get("rules");
        List<Rule> newRules = new ArrayList<>();
        if (rules != null) {
            for (int i = 0; i < rules.length(); i++) {
                JSONObject ruleDetails = rules.getJSONObject(i);
                String ruleName = ruleDetails.getString("name");
                newRules.add(ruleRepository.findByName(ruleName).get());
            }
        }
        return newRules;
    }


    /**
     * Creates a list of the updated sensor configurations included into the test.
     *
     * @param config update/editUseNewData information for the sensor configuration
     * @return List new sensor Configurations
     * @throws JSONException In case of parsing problems
     */
    public List<List<ParameterInstance>> updateSenorConfig(Object config) throws JSONException {
        ParameterInstance instance;
        // Get updates for the sensor config
        JSONArray configEntries = (JSONArray) config;

        // Create a new List of Parameter Instances for the updates
        List<List<ParameterInstance>> newConfig = new ArrayList<>();
        if (configEntries != null) {
            for (int i = 0; i < configEntries.length(); i++) {
                // get out single configurations for the different sensors
                JSONArray singleConfig = (JSONArray) configEntries.get(i);
                List<ParameterInstance> newConfigInner = new ArrayList<>();
                for (int j = 0; j < singleConfig.length(); j++) {
                    // get out the name and values of the update Parameter Instances
                    instance = new ParameterInstance(singleConfig.getJSONObject(j).getString("name"), singleConfig.getJSONObject(j).getString("value"));
                    newConfigInner.add(instance);
                }
                newConfig.add(newConfigInner);

            }
        }

        return newConfig;
    }


    /**
     * Registers the Testing Device which is used for testing purposes.
     *
     * @return response entity if insertion was successful or not
     */
    public ResponseEntity registerTestDevice() {
        ResponseEntity responseEntity;
        Device testDevice = null;


        try {
            // Check if device with this name is already registered
            testDevice = getTestDevice();

            if (testDevice == null) {
                //Enrich device for details
                testDevice = new Device();
                testDevice.setName(TEST_DEVICE);
                testDevice.setComponentType("Computer");
                testDevice.setIpAddress(TEST_DEVICE_IP);
                testDevice.setUsername(TEST_DEVICE_USERNAME);
                testDevice.setPassword(TEST_DEVICE_PASSWORD);
            }

            // Validate & insert device
            deviceCreateValidator.validateCreatable(testDevice);
            deviceRepository.insert(testDevice);
            deviceCreateEventHandler.onCreate(testDevice);


            responseEntity = new ResponseEntity<>(HttpStatus.OK);
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
    public ResponseEntity registerTestActuator() {
        //Validation errors
        Errors errors;
        ResponseEntity responseEntity = null;
        Actuator testingActuator;

        Device testDevice;


        try {


            boolean testingActuatorExists = actuatorRepository.existsByName(ACTUATOR_NAME);
            Operator testActuatorAdapter = operatorRepository.findByName(ACTUATOR_NAME).get();
            testDevice = getTestDevice();

            // Check if testing device and actuator are already registered
            if (testDevice == null) {
                // Register the Testing device automatically if not existing
                registerTestDevice();
            } else {
                // Check if Actuator is already existing
                if (testingActuatorExists == false) {
                    // Check if the corresponding adapter is registered
                    if (testActuatorAdapter != null) {
                        //Enrich actuator for details


                        testingActuator = new Actuator();
                        testingActuator.setName(ACTUATOR_NAME);
                        testingActuator.setOwner(null);
                        testingActuator.setDevice(testDevice);
                        testingActuator.setOperator(testActuatorAdapter);
                        testingActuator.setComponentType("Buzzer");


                        // Validate & insert testing actuator
                        componentCreateValidator.validateCreatable(testingActuator);
                        actuatorRepository.insert(testingActuator);
                        componentCreateEventHandler.onCreate(testingActuator);

                        responseEntity = new ResponseEntity(HttpStatus.CREATED);

                    }
                } else {
                    responseEntity = new ResponseEntity(HttpStatus.BAD_REQUEST);

                }
            }


        } catch (Exception e) {
            responseEntity = new ResponseEntity(HttpStatus.CONFLICT);

        }
        return responseEntity;
    }

    /**
     * Checks if the Testing Device is already registered and turn back this device or null.
     *
     * @return the test Device if existing
     */
    private Device getTestDevice() {
        Device testDevice = null;

        // List of all registered Devices
        List<Device> testDeviceList = deviceRepository.findAll();

        // Go through the List of devices and check if testing Device is available
        Iterator iterator = testDeviceList.listIterator();
        while (iterator.hasNext()) {
            Device tempDevice = (Device) iterator.next();
            if (tempDevice.getName().equals(TEST_DEVICE)) {
                testDevice = tempDevice;
                break;
            }
        }
        return testDevice;
    }

    /**
     * Checks if given Sensor Simulator is already registered and turn back this sensor or null.
     *
     * @return the sensor simulator if existing
     */
    private Sensor getSensorSimulator(String sensorName) {
        Sensor sensorSimulator = null;

        // List of all registered Sensors
        List<Sensor> sensorList = sensorRepository.findAll();

        // Go through the List of sensors and check if specific sensor is available
        Iterator iterator = sensorList.listIterator();
        while (iterator.hasNext()) {
            Sensor tempSensor = (Sensor) iterator.next();
            if (tempSensor.getName().equals(sensorName)) {
                sensorSimulator = tempSensor;
            }

        }

        return sensorSimulator;
    }


    /**
     * Registers the wished sensor simulator if the corresponding adapter is already registered
     *
     * @param sensorName Name of the sensor simulator to be registered
     * @return ResponseEntity if the registration was successful or not
     */
    public ResponseEntity registerSensorSimulator(String sensorName) {

        ResponseEntity<String> responseEntity;

        Operator sensorAdapter = operatorRepository.findByName(sensorName).get();
        Device testingDevice = getTestDevice();
        Sensor sensorSimulator = getSensorSimulator(sensorName);

        try {
            // Check if corresponding adapter exists
            if (sensorAdapter == null) {
                return new ResponseEntity<>(HttpStatus.CONFLICT);

            } else if (testingDevice == null) {
                registerTestDevice();
            } else if (sensorSimulator == null) {
                //Enrich actuator for details
                sensorSimulator = new Sensor();
                sensorSimulator.setName(sensorName);
                sensorSimulator.setOwner(null);

                if (sensorName.contains("Temperature")) {
                    sensorSimulator.setComponentType("Temperature");
                } else if (sensorName.contains("Humidity")) {
                    sensorSimulator.setComponentType("Humidity");
                } else {
                    sensorSimulator.setComponentType("Motion");
                }

                sensorSimulator.setOperator(sensorAdapter);
                sensorSimulator.setDevice(testingDevice);

                //Validate & insert sensor
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
     * Checks if the one and three dimensional sensor simulators are already registered.
     *
     * @param sensor Name of the sensor to be checked
     * @return Boolean if the sensor is already registered or not
     */
    public Boolean isSimulatorRegistr(String sensor) {
        Boolean registered = false;
        String dimX = sensor + "X";
        String dimY = sensor + "Y";
        String dimZ = sensor + "Z";

        if (THREE_DIM_SIMULATOR_LIST.contains(sensor)) {
            Sensor sensorX = getSensorSimulator(dimX);
            Sensor sensorY = getSensorSimulator(dimY);
            Sensor sensorZ = getSensorSimulator(dimZ);

            if (sensorX != null && sensorY != null && sensorZ != null) {
                registered = true;
            }
        } else {
            if (getSensorSimulator(sensor) != null) {
                registered = true;
            }
        }

        return registered;
    }


    /**
     * Registers the wished three dimensional sensor simulator if the corresponding adapter is already registered.
     *
     * @param sensorName of the three dimensional sensor to be registered
     * @return Response entity if registration was successful
     */
    public void registerThreeDimSensorSimulator(String sensorName) {
        //TODO
    }

    /**
     * Delete Tets with specific test id.
     *
     * @param testId
     */
    public void deleteTest(String testId) {
        TestDetails testDetails = testDetailsRepository.findById(testId).get();

        if (testDetailsRepository.existsById(testId)) {
            testDetailsRepository.deleteById(testId);
        }

    }

    /**
     * Delete a specific test report of a test.
     *
     * @param reportId
     * @return
     */
    public ResponseEntity deleteReport(String reportId) {
        ResponseEntity responseEntity = null;
        try {
            if (testReportRepository.existsById(reportId)) {
                TestReport testReport = testReportRepository.findById(reportId).get();
                testReportRepository.delete(testReport);
                responseEntity = new ResponseEntity<>(HttpStatus.OK);
            }
        } catch (Exception e) {
            responseEntity = new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        return responseEntity;
    }
}