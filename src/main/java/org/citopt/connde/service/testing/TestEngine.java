package org.citopt.connde.service.testing;


import org.citopt.connde.domain.adapter.Adapter;
import org.citopt.connde.domain.adapter.parameters.ParameterInstance;
import org.citopt.connde.domain.component.Actuator;
import org.citopt.connde.domain.component.Sensor;
import org.citopt.connde.domain.device.Device;
import org.citopt.connde.domain.rules.Rule;
import org.citopt.connde.domain.rules.RuleTrigger;
import org.citopt.connde.domain.testing.TestDetails;
import org.citopt.connde.domain.testing.Testing;
import org.citopt.connde.domain.valueLog.ValueLog;
import org.citopt.connde.repository.*;
import org.citopt.connde.service.receiver.ValueLogReceiver;
import org.citopt.connde.service.receiver.ValueLogReceiverObserver;
import org.citopt.connde.web.rest.RestDeploymentController;
import org.citopt.connde.web.rest.RestRuleController;
import org.json.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;


import java.util.Map;

@Component
public class TestEngine implements ValueLogReceiverObserver {
    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private SensorRepository sensorRepository;

    @Autowired
    private AdapterRepository adapterRepository;

    @Autowired
    private TestDetailsRepository testDetailsRepository;

    @Autowired
    private TestEngine testEngine;

    @Autowired
    private RuleTriggerRepository ruleTriggerRepository;

    @Autowired
    private ActuatorRepository actuatorRepository;

    @Autowired
    private TestRepository testRepo;

    @Autowired
    private RestDeploymentController restDeploymentController;

    @Autowired
    private RuleRepository ruleRepository;

    @Autowired
    private RestRuleController restRuleController;


    // List of all active Tests/testValues
    Map<String, TestDetails> activeTests = new HashMap<>();
    Map<String, LinkedHashMap<Long, Double>> testValues = new HashMap<>();

    String[] sensorSim = {"TestingTemperaturSensor", "TestingTemperaturSensorPl", "TestingFeuchtigkeitsSensor", "TestingFeuchtigkeitsSensorPl", "TestingGPSSensorPl", "TestingGPSSensor", "TestingBeschleunigungsSensor", "TestingBeschleunigungsSensorPl"};
    List<String> sensorSimulators = Arrays.asList(sensorSim);


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
     * Returns a list of all incomming values of the sensors of the activeted tests.
     *
     * @return list of test values
     */
    public Map<String, LinkedHashMap<Long, Double>> getTestValues() {
        return testValues;
    }

    /**
     * Sets a list of all incomming values of the sensors of the activeted tests.
     *
     * @param testValues list of test values
     */
    public void setTestValues(Map<String, LinkedHashMap<Long, Double>> testValues) {
        this.testValues = testValues;
    }

    /**
     * Registers the TestEngine as an Observer to the ValueLogReceiver which then will be notified about incoming value logs.
     *
     * @param valueLogReceiver The value log receiver instance to use
     */
    @Autowired
    private TestEngine(ValueLogReceiver valueLogReceiver) {
        valueLogReceiver.registerObserver(this);
    }

    /**
     * Stores all Values from the active Tests
     *
     * @param valueLog The corresponding value log that arrived
     */
    @Override
    public void onValueReceived(ValueLog valueLog) {
        if (!activeTests.containsKey(valueLog.getIdref())) {
            return;
        }
        if (!testValues.containsKey(valueLog.getIdref())) {
            LinkedHashMap<Long, Double> newList = new LinkedHashMap<>();
            newList.put(valueLog.getTime().getEpochSecond(), valueLog.getValue());
            testValues.put(valueLog.getIdref(), newList);
        } else {
            Map<Long, Double> oldList = testValues.get(valueLog.getIdref());
            oldList.put(valueLog.getTime().getEpochSecond(), valueLog.getValue());
        }
    }

    /**
     * Checks if the sensors of the specific test are running
     *
     * @param testDetails specific test with all details
     * @return boolean, if test is still running
     */
    public boolean testRunning(TestDetails testDetails) {
        List<Sensor> testingSensors = testDetails.getSensor();
        boolean response = false;
        for (Sensor sensor : testingSensors) {
            ResponseEntity<Boolean> sensorRunning = restDeploymentController.isRunningSensor(sensor.getId());
            response = sensorRunning.getBody();
        }

        return response;
    }

    /**
     * Sets the End time of the test, if every Sensor of a test is finished.
     *
     * @param testId Id of the the running test
     * @return value-list of the simulated Sensor
     */
    public Map<String, LinkedHashMap<Long, Double>> isFinished(String testId) {
        boolean response = true;
        TestDetails testDetails = testDetailsRepository.findById(testId);
        while (response) {
            response = testEngine.testRunning(testDetails);
        }
        testDetails.setEndTestTimeNow();
        testDetailsRepository.save(testDetails);

        return testEngine.getTestValues();
    }


    /**
     * Checks if the test was successful or not.
     *
     * @param triggerValuesMap map of all trigger values of a specific test
     * @param ruleNames        Names of all rules regarding to the test
     * @return information about the success
     */
    public String checkSuccess(TestDetails test, Map<String, List<Double>> triggerValuesMap, List<String> ruleNames) {
        String success = "Not Successful";
        boolean triggerRules = test.isTriggerRules();

        if (triggerRules) {
            if (triggerValuesMap.size() == ruleNames.size()) {
                for (String ruleName : ruleNames) {
                    if (triggerValuesMap.containsKey(ruleName)) {
                        success = "Successful";
                    } else {
                        success = "Not Successful";
                        break;
                    }
                }

            }
        } else {
            if (triggerValuesMap.size() == 0) {
                success = "Successful";
            }
        }

        return success;
    }

    /**
     * Enable selected Rules, Deploy and Start the Actuator and Sensors of the test
     *
     * @param testDetails specific test to be executed
     */
    public ResponseEntity<String> startTest(TestDetails testDetails) {
        Actuator testingActuator = actuatorRepository.findByName("TestingActuator");

        List<Sensor> testingSensor = testDetails.getSensor();
        List<Rule> rules = testDetails.getRules();
        List<List<ParameterInstance>> config = testDetails.getConfig();

        //activate the selected rules for the test
        for (Rule rule : rules) {
            // check if selected rules are active --> if not active Rules
            restRuleController.enableRule(rule.getId());
        }

        //check if test exists
        if (!testDetailsRepository.exists(testDetails.getId())) {

            return new ResponseEntity<>("Test does not exists.", HttpStatus.NOT_FOUND);
        }

        //check if actuator is deployed
        ResponseEntity<Boolean> actuatorDeployed = restDeploymentController.isRunningActuator(testingActuator.getId());
        if (!actuatorDeployed.getBody()) {
            //if false deploy actuator
            restDeploymentController.deployActuator(testingActuator.getId());
        }
        // start the Actuator
        restDeploymentController.startActuator(testingActuator.getId(), new ArrayList<>());


        if (!testDetails.isUseNewData()) {


            for (Sensor sensor : testingSensor) {

                List<ParameterInstance> parametersWrapper = new ArrayList<>();

                // If not a sensor simulator
                if (!sensorSimulators.contains(sensor) && !sensor.getName().contains("RERUN_")) {

                    restDeploymentController.stopSensor(sensor.getId());

                    for (Map.Entry<String, LinkedHashMap<Long, Double>> entry : testDetails.getSimulationList().entrySet()) {
                        if (entry.getKey().equals(sensor.getName())) {
                            String intervalString = "[";
                            String valueString = "[";
                            String comma = "-";


                            for (Iterator<Map.Entry<Long, Double>> iterator = entry.getValue().entrySet().iterator(); iterator.hasNext(); ) {

                                Map.Entry<Long, Double> interval = iterator.next();
                                if (!iterator.hasNext()) {
                                    comma = "";
                                }
                                intervalString = intervalString + interval.getKey().toString() + comma;
                                valueString = valueString + interval.getValue().toString() + comma;


                            }
                            intervalString = intervalString + "]";
                            valueString = valueString + "]";
                            ParameterInstance interval = new ParameterInstance();
                            interval.setName("interval");
                            interval.setValue(intervalString);
                            ParameterInstance value = new ParameterInstance();
                            value.setName("value");
                            value.setValue(valueString);
                            parametersWrapper.add(interval);
                            parametersWrapper.add(value);


                        }
                    }


                    Sensor rerunSensor = sensorRepository.findByName("RERUN_" + sensor.getName());
                    ResponseEntity<Boolean> sensorDeployed = restDeploymentController.isRunningSensor(rerunSensor.getId());

                    if (!sensorDeployed.getBody()) {
                        //if not deploy Sensor
                        restDeploymentController.deploySensor((rerunSensor.getId()));
                    }
                    restDeploymentController.stopSensor(rerunSensor.getId());
                    restDeploymentController.startSensor(rerunSensor.getId(), parametersWrapper);
                }
            }


        } else {
            // check if the sensor/s are currently running
            for (Sensor sensor : testingSensor) {
                ResponseEntity<Boolean> sensorDeployed = restDeploymentController.isRunningSensor(sensor.getId());
                String sensorName = sensor.getName();
                for (List<ParameterInstance> configSensor : config) {
                    for (ParameterInstance parameterInstance : configSensor) {
                        if (parameterInstance.getName().equals("ConfigName") && parameterInstance.getValue().equals(sensorName)) {
                            // check if sensor is deployed
                            if (!sensorDeployed.getBody()) {
                                //if not deploy Sensor
                                restDeploymentController.deploySensor((sensor.getId()));
                            }
                            restDeploymentController.stopSensor(sensor.getId());
                            restDeploymentController.startSensor(sensor.getId(), configSensor);
                        }
                    }
                }
            }
        }


        return new ResponseEntity<>("Test successfully started.", HttpStatus.OK);
    }


    /**
     * Saved information about the success, executed Rules and the trigger-values
     *
     * @param testId ID of the executed test
     */
    public void testSuccess(String testId) {
        TestDetails testDetails = testDetailsRepository.findById(testId);
        List<String> ruleNames = new ArrayList<>();

        List<Rule> ruleList = testDetails.getRules();

        // add all  names and triggerIDs of the rules of the application  tested
        if (testDetails.isUseNewData()) {
            for (Rule rule : ruleList) {
                ruleNames.add(rule.getName());
            }
        } else {
            for (Rule rule : ruleList) {
                ruleNames.add("RERUN_" + rule.getName());
            }
        }


        Map<String, List<Double>> triggerValues = getTriggerValues(testId);
        String sucessResponse = testEngine.checkSuccess(testDetails, triggerValues, ruleNames);
        List<String> rulesExecuted = testEngine.getRulesExecuted(triggerValues);

        // save informations about the success and rules of the executed test
        testDetails.setTriggerValues(triggerValues);
        testDetails.setSuccessful(sucessResponse);
        testDetails.setRulesExecuted(rulesExecuted);
        testDetailsRepository.save(testDetails);
    }

    /**
     * Returns a list of all values that triggered the selected rules in the test, between start and end time.
     *
     * @param testId ID of the executed test
     * @return List of trigger-values
     */
    public Map<String, List<Double>> getTriggerValues(String testId) {
        Map<String, List<Double>> testValues = new HashMap<>();


        TestDetails testDetails = testDetailsRepository.findById(testId);
        List<String> ruleNames = new ArrayList<>();
        List<String> triggerID = new ArrayList<>();

        Integer startTime = testDetails.getStartTimeUnix();
        long endTime = testDetails.getEndTimeUnix();


        for (int i = 0; i < testDetails.getSensor().size(); i++) {
            for (RuleTrigger trigger : ruleTriggerRepository.findAll()) {
                Sensor sensor = testDetails.getSensor().get(i);
                String s = sensor.getId();
                if (trigger.getQuery().contains(s)) {
                    triggerID.add(trigger.getId());
                    for (Rule nextRule : ruleRepository.findAll()) {
                        if (nextRule.getTrigger().getId().equals(trigger.getId())) {
                            ruleNames.add(nextRule.getName());
                        }
                    }
                }
            }
        }

        for (int i = 0; i < ruleNames.size(); i++) {
            List<Double> values = new ArrayList<>();
            String rulename = ruleNames.get(i);
            List<Testing> test = testRepo.findAllByTriggerId(triggerID.get(i));
            for (Testing testing : test) {
                if (testing.getRule().contains(rulename)) {
                    LinkedHashMap<String, Double> timeTiggerValue = (LinkedHashMap<String, Double>) testing.getOutput().getOutputMap().get("event_0");
                    LinkedHashMap<String, Long> timeTiggerValMp = (LinkedHashMap<String, Long>) testing.getOutput().getOutputMap().get("event_0");
                    long timeTiggerVal = timeTiggerValMp.get("time");
                    if (timeTiggerVal >= startTime && timeTiggerVal <= endTime) {
                        values.add(timeTiggerValue.get("value"));
                    }


                }
            }
            if (values.size() > 0) {
                testValues.put(rulename, values);
            }
        }
        return testValues;
    }


    /**
     * Returns all information about the rules of the tested application before the execution
     *
     * @param test to be executed test
     * @return list of informations about the rules of the tested application before execution
     */
    public List<Rule> getStatRulesBefore(TestDetails test) {
        // Get the rules selected by the user with their information about the last execution,.. before the sensor is started
        List<Rule> rulesbefore = new ArrayList<>(test.getRules());

        List<RuleTrigger> allRules = ruleTriggerRepository.findAll();
        // Get information for all rules of the IoT-Application
        for (int i = 0; i < test.getSensor().size(); i++) {
            for (RuleTrigger trigger : allRules) {
                Sensor sensor = test.getSensor().get(i);
                String sensorID = sensor.getId();
                if (trigger.getQuery().contains(sensorID)) {
                    for (Rule nextRule : ruleRepository.findAll()) {
                        if (nextRule.getTrigger().getId().equals(trigger.getId())) {
                            if (!rulesbefore.contains(nextRule)) {
                                rulesbefore.add(nextRule);
                            }
                        }
                    }
                }
            }
        }


        //TODO: WARUM INVOCE TARGET INCEPTION
        if (!test.isUseNewData()) {
            for (int i = 0; i < rulesbefore.size(); i++) {
                Rule rule = rulesbefore.get(i);
                String rulename = "RERUN_" + rule.getName();
                Rule rule2 = ruleRepository.findByName(rulename);
                if (rule2 != null) {
                    rulesbefore.add(rule2);
                }

            }

        }


        return rulesbefore;
    }

    /**
     * Starts the test and saves all values form the sensor.
     *
     * @param test test to be executed
     */
    public Map<String, LinkedHashMap<Long, Double>> executeTest(TestDetails test) {

        Map<String, TestDetails> activeTests = testEngine.getActiveTests();
        Map<String, LinkedHashMap<Long, Double>> list = testEngine.getTestValues();


        if (test.isUseNewData()) {
            for (Sensor sensor : test.getSensor()) {
                if (!sensor.getName().contains("RERUN_")) {
                    activeTests.put(sensor.getId(), test);
                    list.remove(sensor.getId());
                }

            }
        } else {
            for (Sensor sensor : test.getSensor()) {
                if (sensor.getName().contains("RERUN_")) {
                    activeTests.put(sensor.getId(), test);
                    list.remove(sensor.getId());
                }
            }
        }


        testEngine.setActiveTests(activeTests);
        testEngine.setTestValues(list);
        testEngine.startTest(testDetailsRepository.findById(test.getId()));

        Map<String, LinkedHashMap<Long, Double>> valueList;
        Map<String, LinkedHashMap<Long, Double>> valueListTest = new HashMap<>();

        // Get List of all simulated Values
        valueList = testEngine.isFinished(test.getId());
        TestDetails testDetails2 = testDetailsRepository.findOne(test.getId());
        for (Sensor sensor : test.getSensor()) {
            if (testDetails2.isUseNewData()) {
                LinkedHashMap<Long, Double> temp = valueList.get(sensor.getId());
                valueList.put(sensor.getName(), temp);
                valueListTest.put(sensor.getName(), temp);
                list.remove(sensor.getId());
            } else {
                if (!sensor.getName().contains("RERUN_")) {
                    Sensor rerunSensor = sensorRepository.findByName("RERUN_" + sensor.getName());
                    LinkedHashMap<Long, Double> temp = valueList.get(rerunSensor.getId());
                    valueList.put(sensor.getName(), temp);
                    valueListTest.put(sensor.getName(), temp);
                    list.remove(rerunSensor.getId());
                }
            }


        }

        // save list of sensor values to database
        testDetails2.setSimulationList(valueListTest);
        testDetailsRepository.save(testDetails2);

        return valueListTest;
    }


    /**
     * Gets all Rules executed by the test
     *
     * @param triggerValues map of all trigger values of a specific test
     * @return a list of all executed rules
     */
    public List<String> getRulesExecuted(Map<String, List<Double>> triggerValues) {

        List<String> executedRules = new ArrayList<>();
        triggerValues.forEach((k, v) -> executedRules.add(k));

        return executedRules;
    }

    /**
     * Method to download a specific Test Report
     *
     * @param path to the specific Test Report to download
     * @return
     */
    public ResponseEntity downloadPDF(String path) throws IOException {
        TestDetails test = null;
        Pattern pattern = Pattern.compile("(.*?)_");
        Matcher m = pattern.matcher(path);
        if (m.find()) {
            test = testDetailsRepository.findById(m.group(1));
        }


        assert test != null;
        File result = new File(test.getPathPDF() + "/" + path + ".pdf");

        ResponseEntity respEntity;

        if (result.exists()) {
            InputStream inputStream = new FileInputStream(result);

            byte[] out = org.apache.commons.io.IOUtils.toByteArray(inputStream);

            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add("content-disposition", "attachment; filename=" + path + ".pdf");

            respEntity = new ResponseEntity(out, responseHeaders, HttpStatus.OK);
            inputStream.close();
        } else {
            respEntity = new ResponseEntity("File Not Found", HttpStatus.NOT_FOUND);
        }


        return respEntity;
    }

    /**
     * Returns a Hashmap with date and path to of all Test Reports regarding to a specific test.
     *
     * @param testId ID of the test from which all reports are to be found
     * @return hashmap with the date and path to every report regarding to the specific test
     */
    public ResponseEntity getPDFList(String testId) {
        ResponseEntity pdfList;
        Map<Long, String> nullList = new TreeMap<>();
        TestDetails testDetails = testDetailsRepository.findOne(testId);
        try {
            if (testDetails.isPdfExists()) {
                Stream<Path> pathStream = Files.find(Paths.get(testDetails.getPathPDF()), 10, (path, basicFileAttributes) -> {
                    File file = path.toFile();
                    return !file.isDirectory() &&
                            file.getName().contains(testId + "_");
                });

                pdfList = new ResponseEntity(generateReportList(pathStream), HttpStatus.OK);
            } else {
                pdfList = new ResponseEntity(nullList, HttpStatus.OK);
            }


        } catch (IOException e) {
            pdfList = new ResponseEntity(HttpStatus.NOT_FOUND);
        }

        return pdfList;
    }


    /**
     * Generates a Hashmap where the entries consist of the creation date of the report and the path to it.
     *
     * @param pathStream Stream of the matching reports regarding to to the specific test
     * @return Map out of the creation dates and paths to the report
     */
    public Map<Long, String> generateReportList(Stream<Path> pathStream) {
        Map<Long, String> pdfEntry = new TreeMap<>();

        // Pattern to find the PDF-Files for a specific test with the specific ID in the Filename
        Pattern pattern = Pattern.compile("_(.*?).pdf");


        Long dateMilliseconds = null;

        // Put every path out of the stream into a list
        List<Path> files = pathStream.sorted(Comparator.comparing(Path::toString)).collect(Collectors.toList());

        for (Path singlePath : files) {
            // get  date in milliseconds out of the filename and convert this into the specified date format
            Matcher machter = pattern.matcher(singlePath.toString());
            if (machter.find()) {
                dateMilliseconds = Long.valueOf(machter.group(1));
            }
            //Add properties to object
            pdfEntry.put(dateMilliseconds, singlePath.getFileName().toString());

        }


        return sortMap(pdfEntry);
    }

    public void addSensor(String newSensorName, TestDetails testDetails) {
        List<Sensor> sensors = testDetails.getSensor();
        sensors.add(sensorRepository.findByName(newSensorName));
        testDetails.setSensor(sensors);
        testDetailsRepository.save(testDetails);
    }


    /**
     * Adds the Sensors for the Test Rerun with real Sensors.
     *
     * @param realSensorName
     */
    public void addRerunSensor(String realSensorName, TestDetails testDetails) {

        Sensor newSensor = new Sensor();
        // New sensor is nor owned by anyone
        newSensor.setOwner(null);
        Adapter rerunOperator = adapterRepository.findByName("RERUN_OPERATOR");
        Device testingDevice = deviceRepository.findByName("TestingDevice");
        String newSensorName = "RERUN_" + realSensorName;

        try {
            if (sensorRepository.findByName(newSensorName) == null) {
                if (rerunOperator != null && testingDevice != null) {
                    // Set all relevant information
                    newSensor.setName(newSensorName);
                    newSensor.setComponentType("Computer");
                    newSensor.setDevice(testingDevice);
                    newSensor.setAdapter(rerunOperator);

                    //Insert new sensor into repository
                    sensorRepository.insert(newSensor);

                }
            }
            if (!testDetails.getSensor().contains(sensorRepository.findByName(newSensorName))) {
                addSensor(newSensorName, testDetails);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void addRerunRule(TestDetails testDetails) {
        // Get a list of every rule belonging to the IoT-Application
        List<Rule> applicationRules = getStatRulesBefore(testDetails);

        for (Rule rule : applicationRules) {
            if (ruleRepository.findByName("RERUN_" + rule.getName()) == null) {
                Rule rerunRule = new Rule();
                rerunRule.setName("RERUN_" + rule.getName());
                rerunRule.setOwner(null);
                rerunRule.setActions(rule.getActions());

                if (ruleTriggerRepository.findByName("RERUN_" + rule.getTrigger().getName()) == null) {
                    RuleTrigger newTrigger = new RuleTrigger();
                    newTrigger.setDescription(rule.getTrigger().getDescription());
                    newTrigger.setName("RERUN_" + rule.getTrigger().getName());


                    String triggerQuery = rule.getTrigger().getQuery();
                    // Regex to get out the sensor ID
                    Pattern pattern = Pattern.compile("(?<=sensor_)(.*)(?=\\)\\])");
                    Matcher matcher = pattern.matcher(triggerQuery);
                    while (matcher.find()) {
                        String sensorID = matcher.group();
                        List<Sensor> realSensors = sensorRepository.findAll();
                        for (Sensor realSensor : realSensors) {
                            if (realSensor.getId().equals(sensorID)) {
                                Sensor rerunSensor = sensorRepository.findByName("RERUN_" + realSensor.getName());
                                triggerQuery = triggerQuery.replace(realSensor.getId(), rerunSensor.getId());
                                newTrigger.setQuery(triggerQuery);
                                ruleTriggerRepository.insert(newTrigger);

                            }
                        }
                        rerunRule.setTrigger(newTrigger);
                    }
                } else {
                    rerunRule.setTrigger(ruleTriggerRepository.findByName("RERUN_" + rule.getTrigger().getName()));
                }

                ruleRepository.insert(rerunRule);
            }
        }
    }

    public void deleteRerunRules(TestDetails testDetails) {
    }

    /**
     * Sorts the timestamps of the List of Test-Reports.
     *
     * @param unsortedMap Sorted map with the timestamp as Long in the key
     * @return sorted Map depending on the key
     */
    private Map<Long, String> sortMap(Map<Long, String> unsortedMap) {

        Map<Long, String> treeMap = new TreeMap<>(Long::compareTo);

        treeMap.putAll(unsortedMap);

        return treeMap;
    }


}

    
