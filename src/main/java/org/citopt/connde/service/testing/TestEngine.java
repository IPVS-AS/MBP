package org.citopt.connde.service.testing;


import org.citopt.connde.domain.adapter.parameters.ParameterInstance;
import org.citopt.connde.domain.component.Sensor;
import org.citopt.connde.domain.component.SensorValidator;
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
import org.citopt.connde.web.rest.event_handler.SensorEventHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
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
    private TestDetailsRepository testDetailsRepository;

    @Autowired
    private TestEngine testEngine;

    @Autowired
    private RuleTriggerRepository ruleTriggerRepository;


    @Autowired
    private TestRepository testRepo;

    @Autowired
    private RestDeploymentController restDeploymentController;

    @Autowired
    private RuleRepository ruleRepository;

    @Autowired
    private TestExecutor testExecutor;

    @Autowired
    private TestRerunService testRerunService;

    // List of all active Tests/testValues
    Map<String, LinkedHashMap<Long, Double>> testValues = new HashMap<>();


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

        if (!testExecutor.getActiveTests().containsKey(valueLog.getIdref())) {
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

        // save information about the success and rules of the executed test
        testDetails.setTriggerValues(triggerValues);
        testDetails.setSuccessful(sucessResponse);
        testDetails.setRulesExecuted(rulesExecuted);
        testDetailsRepository.save(testDetails);
    }

    /**
     * Returns a list of all values that triggered the selected rules in the test, between start and end time.
     *
     * @param testId ID of the executed test
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
            if (testRepo.findAllByTriggerId(triggerID.get(i)) != null) {
                List<Testing> test = testRepo.findAllByTriggerId(triggerID.get(i));
                for (Testing testing : test) {
                    //TODO beachte RERUN RULES
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

        }
        return testValues;
    }


    /**
     * Returns all information about the rules of the tested application before the execution
     *
     * @param test to be executed test
     * @return list of information about the rules of the tested application before execution
     */
    public List<Rule> getCorrespondingRules(TestDetails test) {
        // Get the rules selected by the user with their information about the last execution,.. before the sensor is started
        List<Rule> rulesBefore = new ArrayList<>(test.getRules());

        List<RuleTrigger> allRules = ruleTriggerRepository.findAll();
        // Get information for all rules of the IoT-Application
        for (int i = 0; i < test.getSensor().size(); i++) {
            for (RuleTrigger trigger : allRules) {
                Sensor sensor = test.getSensor().get(i);
                String sensorID = sensor.getId();
                if (trigger.getQuery().contains(sensorID)) {
                    for (Rule nextRule : ruleRepository.findAll()) {
                        if (nextRule.getTrigger().getId().equals(trigger.getId())) {
                            if (!rulesBefore.contains(nextRule)) {
                                rulesBefore.add(nextRule);
                            }
                        }
                    }
                }
            }
        }


        return rulesBefore;
    }


    /**
     * Gets all Rules executed by the test.
     *
     * @param triggerValues map of all trigger values of a specific test
     * @return a list of all executed rules
     */
    public List<String> getRulesExecuted(Map<String, List<Double>> triggerValues) {

        List<String> executedRules = new ArrayList<>();
        triggerValues.forEach((k, v) -> executedRules.add(k));

        return executedRules;
    }


    public HttpEntity<Object> editTestConfig(String testID, String changes) {
        try {
            TestDetails testToUpdate = testDetailsRepository.findById(testID);

            // Clear the configuration and rules field of the specific test
            testToUpdate.getConfig().clear();
            testToUpdate.getRules().clear();

            // convert the string of the request body to a JSONObject in order to continue working with it
            JSONObject updateInfos = new JSONObject(changes);

            editSenorConfig(testToUpdate, updateInfos.get("config"));

            // Update the rules to be observed in the test
            updateRuleInformation(testToUpdate, updateInfos);

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Updates the rules that should be observed within the test and whether they should be triggered or not
     *
     * @param test        to be updated
     * @param updateInfos update/editUseNewData information for the rules and the trigger rules
     * @throws JSONException
     */
    private void updateRuleInformation(TestDetails test, JSONObject updateInfos) throws JSONException {
        Pattern pattern = Pattern.compile("rules/(.*)$");
        JSONArray rules = (JSONArray) updateInfos.get("rules");
        List<Rule> newRules = new ArrayList<>();
        if (rules != null) {
            for (int i = 0; i < rules.length(); i++) {
                Matcher m = pattern.matcher(rules.getString(i));
                if (m.find()) {
                    newRules.add(ruleRepository.findById(m.group(1)));
                }
            }
        }
        test.setRules(newRules);

        // Update the information if the selected rules be triggered during the test or not
        test.setTriggerRules(updateInfos.getBoolean("triggerRules"));

        //save updates
        testDetailsRepository.save(test);
    }


    /**
     * Updates the sensor configurations.
     *
     * @param test   to be updated
     * @param config update/editUseNewData information for the sensor configuration
     * @throws JSONException In case of parsing problems
     */
    void editSenorConfig(TestDetails test, Object config) throws JSONException {
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
        // set the updated configuration to the test
        test.setConfig(newConfig);

        //save updates
        testDetailsRepository.save(test);
    }

    /**
     * Method to download a specific Test Report
     *
     * @param path to the specific Test Report to download
     * @return ResponseEntity
     */
    public ResponseEntity downloadPDF(String path) throws IOException {
        TestDetails test = null;
        ResponseEntity respEntity;
        Pattern pattern = Pattern.compile("(.*?)_");
        Matcher m = pattern.matcher(path);
        if (m.find()) {
            test = testDetailsRepository.findById(m.group(1));
        }


        assert test != null;
        File result = new File(test.getPathPDF() + "/" + path + ".pdf");


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
     * Deletes the last Test report and the corresponding graph if existing.
     *
     * @param testId of the test the report to be deleted belongs to
     * @return if files could be deleted successfully or not
     */
    public ResponseEntity deleteReport(String testId) {
        ResponseEntity response;

        TestDetails testDetails = testDetailsRepository.findById(testId);

        if (testDetails.isPdfExists()) {
            Path pathTestReport = Paths.get(testDetails.getPathPDF());
            Path pathDiagram = Paths.get(pathTestReport.getParent().toString(), testId + ".gif");

            try {
                Files.delete(pathTestReport);
                Files.delete(pathDiagram);
                response = new ResponseEntity<>("Test report successfully deleted", HttpStatus.OK);
            } catch (NoSuchFileException x) {
                response = new ResponseEntity<>("Test report doesn't exist.", HttpStatus.NOT_FOUND);
            } catch (IOException x) {
                response = new ResponseEntity<>(x, HttpStatus.CONFLICT);
            }
        } else {
            response = new ResponseEntity<>("No available Test report for this Test.", HttpStatus.NOT_FOUND);
        }
        return response;
    }


    /**
     * Returns a HashMap with date and path to of all Test Reports regarding to a specific test.
     *
     * @param testId ID of the test from which all reports are to be found
     * @return hashMap with the date and path to every report regarding to the specific test
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

    
