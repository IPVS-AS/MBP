package org.citopt.connde.service.testing.executor;

import com.fasterxml.jackson.databind.annotation.JsonAppend;
import org.citopt.connde.domain.adapter.parameters.ParameterInstance;
import org.citopt.connde.domain.component.Actuator;
import org.citopt.connde.domain.component.Sensor;
import org.citopt.connde.domain.rules.Rule;
import org.citopt.connde.domain.testing.TestDetails;
import org.citopt.connde.repository.*;
import org.citopt.connde.service.testing.PropertiesService;
import org.citopt.connde.service.testing.analyzer.GraphPlotter;
import org.citopt.connde.service.testing.analyzer.TestAnalyzer;
import org.citopt.connde.service.testing.analyzer.TestReport;
import org.citopt.connde.web.rest.RestDeploymentController;
import org.citopt.connde.web.rest.RestRuleController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.sql.Struct;
import java.util.*;

/**
 * Starts the test and all its components. Then analyzes the results.
 */
@Component
public class TestExecutor {

    @Autowired
    private SensorRepository sensorRepository;

    @Autowired
    private GraphPlotter graphPlotter;

    @Autowired
    private TestReport testReport;

    @Autowired
    private TestDetailsRepository testDetailsRepository;

    @Autowired
    private ActuatorRepository actuatorRepository;

    @Autowired
    private RestDeploymentController restDeploymentController;

    @Autowired
    private RestRuleController restRuleController;

    @Autowired
    private TestAnalyzer testAnalyzer;

    @Autowired
    private PropertiesService propertiesService;


    // List of all active Tests
    Map<String, TestDetails> activeTests = new HashMap<>();
    @Value("#{'${testingTool.sensorSimulators}'.split(',')}")
    List<String> SIMULATOR_LIST;

    private final String RERUN_IDENTIFIER;
    private final String TESTING_ACTUATOR;
    private final String CONFIG_SENSOR_NAME_KEY;

    //To resolve ${} in @Value
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
        return new PropertySourcesPlaceholderConfigurer();
    }


    public TestExecutor(  ) throws IOException {
        propertiesService = new PropertiesService();
        this.RERUN_IDENTIFIER = propertiesService.getPropertiesString("testingTool.RerunIdentifier");
        this.TESTING_ACTUATOR = propertiesService.getPropertiesString("testingTool.actuatorName");
        this.CONFIG_SENSOR_NAME_KEY = propertiesService.getPropertiesString("testingTool.ConfigSensorNameKey");
    }


    /**
     * Returns a list of all active tests.
     *
     * @return activeTests
     */
    public Map<String, TestDetails> getActiveTests() {
        return activeTests;
    }

    /**
     * Sets a list of all active tests
     *
     * @param activeTests active/running tests
     */
    public void setActiveTests(Map<String, TestDetails> activeTests) {
        this.activeTests = activeTests;
    }


    /**
     * Puts the test and the corresponding sensors into a list and resets the list of values that where previously saved in other tests.
     *
     * @param test to be executed
     */
    public void activateTest(TestDetails test) {
        Map<String, TestDetails> activeTests = getActiveTests();
        Map<String, LinkedHashMap<Long, Double>> list = testAnalyzer.getTestValues();

        if (test.isUseNewData()) {
            for (Sensor sensor : test.getSensor()) {
                if (!sensor.getName().contains(RERUN_IDENTIFIER)) {
                    activeTests.put(sensor.getId(), test);
                    list.remove(sensor.getId());
                }

            }
        } else {
            for (Sensor sensor : test.getSensor()) {
                if (sensor.getName().contains(RERUN_IDENTIFIER)) {
                    activeTests.put(sensor.getId(), test);
                    list.remove(sensor.getId());
                }
            }
        }

        setActiveTests(activeTests);
        testAnalyzer.setTestValues(list);
    }


    /**
     * Starts the test and saves all values form the sensor.
     *
     * @param test test to be executed
     */
    public void executeTest(TestDetails test) throws Exception {

        // Set the exact start time of the test
        test.setStartTestTimeNow();
        testDetailsRepository.save(test);

        // get  information about the status of the rules before the execution of the test
        List<Rule> rulesBefore = testAnalyzer.getCorrespondingRules(test);

        // add test and sensors to the activation list
        activateTest(test);

        // start all components relevant for the test
        startTest(testDetailsRepository.findById(test.getId()));

        // Get List of all simulated Values
        Map<String, LinkedHashMap<Long, Double>> valueList = testAnalyzer.isFinished(test.getId());
        saveValues(test, valueList);
        analyzeTest(test, rulesBefore);



    }

    /**
     * Analysis of the test results and creation of the test report
     *
     * @param test        that was executed
     * @param rulesBefore status of the rules before the test was executed
     */
    private void analyzeTest(TestDetails test, List<Rule> rulesBefore) throws Exception {
        // Check the test for success
        testAnalyzer.testSuccess(test.getId());
        TestDetails testDetails = testDetailsRepository.findOne(test.getId());

        // Create test report with graph of sensor values as a pdf
        graphPlotter.createGraph(testDetails);
        String pdfPath = testReport.generateTestReport(testDetails.getId(), rulesBefore);
        testDetails.setPathPDF(pdfPath);
        testDetails.setPdfExists(true);

        // save success and path of test report to database
        testDetailsRepository.save(testDetails);
    }

    /**
     * Saves the generated values through the test in the repository.
     *
     * @param test      executed test
     * @param valueList generated value list
     */
    private void saveValues(TestDetails test, Map<String, LinkedHashMap<Long, Double>> valueList) {

        Map<String, LinkedHashMap<Long, Double>> valueListTest = new HashMap<>();
        TestDetails testDetails = testDetailsRepository.findOne(test.getId());

        for (Sensor sensor : test.getSensor()) {
            if (testDetails.isUseNewData()) {
                if(valueList.get(sensor.getId())!=null){
                    LinkedHashMap<Long, Double> temp = valueList.get(sensor.getId());
                    valueListTest.put(sensor.getName(), temp);
                    // list.remove(sensor.getId());
                    // save list of sensor values to database
                    testDetails.setSimulationList(valueListTest);
                }

            }
        }
        testDetailsRepository.save(testDetails);

    }


    /**
     * Enable selected Rules, Deploy and Start the Actuator and Sensors of the test
     *
     * @param testDetails specific test to be executed
     */
    public void startTest(TestDetails testDetails) {

        //check if test exists
        if (!testDetailsRepository.exists(testDetails.getId())) {
            new ResponseEntity<>("Test does not exists.", HttpStatus.NOT_FOUND);
            return;
        }

        // Enable rules that belong to the test
        enableRules(testDetails);

        // start Actuator
        startActuator();

        // Checks and starts Sensors w/ right parameter instance list
        sensorService(testDetails);

        new ResponseEntity<>("Test successfully started.", HttpStatus.OK);
    }

    /**
     * Checks if the sensors are rerun sensors, creates the parameter instance list and starts the sensors.
     *
     * @param test test to be executed
     */
    private void sensorService(TestDetails test) {

        List<Sensor> testingSensors = test.getSensor();

        if (!test.isUseNewData()) {
            for (Sensor sensor : testingSensors) {
                List<ParameterInstance> parametersWrapper = new ArrayList<>();
                // If not a sensor simulator

                if (!SIMULATOR_LIST.contains(sensor.getName()) && !sensor.getName().contains(RERUN_IDENTIFIER)) {
                    restDeploymentController.stopSensor(sensor.getId());
                    for (Map.Entry<String, LinkedHashMap<Long, Double>> sensorValues : test.getSimulationList().entrySet()) {
                        if (sensorValues.getKey().equals(sensor.getName())) {
                            // Get parameter values for starting the sensors
                            Map<String, ParameterInstance> parameterValues = createRerunParameters(sensorValues);
                            parametersWrapper.add(parameterValues.get("interval"));
                            parametersWrapper.add(parameterValues.get("value"));
                        }
                    }
                    // Start rerun sensor
                    startSensors(sensor, parametersWrapper, !test.isUseNewData());
                }
            }
        } else {
            // start sensor of the test
            for (Sensor sensor : testingSensors) {
                String sensorName = sensor.getName();
                for (List<ParameterInstance> configSensor : test.getConfig()) {
                    for (ParameterInstance parameterInstance : configSensor) {
                        if (parameterInstance.getName().equals(CONFIG_SENSOR_NAME_KEY) && parameterInstance.getValue().equals(sensorName)) {
                            // start the sensor with the right corresponding configuration
                            startSensors(sensor, configSensor, !test.isUseNewData());
                        }
                    }
                }
            }
        }
    }


    /**
     * Starts rerun sensors of the test with the right parameter instances.
     *
     * @param testSensor      to be started (rerun)
     * @param parameterValues parameter Instances for the start of the rerun sensor
     */
    public void startSensors(Sensor testSensor, List<ParameterInstance> parameterValues, Boolean rerunSensor) {
        String rerun = "";
        if (rerunSensor == true) {
            rerun = RERUN_IDENTIFIER;
        }
        Sensor startSensor = sensorRepository.findByName(rerun + testSensor.getName());
        ResponseEntity<Boolean> sensorDeployed = restDeploymentController.isRunningSensor(startSensor.getId());

        if (!sensorDeployed.getBody()) {
            //if not deploy Sensor
            restDeploymentController.deploySensor((startSensor.getId()));
        }
        restDeploymentController.stopSensor(startSensor.getId());
        restDeploymentController.startSensor(startSensor.getId(), parameterValues);
    }


    /**
     * Creates the List of Parameter Instances for start of the rerun sensors.
     *
     * @param sensorValues map of all sensor values for the rerun test
     * @return Map of parameter Instances to start the rerun sensor
     */
    public Map<String, ParameterInstance> createRerunParameters(Map.Entry<String, LinkedHashMap<Long, Double>> sensorValues) {
        Map<String, ParameterInstance> parameterValues = new HashMap<>();
        ParameterInstance interval = new ParameterInstance();
        ParameterInstance value = new ParameterInstance();

        StringBuilder intervalString = new StringBuilder("[");
        StringBuilder valueString = new StringBuilder("[");
        String comma = "-";


        for (Iterator<Map.Entry<Long, Double>> iterator = sensorValues.getValue().entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<Long, Double> intervalValues = iterator.next();
            if (!iterator.hasNext()) {
                comma = "";
            }
            intervalString.append(intervalValues.getKey().toString()).append(comma);
            valueString.append(intervalValues.getValue().toString()).append(comma);
        }

        // complete Strings with bracket
        intervalString.append("]");
        valueString.append("]");

        // fill the Parameter instances
        interval.setName("interval");
        interval.setValue(intervalString.toString());

        value.setName("value");
        value.setValue(valueString.toString());

        // Put the created parameter instances into a Map
        parameterValues.put("interval", interval);
        parameterValues.put("value", value);

        return parameterValues;
    }

    /**
     * Checks if the actuator simulator for the test is deployed and started. If not, this is now done here.
     */
    private void startActuator() {
        //check if actuator is deployed
        Actuator testingActuator = actuatorRepository.findByName(TESTING_ACTUATOR);

        ResponseEntity<Boolean> actuatorDeployed = restDeploymentController.isRunningActuator(testingActuator.getId());
        if (!actuatorDeployed.getBody()) {
            //if false deploy actuator
            restDeploymentController.deployActuator(testingActuator.getId());
        }
        // start the Actuator
        restDeploymentController.startActuator(testingActuator.getId(), new ArrayList<>());
    }

    /**
     * Enables every rule corresponding to the IoT-Application to be tested.
     *
     * @param test to be be executed
     */
    private void enableRules(TestDetails test) {
        // Get a list of every rule corresponding to the application
        List<Rule> rules = testAnalyzer.getCorrespondingRules(test);

        //enable the selected rules for the test
        for (Rule rule : rules) {
            restRuleController.enableRule(rule.getId());
        }
    }

    /**
     * Stops all sensors of a test, to stop the test.
     *
     * @param testId of the test to be stopped
     */
    public void stopTest(String testId) {
        TestDetails test = testDetailsRepository.findById(testId);
        // Stop every sensor running for the specific test
        for (Sensor sensor : test.getSensor()) {
            restDeploymentController.stopSensor(sensor.getId());
        }
    }
}