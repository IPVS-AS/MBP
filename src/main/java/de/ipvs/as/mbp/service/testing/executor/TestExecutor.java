package de.ipvs.as.mbp.service.testing.executor;

import de.ipvs.as.mbp.domain.component.Actuator;
import de.ipvs.as.mbp.domain.component.Sensor;
import de.ipvs.as.mbp.domain.operator.parameters.ParameterInstance;
import de.ipvs.as.mbp.domain.rules.Rule;
import de.ipvs.as.mbp.domain.settings.Settings;
import de.ipvs.as.mbp.domain.testing.TestDetails;
import de.ipvs.as.mbp.domain.testing.TestReport;
import de.ipvs.as.mbp.domain.valueLog.ValueLog;
import de.ipvs.as.mbp.repository.*;
import de.ipvs.as.mbp.service.deployment.DeployerDispatcher;
import de.ipvs.as.mbp.service.deployment.IDeployer;
import de.ipvs.as.mbp.service.deployment.demo.DemoDeployer;
import de.ipvs.as.mbp.service.rules.RuleEngine;
import de.ipvs.as.mbp.service.settings.SettingsService;
import de.ipvs.as.mbp.service.testing.TestDevicePropertiesService;
import de.ipvs.as.mbp.service.testing.analyzer.TestAnalyzer;
import org.bson.Document;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;
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
    private TestDevicePropertiesService testDevicePropertiesService;

    @Autowired
    private RuleRepository ruleRepository;

    @Autowired
    private DeployerDispatcher deployerDispatcher;

    @Autowired
    private SettingsService settingsService;


    // List of all active Tests
    Map<String, TestDetails> activeTests = new HashMap<>();
    @Value("#{'${testingTool.sensorSimulators}'.split(',')}")
    List<String> SIMULATOR_LIST;

    private final String TESTING_ACTUATOR;
    private final String CONFIG_SENSOR_NAME_KEY;


    //To resolve ${} in @Value
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
        return new PropertySourcesPlaceholderConfigurer();
    }


    public TestExecutor() {
        this.TESTING_ACTUATOR = testDevicePropertiesService.getActuatorName();
        this.CONFIG_SENSOR_NAME_KEY = testDevicePropertiesService.getConfigSensorNameKey();
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
            Map<String, LinkedHashMap<Long, Document>> list =
                    testAnalyzer.getTestValues();


            for (Sensor sensor : testSensors) {
                    activeTests.put(sensor.getId(), test);
                    list.remove(sensor.getId());
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
        Map<String, LinkedHashMap<Long, Document>> list = testAnalyzer.getTestValues();

        setActiveTests(activeTests);
        testAnalyzer.setTestValues(list);
    }

    /**
     * Reruns a specific test execution with the same configurations and sensor values.
     *
     * @param test         tests to be repeated
     * @param testReportId test report with all needed information for the repetition
     */
    public void rerunTest(TestDetails test, String testReportId) {
        // Get the current (old) status of the demonstration mode
        Settings currSetting = settingsService.getSettings();

        // Marker whether the demo mode should be set again to false after testing
        boolean setDemoModeFalseAfterTest = false;

        // Set the demo mode to true if it is not already set to true
        if (!currSetting.isDemoMode()) {
            setDemoModeFalseAfterTest = true;
            currSetting.setDemoMode(true);
            try {
                settingsService.updateSettings(currSetting);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }

        TestReport updatedReport = new TestReport();
        try {
            Optional<TestReport> oldReportOptional = testReportRepository.findById(testReportId);

            if (oldReportOptional.isPresent()) {
                TestReport oldReport = oldReportOptional.get();
                String reportId = setRerunReportInformation(test, oldReport);

                if (testReportRepository.findById(reportId).isPresent()) {
                    updatedReport = testReportRepository.findById(reportId).get();
                    // add test and sensors to the activation list
                    activateTest(updatedReport.getSensor(), test.getId(), true);

                    // Enable rules that belong to the test
                    enableRules(test);

                    // start Actuator
                    startActuator();

                    // start Sensors
                    sensorRerunService(updatedReport, oldReport.getSimulationList());

                    // Get List of all simulated Values
                    Map<String, LinkedHashMap<Long, Document>> valueList = testAnalyzer.isFinished(reportId, test.getId(), false);

                    List<Rule> rulesBefore = testAnalyzer.getCorrespondingRules(updatedReport.getRules(), updatedReport.getSensor());
                    saveAmountRulesTriggered(reportId, rulesBefore);
                    saveValues(test, reportId, valueList);

                    deactivateTest(test.getSensor(), false);
                    // Check the test for success
                    testAnalyzer.testSuccess(test.getId(), reportId);
                }
            }
        } catch (Exception e) {
            updatedReport.setEndTestTimeNow();
            updatedReport.setSuccessful("ERROR DURING TEST");
            List<Rule> rulesAfter = testAnalyzer.getCorrespondingRules(test.getRules(), test.getSensor());
            saveAmountRulesTriggered(updatedReport.getId(), rulesAfter);
        } finally {
            // Set the demo mode again to false if test finsihed or an error occured and it was set to false before the rerun
            if (setDemoModeFalseAfterTest) {
                currSetting.setDemoMode(false);
                try {
                    settingsService.updateSettings(currSetting);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * Adds and saves the specific report information for a test rerun.
     *
     * @param test      to be executed
     * @param oldReport report and configurations of the test to be repeated.
     * @return Id of the generated test report
     */
    private String setRerunReportInformation(TestDetails test, TestReport oldReport) {
        TestReport testReport = new TestReport();

        // Get information to add
        //List<Sensor> rerunSensors = getRerunSensorsReport(test);
        List<Sensor> rerunSensors = oldReport.getSensor();
        //List<Rule> rerunRules = getRerunRulesReport(oldReport.getRules());
        List<Rule> rerunRules = oldReport.getRules();
        //List<String> rerunRuleNames = getRerunRuleNamesReport(oldReport.getRuleNames());
        List<String> rerunRuleNames = oldReport.getRuleNames();

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

    private String setReportInformation(TestDetails testDetails) {
        TestReport testReport = new TestReport();
        String reportId = testReport.getId();

        try {
            // Set the exact start time of the test
            testReport.setName(testDetails.getName());
            testReport.setStartTestTimeNow();
            testReport.setConfig(getTestReportConfig(testDetails));
            testReport.setRules(testDetails.getRules());
            testReport.setRuleNames(testDetails.getRuleNames());
            testReport.setSensor(testDetails.getSensor());
            testReport.setTriggerRules(testDetails.isTriggerRules());
            testReport.setUseNewData(true);
            // get  information about the status of the rules before the execution of the test
            List<Rule> rulesBefore = testAnalyzer.getCorrespondingRules(testDetails.getRules(), testDetails.getSensor());
            testReport.setRuleInformationBefore(rulesBefore);
            reportId = testReportRepository.save(testReport).getId();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return reportId;
    }

    /**
     * Reruns a specific test execution under the same conditions and with the same values.
     *
     * @param testReport     information about the test to be repeated
     * @param simulationList List of values generated during the test which should be repeated
     */
    private void sensorRerunService(TestReport testReport, Map<String, LinkedHashMap<Long, Document>> simulationList) {

        IDeployer deployer = deployerDispatcher.getDeployer();
        DemoDeployer demoDeployer = (DemoDeployer) deployer;

        for (Sensor sensor : testReport.getSensor()) {

            // Check if the component is still running and if yes stop it
            if (demoDeployer.isComponentRunning(sensor)) {
                demoDeployer.stopComponent(sensor);
            }

            for (Map.Entry<String, LinkedHashMap<Long, Document>> sensorValues : simulationList.entrySet()) {
                if (sensor.getName().contains(sensorValues.getKey())) {
                    demoDeployer.addRerunValueLogsForComponent(sensor, getRerunQueueForValueLogs(sensorValues.getValue(), sensor));
                }
            }

            startSensors(sensor, new ArrayList<>());
        }
    }

    /**
     * Transforms a Map of Long and Documents to a ValueLog and puts it into a queue so that
     * the {@link DemoDeployer} can use this queue to resend old sensor data.
     *
     * @param toTransform The Map to transform to a queue.
     * @param sensor      The sensor to which the data belongs
     * @return A queue with ValueLogs
     */
    private Queue<ValueLog> getRerunQueueForValueLogs(Map<Long, Document> toTransform, Sensor sensor) {
        Queue<ValueLog> retQueue = new LinkedList<>();

        // Transform the toTransform map to a list for sorting purposes
        List<Map.Entry<Long, Document>> sortedValueLogs = new ArrayList<>(toTransform.entrySet());

        // Sort the queue based on the long entries (which are meant to be timestamps)
        sortedValueLogs.sort(Comparator.comparingLong(Map.Entry::getKey));

        // Fill the queue with ValueLogs
        for (Map.Entry<Long, Document> e : sortedValueLogs) {
            ValueLog newLog = new ValueLog();
            newLog.setValue(e.getValue());
            newLog.setComponent(sensor.getComponentTypeName());
            newLog.setMessage("Testing tool rerun");
            newLog.setTopic("sensor/" + sensor.getId());
            newLog.setIdref(sensor.getId());
            newLog.setQos(0);
            newLog.setTime(Instant.now());

            retQueue.add(newLog);
        }

        return retQueue;
    }


    /**
     * Starts the test and saves all values form the sensor.
     *
     * @param test test to be executed
     */
    public void executeTest(TestDetails test) {
        TestReport testReport = new TestReport();
        try {
            String reportId = setReportInformation(test);

            // add test and sensors to the activation list
            activateTest(test.getSensor(), test.getId(), true);

            // start all components relevant for the test
            startTest(test);

            // Get List of all simulated Values
            Map<String, LinkedHashMap<Long, Document>> valueList = testAnalyzer.isFinished(reportId, test.getId(), true);

            List<Rule> rulesBefore = testAnalyzer.getCorrespondingRules(test.getRules(), test.getSensor());
            saveAmountRulesTriggered(reportId, rulesBefore);
            saveValues(test, reportId, valueList);
            deactivateTest(test.getSensor(), false);
            testAnalyzer.testSuccess(test.getId(), reportId);
        } catch (Exception e) {
            testReport.setEndTestTimeNow();
            testReport.setSuccessful("ERROR DURING TEST");
            List<Rule> rulesAfter = testAnalyzer.getCorrespondingRules(test.getRules(), test.getSensor());
            stopTest(test.getId());
            saveAmountRulesTriggered(testReport.getId(), rulesAfter);

        }
    }

    /**
     * Calculate and save the amount of rule executions during the test for the corresponding rules of the application.
     *
     * @param reportId    of the report in which to save the test execution information
     * @param rulesBefore information about the state of the rules before the test execution
     */
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

    /**
     * Returns the converted configuration of the test.
     *
     * @param test of which the configuration should be converted.
     * @return converted configuration of the test
     */
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

    /**
     * Converts the configuration instances of the sensor simulators.
     *
     * @param configInstance to be converted
     * @return converted parameter instance list
     */
    private List<ParameterInstance> convertConfigInstances(List<ParameterInstance> configInstance) {
        List<ParameterInstance> convertedConfig = new ArrayList<>();
        ParameterInstance type = null;
        ParameterInstance eventInstance = null;
        ParameterInstance anomalyInstance = null;
        int event;
        for (ParameterInstance instance : configInstance) {
            switch (instance.getName()) {
                case "ConfigName":
                    type = getSensorType(instance.getValue());
                    convertedConfig.add(type);
                    break;
                case "event":
                    assert type != null;
                    eventInstance = instance;
                    convertedConfig.add(getEventType(type.getValue().toString(), Integer.parseInt(instance.getValue().toString())));
                    break;
                case "anomaly":
                    anomalyInstance = instance;
                    break;
            }
        }

        assert eventInstance != null;
        event = Integer.parseInt(eventInstance.getValue().toString());
        if (event == 1 || event == 2) {
            convertedConfig.add(getAnomalyType(Integer.parseInt(anomalyInstance.getValue().toString())));
        } else {
            convertedConfig.add(getAnomalyType(Integer.parseInt(eventInstance.getValue().toString())));
        }

        return convertedConfig;
    }


    /**
     * Converts the configuration event numbers into human readable configuration types to display them in the test report.
     *
     * @param type  type of simulator (temperature / humidity)
     * @param event number of event to be simulated defined by the user
     * @return human readable event type
     */
    private ParameterInstance getEventType(String type, int event) {
        ParameterInstance eventType = new ParameterInstance();
        String simType = "Temperature".equals(type) ? "Temperature" : "Humidity";

        eventType.setName("eventType");
        switch (event) {
            case 1:
                eventType.setValue(simType + " rise");
                break;
            case 2:
                eventType.setValue(simType + " drop");
                break;
            default:
                eventType.setValue("-");
                break;
        }
        return eventType;
    }


    /**
     * Converts the configuration anomaly numbers into human readable configuration types to display them in the test report.
     *
     * @param anomaly to be simulated defined by the user
     * @return human readable anomaly types
     */
    private ParameterInstance getAnomalyType(int anomaly) {
        ParameterInstance anomalyType = new ParameterInstance();
        anomalyType.setName("anomalyType");
        switch (anomaly) {
            case 3:
                anomalyType.setValue("Outliers");
                break;
            case 4:
                anomalyType.setValue("Missing values");
                break;
            case 5:
                anomalyType.setValue("Wrong value type");
                break;
            default:
                anomalyType.setValue("No combination");
                break;
        }
        return anomalyType;
    }


    /**
     * Returns a parameter instance for the type of simulator (temperature/humidity sensor simulator)
     *
     * @param sensorName name of the sensor simulator
     * @return parameter instance of the sensor type
     */
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
     * Saves the generated values during the test in the repository.
     *
     * @param testDetails executed test
     * @param valueList   generated value list
     */
    private void saveValues(TestDetails testDetails, String reportId, Map<String, LinkedHashMap<Long, Document>> valueList) {
        Map<String, LinkedHashMap<Long, Document>> valueListTest = new HashMap<>();
        Optional<TestReport> testReportOptional = testReportRepository.findById(reportId);

        if (testReportOptional.isPresent()) {
            TestReport testReport = testReportOptional.get();

            for (Sensor sensor : testReport.getSensor()) {
                if (valueList.get(sensor.getId()) != null) {
                    LinkedHashMap<Long, Document> temp = valueList.get(sensor.getId());
                    valueListTest.put(sensor.getName(), temp);
                    testReport.setSimulationList(valueListTest);
                    testDetails.setSimulationList(valueListTest);
                }

            }
            testReportRepository.save(testReport);
            testDetailsRepository.save(testDetails);
        }

    }


    /**
     * Enable selected Rules, Deploy and Start the Actuator and Sensors of the test.
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
        // start sensors of the test
        for (Sensor sensor : testingSensors) {
            String sensorName = sensor.getName();
            for (List<ParameterInstance> configSensor : test.getConfig()) {
                for (ParameterInstance parameterInstance : configSensor) {
                    if (parameterInstance.getName().equals(CONFIG_SENSOR_NAME_KEY) &&
                            parameterInstance.getValue().equals(sensorName)) {
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
            Map.Entry<String, LinkedHashMap<Long, Document>> sensorValues) {
        Map<String, ParameterInstance> parameterValues = new HashMap<>();
        ParameterInstance interval = new ParameterInstance();
        ParameterInstance value = new ParameterInstance();

        StringBuilder intervalString = new StringBuilder("[");
        StringBuilder valueString = new StringBuilder("[");
        String comma = "||";


        for (Iterator<Map.Entry<Long, Document>> iterator =
             sensorValues.getValue().entrySet().iterator();
             iterator.hasNext(); ) {
            Map.Entry<Long, Document> intervalValues = iterator.next();
            if (!iterator.hasNext()) {
                comma = "";
            }
            intervalString.append(intervalValues.getKey().toString()).append(comma);
            valueString.append(intervalValues.getValue().toJson()).append(comma);
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