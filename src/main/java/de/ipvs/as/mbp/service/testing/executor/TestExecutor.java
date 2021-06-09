package de.ipvs.as.mbp.service.testing.executor;

import de.ipvs.as.mbp.domain.component.Actuator;
import de.ipvs.as.mbp.domain.component.Sensor;
import de.ipvs.as.mbp.domain.operator.parameters.ParameterInstance;
import de.ipvs.as.mbp.domain.rules.Rule;
import de.ipvs.as.mbp.domain.testing.TestDetails;
import de.ipvs.as.mbp.domain.testing.TestReport;
import de.ipvs.as.mbp.repository.*;
import de.ipvs.as.mbp.service.deployment.DeployerDispatcher;
import de.ipvs.as.mbp.service.deployment.IDeployer;
import de.ipvs.as.mbp.service.rules.RuleEngine;
import de.ipvs.as.mbp.service.testing.PropertiesService;
import de.ipvs.as.mbp.service.testing.analyzer.TestAnalyzer;
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
    RuleEngine ruleEngine;

    @Autowired
    private TestAnalyzer testAnalyzer;

    @Autowired
    private final PropertiesService propertiesService;

    @Autowired
    private RuleRepository ruleRepository;

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
     * Puts the test and the corresponding sensors into a list and resets the list of values that where previously saved in other test executions.
     */
    public void activateTest(List<Sensor> testSensors, String testId, Boolean useNewData) {
        Optional<TestDetails> testDetailsOptional = testDetailsRepository.findById(testId);

        if (testDetailsOptional.isPresent()) {
            TestDetails test = testDetailsOptional.get();
            Map<String, TestDetails> activeTests = getActiveTests();
            Map<String, LinkedHashMap<Long, Double>> list =
                    testAnalyzer.getTestValues();

            if (useNewData) {
                for (Sensor sensor : testSensors) {
                    if (!sensor.getName().contains(RERUN_IDENTIFIER)) {
                        activeTests.put(sensor.getId(), test);
                        list.remove(sensor.getId());
                    }

                }
            } else {
                for (Sensor sensor : testSensors) {
                    if (sensor.getName().contains(RERUN_IDENTIFIER)) {
                        activeTests.put(sensor.getId(), test);
                        list.remove(sensor.getId());
                    }
                }
            }

            setActiveTests(activeTests);
            testAnalyzer.setTestValues(list);
        }

    }

    /**
     * Removes the test and the corresponding sensors from the list of activated sensors/tests.
     *
     * @param testSensors list of sensors included into de specific test to deactivate
     * @param useNewData  information if new sensor data should be generated or not
     */
    public void deactivateTest(List<Sensor> testSensors, Boolean useNewData) {
        Map<String, TestDetails> activeTests = getActiveTests();
        Map<String, LinkedHashMap<Long, Double>> list = testAnalyzer.getTestValues();

        if (useNewData) {
            for (Sensor sensor : testSensors) {
                if (!sensor.getName().contains(RERUN_IDENTIFIER)) {
                    activeTests.remove(sensor.getId());
                }
            }
        } else {
            for (Sensor sensor : testSensors) {
                if (sensor.getName().contains(RERUN_IDENTIFIER)) {
                    activeTests.remove(sensor.getId());
                }
            }
        }

        setActiveTests(activeTests);
        testAnalyzer.setTestValues(list);
    }

    /**
     * Reruns a specific test execution with the same configurations an sensor values.
     *
     * @param test         tests to be repeated
     * @param testReportId test report with all needed information for the repetition
     */
    public void rerunTest(TestDetails test, String testReportId) {
        try {
            Optional<TestReport> oldReportOptional = testReportRepository.findById(testReportId);

            if (oldReportOptional.isPresent()) {
                TestReport oldReport = oldReportOptional.get();
                String reportId = setReportInformation(test, oldReport);

                if(testReportRepository.findById(reportId).isPresent()){
                    TestReport updatedReport = testReportRepository.findById(reportId).get();
                    // add test and sensors to the activation list
                    activateTest(updatedReport.getSensor(), test.getId(), false);

                    // Enable rules that belong to the test
                    enableRules(test);

                    // start Actuator
                    startActuator();

                    // start Sensors
                    sensorRerunService(updatedReport, oldReport.getSimulationList());

                    // Get List of all simulated Values
                    Map<String, LinkedHashMap<Long, Double>> valueList = testAnalyzer.isFinished(reportId, test.getId(), false);

                    List<Rule> rulesBefore = testAnalyzer.getCorrespondingRules(updatedReport.getRules(), updatedReport.getSensor());
                    saveAmountRulesTriggered(reportId, rulesBefore);
                    saveValues(test, reportId, valueList);

                    deactivateTest(test.getSensor(), false);
                    // Check the test for success
                    testAnalyzer.testSuccess(test.getId(), reportId);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * Adds and saves the specific report information for a test rerun.
     *
     * @param test to be executed
     * @param oldReport report and configurations of the test to be repeated.
     * @return Id of the generated test report
     */
    private String setReportInformation(TestDetails test, TestReport oldReport) {
        TestReport testReport = new TestReport();

        // Get information to add
        List<Sensor> rerunSensors = getRerunSensorsReport(test);
        List<Rule> rerunRules = getRerunRulesReport(oldReport.getRules());
        List<String> rerunRuleNames = getRerunRuleNamesReport(oldReport.getRuleNames());

        // enrich the test report with the specific information
        testReport.setName(test.getName());
        testReport.setStartTestTimeNow();
        testReport.setSensor(rerunSensors);
        testReport.setConfig(oldReport.getConfig());
        testReport.setRules(rerunRules);
        testReport.setRuleNames(rerunRuleNames);
        testReport.setTriggerRules(oldReport.isTriggerRules());
        testReport.setUseNewData(false);

        // get  information about the status of the rules before the execution of the test
        List<Rule> rulesBefore = testAnalyzer.getCorrespondingRules(testReport.getRules(), testReport.getSensor());
        testReport.setRuleInformationBefore(rulesBefore);
        return testReportRepository.save(testReport).getId();

    }

    /**
     * Returns a list of rule names for the rerun tests filtered by the rerun identifier.
     *
     * @param ruleNames list of all rule names of the test to be filtered
     * @return list of all rerun rule names of the test
     */
    private List<String> getRerunRuleNamesReport(List<String> ruleNames) {
        List<String> rerunRuleNames = new ArrayList<>();

        for (String ruleName : ruleNames) {
            rerunRuleNames.add(RERUN_IDENTIFIER + ruleName);
        }
        return rerunRuleNames;
    }


    /**
     * Returns a list of rules for the rerun tests filtered by the rerun identifier.
     *
     * @param testRules list of all rules of the test to be filtered
     * @return list of all rerun rules of the test
     */
    private List<Rule> getRerunRulesReport(List<Rule> testRules) {
        List<Rule> rerunRules = new ArrayList<>();

        for (Rule rule : testRules) {
            if (ruleRepository.findByName(RERUN_IDENTIFIER + rule.getName()).isPresent()) {
                rerunRules.add(ruleRepository.findByName(RERUN_IDENTIFIER + rule.getName()).get());
            }
        }

        return rerunRules;
    }

    /**
     * Returns a list of sensors for the rerun tests filtered by the rerun identifier.
     *
     * @param testDetails test sensors to be repeated/executed
     * @return list of sensors to be started for the rerun
     */
    private List<Sensor> getRerunSensorsReport(TestDetails testDetails) {
        List<Sensor> rerunSensors = new ArrayList<>();

        for (Sensor sensor : testDetails.getSensor()) {
            if (sensor.getName().contains(RERUN_IDENTIFIER)) {
                rerunSensors.add(sensor);
            }
        }

        return rerunSensors;
    }


    /**
     *
     *
     * @param testReport
     * @param simulationList
     */
    private void sensorRerunService(TestReport testReport, Map<String, LinkedHashMap<Long, Double>> simulationList) {

        IDeployer deployer = deployerDispatcher.getDeployer();

        for (Sensor sensor : testReport.getSensor()) {
            List<ParameterInstance> parametersWrapper = new ArrayList<>();
            if (!SIMULATOR_LIST.contains(sensor.getName()) &&
                    sensor.getName().contains(RERUN_IDENTIFIER)) {
                if (deployer.isComponentRunning(sensor)) {
                    deployer.stopComponent(sensor);
                }
                for (Map.Entry<String, LinkedHashMap<Long, Double>> sensorValues : simulationList.entrySet()) {
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

    }


    /**
     * Starts the test and saves all values form the sensor.
     *
     * @param test test to be executed
     */
    public void executeTest(TestDetails test) {

        TestReport testReport = new TestReport();
        try {
            // Set the exact start time of the test
            testReport.setName(test.getName());
            testReport.setStartTestTimeNow();
            testReport.setConfig(getTestReportConfig(test));
            testReport.setRules(test.getRules());
            testReport.setRuleNames(test.getRuleNames());
            testReport.setSensor(test.getSensor());
            testReport.setTriggerRules(test.isTriggerRules());
            testReport.setUseNewData(true);
            // get  information about the status of the rules before the execution of the test
            List<Rule> rulesBefore = testAnalyzer.getCorrespondingRules(test.getRules(), test.getSensor());
            testReport.setRuleInformationBefore(rulesBefore);
            String reportId = testReportRepository.save(testReport).getId();

            // add test and sensors to the activation list
            activateTest(test.getSensor(), test.getId(), true);

            // start all components relevant for the test
            startTest(test);

            // Get List of all simulated Values
            Map<String, LinkedHashMap<Long, Double>> valueList =
                    testAnalyzer.isFinished(reportId, test.getId(), true);

            saveAmountRulesTriggered(reportId, rulesBefore);
            saveValues(test, reportId, valueList);
            deactivateTest(test.getSensor(), true);
            // Check the test for success
            testAnalyzer.testSuccess(test.getId(), reportId);
        } catch (Exception e) {
            testReport.setEndTestTimeNow();
            testReport.setSuccessful("ERROR DURING TEST");
            List<Rule> rulesAfter = testAnalyzer.getCorrespondingRules(test.getRules(), test.getSensor());
            saveAmountRulesTriggered(testReport.getId(), rulesAfter);

        }
    }

    private void saveAmountRulesTriggered(String reportId, List<Rule> rulesBefore) {
        Optional<TestReport> reportOptional = testReportRepository.findById(reportId);

        if (reportOptional.isPresent()) {
            TestReport report = reportOptional.get();
            List<Rule> rulesAfter = testAnalyzer.getCorrespondingRules(report.getRules(), report.getSensor());
            Map<String, Integer> amountTriggered = new HashMap<>();
            for (Rule ruleBefore : rulesBefore) {
                for (Rule ruleAfter : rulesAfter) {
                    if (ruleAfter.getName().equals(ruleBefore.getName())) {
                        amountTriggered.put(ruleAfter.getName(), ruleAfter.getExecutions() - ruleBefore.getExecutions());
                    }
                }
            }
            report.setAmountRulesTriggered(amountTriggered);
            testReportRepository.save(report);
        }
    }

    private List<List<ParameterInstance>> getTestReportConfig(TestDetails test) {
        List<List<ParameterInstance>> reportConfig = new ArrayList<>();

        for (int i = 0; i < test.getConfig().size(); i++) {
            List<ParameterInstance> configurations = test.getConfig().get(i);
            List<ParameterInstance> simulators = configurations.stream().filter(item -> item.getValue().toString().contains("TESTING_")).collect(Collectors.toList());
            if (configurations.size() > 0) {
                if (simulators.size() > 0) {
                    List<ParameterInstance> newConfig;
                    newConfig = convertConfigInstances(configurations);
                    reportConfig.add(newConfig);
                } else {
                    reportConfig.add(configurations);
                }
            }


        }
        return reportConfig;


    }

    private List<ParameterInstance> convertConfigInstances(List<ParameterInstance> configInstance) {
        List<ParameterInstance> convertedConfig = new ArrayList<>();
        ParameterInstance type = null;
        for (ParameterInstance instance : configInstance) {
            switch (instance.getName()) {
                case "ConfigName":
                    type = getSensorType(instance.getValue());
                    convertedConfig.add(type);
                    break;
                case "event":
                    assert type != null;
                    convertedConfig.add(getEventType(type.getValue().toString(), Integer.parseInt(instance.getValue().toString())));
                    break;
                case "anomaly":
                    convertedConfig.add(getAnomalyType(Integer.parseInt(instance.getValue().toString())));
                    break;
            }
        }

        return convertedConfig;
    }

    private ParameterInstance getEventType(String type, int event) {
        ParameterInstance eventType = new ParameterInstance();
        String simType;
        if (type.equals("Temperature")) {
            simType = "Temperature";
        } else {
            simType = "Humidity";
        }

        eventType.setName("eventType");
        if (event == 1) {
            eventType.setValue(simType + " rise");
        } else if (event == 2) {
            eventType.setValue(simType + " drop");
        } else if (event == 3) {
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
     * Saves the generated values through the test in the repository.
     *
     * @param testDetails executed test
     * @param valueList   generated value list
     */
    private void saveValues(TestDetails testDetails, String reportId,
                            Map<String, LinkedHashMap<Long, Double>> valueList) {
        Map<String, LinkedHashMap<Long, Double>> valueListTest = new HashMap<>();

        //Try to find specific test report and test
        Optional<TestReport> testReportOptional = testReportRepository.findById(reportId);

        if (testReportOptional.isPresent()) {
            TestReport testReport = testReportOptional.get();

            for (Sensor sensor : testReport.getSensor()) {
                if (valueList.get(sensor.getId()) != null) {
                    LinkedHashMap<Long, Double> temp = valueList.get(sensor.getId());
                    valueListTest.put(sensor.getName(), temp);
                    // list.remove(sensor.getId());
                    // save list of sensor values to database
                    testReport.setSimulationList(valueListTest);
                    testDetails.setSimulationList(valueListTest);
                }

            }

            testReportRepository.save(testReport);
            testDetailsRepository.save(testDetails);
        }


    }


    /**
     * Enable selected Rules, Deploy and Start the Actuator and Sensors of the
     * test.
     *
     * @param testDetails specific test to be executed
     */
    public void startTest(TestDetails testDetails) {

        //check if test exists
        if (!testDetailsRepository.findByName(testDetails.getName()).isPresent()) {
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


    /**
     * Starts rerun sensors of the test with the right parameter instances.
     *
     * @param testSensor      to be started (rerun)
     * @param parameterValues parameter Instances for the start of the rerun sensor
     */
    public void startSensors(Sensor testSensor,
                             List<ParameterInstance> parameterValues) {

        final IDeployer deployer = deployerDispatcher.getDeployer();

        if (!deployer.isComponentDeployed(testSensor)) {
            //if not deploy Sensor
            deployer.deployComponent(testSensor);
        }

        if (deployer.isComponentRunning(testSensor)) {
            deployer.stopComponent(testSensor);
        }
        deployer.startComponent(testSensor, parameterValues);
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

        final IDeployer deployer = deployerDispatcher.getDeployer();
        //Try to find specific test report and test
        Optional<Actuator> testingActuatorOptional = actuatorRepository.findByName(TESTING_ACTUATOR);

        if (testingActuatorOptional.isPresent()) {
            Actuator testingActuator = testingActuatorOptional.get();

            //check if actuator is deployed
            if (!deployer.isComponentDeployed(testingActuator)) {
                //if false deploy actuator
                deployer.deployComponent(testingActuator);
            }
            if (!deployer.isComponentRunning(testingActuator)) {
                // start the Actuator
                deployer.startComponent(testingActuator, new ArrayList<>());
            }
        }
    }

    /**
     * Enables every rule corresponding to the IoT-Application to be tested.
     *
     * @param test to be be executed
     */
    private void enableRules(TestDetails test) {
        // Get a list of every rule corresponding to the application
        List<Rule> rules = testAnalyzer.getCorrespondingRules(test.getRules(), test.getSensor());

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
        final IDeployer deployer = deployerDispatcher.getDeployer();
        Optional<TestDetails> testDetailsOptional = testDetailsRepository.findById(testId);

        if (testDetailsOptional.isPresent()) {
            TestDetails test = testDetailsOptional.get();
            // Stop every sensor running for the specific test
            for (Sensor sensor : test.getSensor()) {
                if (deployer.isComponentRunning(sensor)) {
                    deployer.stopComponent(sensor);
                }
            }
        }

    }

}