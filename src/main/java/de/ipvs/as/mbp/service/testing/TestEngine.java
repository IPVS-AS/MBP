package de.ipvs.as.mbp.service.testing;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.ipvs.as.mbp.domain.component.Actuator;
import de.ipvs.as.mbp.domain.component.Sensor;
import de.ipvs.as.mbp.domain.operator.parameters.ParameterInstance;
import de.ipvs.as.mbp.domain.rules.Rule;
import de.ipvs.as.mbp.domain.rules.RuleTrigger;
import de.ipvs.as.mbp.domain.testing.TestDetails;
import de.ipvs.as.mbp.domain.testing.Testing;
import de.ipvs.as.mbp.domain.valueLog.ValueLog;
import de.ipvs.as.mbp.service.receiver.ValueLogReceiver;
import de.ipvs.as.mbp.service.receiver.ValueLogReceiverObserver;
import de.ipvs.as.mbp.service.rules.RuleEngine;
import de.ipvs.as.mbp.web.rest.RestDeploymentController;
import de.ipvs.as.mbp.repository.ActuatorRepository;
import de.ipvs.as.mbp.repository.RuleRepository;
import de.ipvs.as.mbp.repository.RuleTriggerRepository;
import de.ipvs.as.mbp.repository.TestDetailsRepository;
import de.ipvs.as.mbp.repository.TestRepository;
import de.ipvs.as.mbp.web.rest.helper.DeploymentWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;


@Component
public class TestEngine implements ValueLogReceiverObserver {
	
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
    private RuleEngine ruleEngine;
    
    @Autowired
    private DeploymentWrapper deploymentWrapper;

    // List of all active Tests/testValues
    Map<String, TestDetails> activeTests = new HashMap<>();
    Map<String, List<Double>> testValues = new HashMap<>();


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
    public Map<String, List<Double>> getTestValues() {
        return testValues;
    }

    /**
     * Sets a list of all incomming values of the sensors of the activeted tests.
     *
     * @param testValues list of test values
     */
    public void setTestValues(Map<String, List<Double>> testValues) {
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
            List<Double> newList = new ArrayList<>();
            newList.add(valueLog.getValue());
            testValues.put(valueLog.getIdref(), newList);
        } else {
            List<Double> oldList = testValues.get(valueLog.getIdref());
            oldList.add(valueLog.getValue());
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
			// TODO: I don't think this implementation is correct: test running = LAST
			// sensor running
			// Not sure what the criteria are for running tests - i guess all sensors have
			// to run -> use a logical AND (&&) to create the response:
//        	response = response && deploymentWrapper.isComponentRunning(sensor);
			response = deploymentWrapper.isComponentRunning(sensor);
		}

        return response;
    }

    /**
     * Sets the End time of the test, if every Sensor of a test is finished.
     *
     * @param testId Id of the the running test
     * @return value-list of the simulated Sensor
     */
    public Map<String, List<Double>> isFinished(String testId) {
        boolean response = true;
        TestDetails testDetails = testDetailsRepository.findById(testId).get();
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
        Actuator testingActuator = actuatorRepository.findByName("TestingActuator").get();

        List<Sensor> testingSensor = testDetails.getSensor();
        List<Rule> rules = testDetails.getRules();
        List<List<ParameterInstance>> config = testDetails.getConfig();

        //activate the selected rules for the test
        for (Rule rule : rules) {
            // check if selected rules are active --> if not active Rules
            ruleEngine.enableRule(rule);
        }

        //check if test exists
        if (!testDetailsRepository.existsById(testDetails.getId())) {
            return new ResponseEntity<>("Test does not exists.", HttpStatus.NOT_FOUND);
        }

        //check if actuator is deployed
        boolean actuatorDeployed = deploymentWrapper.isComponentRunning(testingActuator);
        if (!actuatorDeployed) {
            //if false deploy actuator
            deploymentWrapper.deployComponent(testingActuator);
        }
        // start the Actuator
        deploymentWrapper.startComponent(testingActuator, new ArrayList<>());



		// check if the sensor/s are currently running
		for (Sensor sensor : testingSensor) {
			boolean sensorDeployed = deploymentWrapper.isComponentRunning(sensor);
			String sensorName = sensor.getName();
			for (List<ParameterInstance> configSensor : config) {
				for (ParameterInstance parameterInstance : configSensor) {
					if (parameterInstance.getName().equals("ConfigName")
							&& parameterInstance.getValue().equals(sensorName)) {
						// check if sensor is deployed
						if (!sensorDeployed) {
							// if not deploy Sensor
							deploymentWrapper.deployComponent((sensor));
						}
						deploymentWrapper.stopComponent(sensor);
						deploymentWrapper.startComponent(sensor, configSensor);
					}
				}
			}
		}

        return new ResponseEntity<>("Test successfully started.", HttpStatus.OK);
    }


    /**
     * Saved informations about the success, executed Rules and the trigger-values
     *
     * @param testId ID of the executed test
     */
    public void testSuccess(String testId) {
        TestDetails testDetails = testDetailsRepository.findById(testId).get();
        List<String> ruleNames = new ArrayList<>();

        List<Rule> ruleList = testDetails.getRules();

        // add all  names and triggerIDs of the rules of the application  tested
        for (Rule rule : ruleList) {
            ruleNames.add(rule.getName());
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


        TestDetails testDetails = testDetailsRepository.findById(testId).get();
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
     * Returns all informations about the rules of the tested application before the execution
     *
     * @param test to be executed test
     * @return list of informations about the rules of the tested application before execution
     */
    public List<Rule> getStatRulesBefore(TestDetails test) {
        // Get the rules selected by the user with their informations about the last execution,.. before the sensor is started
        List<Rule> rulesbefore = new ArrayList<>(test.getRules());

        List<RuleTrigger> allRules = ruleTriggerRepository.findAll();
        // Get Informations for all rules of the IoT-Applikation
        for (int i = 0; i < test.getSensor().size(); i++) {
            for (RuleTrigger trigger : allRules) {
                Sensor sensor = test.getSensor().get(i);
                String s = sensor.getId();
                if (trigger.getQuery().contains(s)) {
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

        return rulesbefore;
    }

    /**
     * Starts the test and saves all values form the sensor.
     *
     * @param test test to be executed
     */
    public Map<String, List<Double>> executeTest(TestDetails test) {

        Map<String, TestDetails> activeTests = testEngine.getActiveTests();
        Map<String, List<Double>> list = testEngine.getTestValues();
        for (Sensor sensor : test.getSensor()) {
            activeTests.put(sensor.getId(), test);
            list.remove(sensor.getId());
        }
        testEngine.setActiveTests(activeTests);
        testEngine.setTestValues(list);
        testEngine.startTest(testDetailsRepository.findById(test.getId()).get());

        Map<String, List<Double>> valueList;
        Map<String, List<Double>> valueListTest = new HashMap<>();

        // Get List of all simulated Values
        valueList = testEngine.isFinished(test.getId());
        TestDetails testDetails2 = testDetailsRepository.findById(test.getId()).get();
        for (Sensor sensor : test.getSensor()) {
            List<Double> temp = valueList.get(sensor.getId());
            valueList.put(sensor.getName(), temp);
            valueListTest.put(sensor.getName(), temp);
            list.remove(sensor.getId());
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
            test = testDetailsRepository.findById(m.group(1)).get();
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
        TestDetails testDetails = testDetailsRepository.findById(testId).get();
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

        files.forEach(System.out::println);
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


    /**
     * Sorts the timestamps of the List of Test-Reports.
     *
     * @param unsortedMap Sorted map with the timestamp as Long in the key
     * @return sorted Map depending on the key
     */
    private Map<Long, String> sortMap(Map<Long, String> unsortedMap) {

        Map<Long, String> treeMap = new TreeMap<>((o1, o2) -> o1.compareTo(o2));

        treeMap.putAll(unsortedMap);

        return treeMap;
    }
}
