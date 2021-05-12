package de.ipvs.as.mbp.service.testing.executor;

import de.ipvs.as.mbp.domain.component.Actuator;
import de.ipvs.as.mbp.domain.component.Sensor;
import de.ipvs.as.mbp.domain.operator.parameters.ParameterInstance;
import de.ipvs.as.mbp.domain.rules.Rule;
import de.ipvs.as.mbp.domain.testing.TestDetails;
import de.ipvs.as.mbp.domain.testing.TestReport;
import de.ipvs.as.mbp.repository.ActuatorRepository;
import de.ipvs.as.mbp.repository.TestDetailsRepository;
import de.ipvs.as.mbp.repository.TestReportRepository;
import de.ipvs.as.mbp.service.deployment.DeployerDispatcher;
import de.ipvs.as.mbp.service.deployment.IDeployer;
import de.ipvs.as.mbp.service.rules.RuleEngine;
import de.ipvs.as.mbp.service.testing.PropertiesService;
import de.ipvs.as.mbp.service.testing.analyzer.TestAnalyzer;
import de.ipvs.as.mbp.web.rest.RestDeploymentController;
import de.ipvs.as.mbp.web.rest.helper.DeploymentWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Starts the test and all its components. Then analyzes the results.
 */
@Component
public class TestExecutor {

    @Autowired
    private TestReportRepository testReportRepository;

    @Autowired
    private TestDetailsRepository testDetailsRepository;

    @Autowired
    private ActuatorRepository actuatorRepository;

    @Autowired
    private RestDeploymentController restDeploymentController;

    @Autowired
    RuleEngine ruleEngine;

    @Autowired
    private TestAnalyzer testAnalyzer;

    @Autowired
    private PropertiesService propertiesService;

    @Autowired
    private DeploymentWrapper deploymentWrapper;

    @Autowired
    private DeployerDispatcher deployerDispatcher;


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


    public TestExecutor() throws IOException {
        propertiesService = new PropertiesService();
        this.RERUN_IDENTIFIER =
                propertiesService.getPropertiesString("testingTool.RerunIdentifier");
        this.TESTING_ACTUATOR =
                propertiesService.getPropertiesString("testingTool.actuatorName");
        this.CONFIG_SENSOR_NAME_KEY =
                propertiesService
                        .getPropertiesString("testingTool.ConfigSensorNameKey");
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
     * Sets a list of all active tests.
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
        Map<String, LinkedHashMap<Long, Double>> list =
                testAnalyzer.getTestValues();

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
    public void executeTest(TestDetails test)  {

        TestReport testReport = new TestReport();
        try{
            // Set the exact start time of the test
            testReport.setName(test.getName());
            testReport.setStartTestTimeNow();
            testReport.setConfig(getTestReportConfig(test));
            testReport.setRules(test.getRules());
            testReport.setRuleNames(test.getRuleNames());
            testReport.setSensor(test.getSensor());
            testReport.setTriggerRules(test.isTriggerRules());
            // get  information about the status of the rules before the execution of the test
            List<Rule>  rulesBefore = testAnalyzer.getCorrespondingRules(test);
            testReport.setRuleInformationBefore(rulesBefore);
            String reportId = testReportRepository.save(testReport).getId();

            // add test and sensors to the activation list
            activateTest(test);

            // start all components relevant for the test
            startTest(testDetailsRepository.findById(test.getId()).get());

            // Get List of all simulated Values
            Map<String, LinkedHashMap<Long, Double>> valueList =
                    testAnalyzer.isFinished(reportId, test.getId());

            saveRulesAfter(test, reportId);
            saveValues(test, reportId, valueList);
            analyzeTest(test, reportId, rulesBefore);
        } catch (Exception e){
            testReport.setEndTestTimeNow();
            testReport.setSuccessful("ERROR DURING TEST");
            List<Rule>  rulesAfter = testAnalyzer.getCorrespondingRules(test);
            testReport.setRuleInformationAfter(rulesAfter);
            testReportRepository.save(testReport);

        }



    }

    private void saveRulesAfter(TestDetails test, String reportId) {
        List<Rule>  rulesAfter = testAnalyzer.getCorrespondingRules(test);
        TestReport report = testReportRepository.findById(reportId).get();
        report.setRuleInformationAfter(rulesAfter);
        testReportRepository.save(report);
    }

    private List<List<ParameterInstance>> getTestReportConfig(TestDetails test) {
        List<List<ParameterInstance>> reportConfig = new ArrayList<>();

        for (int i = 0; i < test.getConfig().size(); i++) {
            List<ParameterInstance> ojh = test.getConfig().get(i);
            List<ParameterInstance> simul = ojh.stream().filter(item -> item.getValue().toString().contains("TESTING_")).collect(Collectors.toList());
            if(ojh.size() > 0){
                if (simul.size() > 0) {
                    List<ParameterInstance> newConfig;
                    newConfig = convertConfigInstances(ojh);
                    reportConfig.add(newConfig);
                } else{
                    reportConfig.add(ojh);
                }
            }


        }
        return reportConfig;


    }

    private List<ParameterInstance> convertConfigInstances(List<ParameterInstance> configInstance) {
        List<ParameterInstance> convertedConfig = new ArrayList<>();
        ParameterInstance type = null;
        for (ParameterInstance instance : configInstance) {
            Object i = instance.getValue();
            if (instance.getName().equals("ConfigName")) {
                type = getSensorType(instance.getValue());
                convertedConfig.add(type);
            } else if (instance.getName().equals("event")) {
                convertedConfig.add(getEventType(type.getValue().toString(),Integer.parseInt(instance.getValue().toString())));
            } else if (instance.getName().equals("anomaly")) {
                convertedConfig.add(getAnomalyType(Integer.parseInt(instance.getValue().toString())));
            }
        }

        return convertedConfig;
    }

    private ParameterInstance getEventType(String type, int event) {
        ParameterInstance eventType = new ParameterInstance();
        String simType ="";
        if(type.equals("Temperature")){
            simType ="Temperature";
        } else {
            simType ="Humidity";
        }

        eventType.setName("eventType");
        if (event == 1) {
            eventType.setValue(simType +" rise");
        } else if (event == 2) {
            eventType.setValue(simType +" drop");
        } else if(event == 3){
            eventType.setValue("-");
        }
        return eventType;
    }


    private ParameterInstance getAnomalyType(int anomaly) {
        ParameterInstance anomalyType = new ParameterInstance();
        anomalyType.setName("anomalyType");
        if (anomaly == 3) {
            anomalyType.setValue("Outliers");
        } else if (anomaly == 4) {
            anomalyType.setValue("Missing values");
        } else if (anomaly == 5) {
            anomalyType.setValue("Wrong value type");
        } else {
            anomalyType.setValue("No combination");
        }
        return anomalyType;
    }


    private ParameterInstance getSensorType(Object sensorName) {
        String name = String.valueOf(sensorName);
        ParameterInstance sensorType = new ParameterInstance();
        sensorType.setName("Type");
        if (name.contains("Temperature")) {
            sensorType.setValue("Temperature");
        } else {
            sensorType.setValue("Humidity");
        }
        return sensorType;
    }

    /**
     * Analysis of the test results and creation of the test report.
     *
     * @param test        that was executed
     * @param rulesBefore status of the rules before the test was executed
     */
    private void analyzeTest(TestDetails test, String reportId, List<Rule> rulesBefore) {
        // Check the test for success
        testAnalyzer.testSuccess(test.getId(), reportId);
        TestDetails testDetails =
                testDetailsRepository.findById(test.getId()).get();


        // save success and path of test report to database
        testDetailsRepository.save(testDetails);
    }

    /**
     * Saves the generated values through the test in the repository.
     *
     * @param test      executed test
     * @param valueList generated value list
     */
    private void saveValues(TestDetails test, String reportId,
                            Map<String, LinkedHashMap<Long, Double>> valueList) {

        Map<String, LinkedHashMap<Long, Double>> valueListTest = new HashMap<>();
        TestReport testReport = testReportRepository.findById(reportId).get();
        TestDetails testDetails =
                testDetailsRepository.findById(test.getId()).get();

        for (Sensor sensor : test.getSensor()) {
            if (testDetails.isUseNewData()) {
                if (valueList.get(sensor.getId()) != null) {
                    LinkedHashMap<Long, Double> temp = valueList.get(sensor.getId());
                    valueListTest.put(sensor.getName(), temp);
                    // list.remove(sensor.getId());
                    // save list of sensor values to database
                    testReport.setSimulationList(valueList);
                    testDetails.setSimulationList(valueList);
                }

            }
        }
        testReportRepository.save(testReport);
        testDetailsRepository.save(testDetails);

    }


    /**
     * Enable selected Rules, Deploy and Start the Actuator and Sensors of the
     * test.
     *
     * @param testDetails specific test to be executed
     */
    public void startTest(TestDetails testDetails) {

        //check if test exists
        if (testDetailsRepository.findByName(testDetails.getName()) == null) {
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


                if (!SIMULATOR_LIST.contains(sensor.getName()) &&
                        sensor.getName().contains(RERUN_IDENTIFIER)) {
                    deploymentWrapper.stopComponent(sensor);
                    for (Map.Entry<String, LinkedHashMap<Long, Double>> sensorValues : test
                            .getSimulationList().entrySet()) {
                        if (sensor.getName().contains(sensorValues.getKey())) {
                            // Get parameter values for starting the sensors
                            Map<String, ParameterInstance> parameterValues =
                                    createRerunParameters(sensorValues);
                            parametersWrapper.add(parameterValues.get("interval"));
                            parametersWrapper.add(parameterValues.get("value"));
                        }
                    }
                    // Start rerun sensor
                    startSensors(sensor, parametersWrapper);
                }
            }
        } else {
            // start sensor of the test
            for (Sensor sensor : testingSensors) {
                String sensorName = sensor.getName();
                for (List<ParameterInstance> configSensor : test.getConfig()) {
                    for (ParameterInstance parameterInstance : configSensor) {
                        if (parameterInstance.getName().equals(CONFIG_SENSOR_NAME_KEY) &&
                                parameterInstance.getValue().equals(sensorName)) {
                            // start the sensor with the right corresponding configuration
                            startSensors(sensor, configSensor);
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
    public void startSensors(Sensor testSensor,
                             List<ParameterInstance> parameterValues) {


        IDeployer deployer = deployerDispatcher.getDeployer();

        if (!deployer.isComponentDeployed(testSensor)) {
            //if not deploy Sensor
            deployer.deployComponent(testSensor);
        }

        if(deployer.isComponentRunning(testSensor)){
            deploymentWrapper.stopComponent(testSensor);
        }
        deploymentWrapper.startComponent(testSensor, parameterValues);
    }


    /**
     * Creates the List of Parameter Instances for start of the rerun sensors.
     *
     * @param sensorValues map of all sensor values for the rerun test
     * @return Map of parameter Instances to start the rerun sensor
     */
    public Map<String, ParameterInstance> createRerunParameters(
            Map.Entry<String, LinkedHashMap<Long, Double>> sensorValues) {
        Map<String, ParameterInstance> parameterValues = new HashMap<>();
        ParameterInstance interval = new ParameterInstance();
        ParameterInstance value = new ParameterInstance();

        StringBuilder intervalString = new StringBuilder("[");
        StringBuilder valueString = new StringBuilder("[");
        String comma = "-";


        for (Iterator<Map.Entry<Long, Double>> iterator =
             sensorValues.getValue().entrySet().iterator();
             iterator.hasNext(); ) {
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
        Actuator testingActuator =
                actuatorRepository.findByName(TESTING_ACTUATOR).get();

        testingActuator.getId();
        IDeployer deployer = deployerDispatcher.getDeployer();
        if (!deployer.isComponentDeployed(testingActuator)) {
            //if false deploy actuator
            deployer.deployComponent(testingActuator);
        }
        if(!deployer.isComponentRunning(testingActuator)){
            // start the Actuator
            deploymentWrapper.startComponent(testingActuator, new ArrayList<>());
        }
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
            ruleEngine.enableRule(rule);
        }
    }

    /**
     * Stops all sensors of a test, to stop the test.
     *
     * @param testId of the test to be stopped
     */
    public void stopTest(String testId) {
        TestDetails test = testDetailsRepository.findById(testId).get();
        // Stop every sensor running for the specific test
        for (Sensor sensor : test.getSensor()) {
            deploymentWrapper.stopComponent(sensor);
        }
    }

}