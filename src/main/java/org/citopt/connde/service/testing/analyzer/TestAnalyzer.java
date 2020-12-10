package org.citopt.connde.service.testing.analyzer;

import org.citopt.connde.domain.component.Sensor;
import org.citopt.connde.domain.rules.Rule;
import org.citopt.connde.domain.rules.RuleTrigger;
import org.citopt.connde.domain.testing.TestDetails;
import org.citopt.connde.domain.testing.Testing;
import org.citopt.connde.domain.valueLog.ValueLog;
import org.citopt.connde.repository.RuleRepository;
import org.citopt.connde.repository.RuleTriggerRepository;
import org.citopt.connde.repository.TestDetailsRepository;
import org.citopt.connde.repository.TestRepository;
import org.citopt.connde.service.receiver.ValueLogReceiver;
import org.citopt.connde.service.receiver.ValueLogReceiverObserver;
import org.citopt.connde.service.testing.PropertiesService;
import org.citopt.connde.service.testing.executor.TestExecutor;
import org.citopt.connde.web.rest.RestDeploymentController;
import org.citopt.connde.web.rest.helper.DeploymentWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Component
public class TestAnalyzer implements ValueLogReceiverObserver {


    @Autowired
    private TestDetailsRepository testDetailsRepository;

    @Autowired
    private TestAnalyzer testEngine;

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
    private PropertiesService propertiesService;

    @Autowired
    private DeploymentWrapper deploymentWrapper;

    private final String RERUN_IDENTIFIER;

    /**
     * Registers the TestEngine as an Observer to the ValueLogReceiver which then will be notified about incoming value logs.
     *
     * @param valueLogReceiver The value log receiver instance to use
     */
    @Autowired
    private TestAnalyzer(ValueLogReceiver valueLogReceiver ) throws IOException {
        propertiesService = new PropertiesService();
        RERUN_IDENTIFIER = propertiesService.getPropertiesString("testingTool.RerunIdentifier");
        valueLogReceiver.registerObserver(this);
    }


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
     * Checks if the sensors of the specific test are running and so the test is running.
     *
     * @param test specific test with all details
     * @return boolean, if test is still running
     */
    public boolean areSensorsRunning(TestDetails test) {
        List<Sensor> testingSensors = test.getSensor();
        boolean response = false;
        for (Sensor sensor : testingSensors) {
             ResponseEntity<Boolean> sensorRunning = deploymentWrapper.isComponentRunning(sensor);
            if(sensorRunning.getBody() == true){
                response = sensorRunning.getBody();
            }

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
        TestDetails test = testDetailsRepository.findById(testId);
        while (response) {
            // testRunning
            response = areSensorsRunning(test);
        }
        // set and save end time
        test.setEndTestTimeNow();
        testDetailsRepository.save(test);

        return testEngine.getTestValues();
    }


    /**
     * Checks if only the rules selected by the user were triggered during the test run. In this case the test would be successful, otherwise not.
     *
     * @param triggerValuesMap map of all trigger values of a specific test
     * @param ruleNames        Names of all rules regarding to the test
     * @return String information about the success
     */
    public String successCalc(TestDetails test, Map<String, List<Double>> triggerValuesMap, List<String> ruleNames) {
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
     * Saved information about the success, executed Rules and the trigger-values.
     *
     * @param testId ID of the executed test
     */
    public void testSuccess(String testId) {
        TestDetails test = testDetailsRepository.findById(testId);
        List<String> ruleNames = new ArrayList<>();
        List<Rule> ruleList = test.getRules();

        // add all  names and triggerIDs of the rules of the application  tested
        if (test.isUseNewData()) {
            for (Rule rule : ruleList) {
                ruleNames.add(rule.getName());
            }
        } else {
            for (Rule rule : ruleList) {
                ruleNames.add(RERUN_IDENTIFIER + rule.getName());
            }
        }

        // get trigger values
        Map<String, List<Double>> triggerValues = getTriggerValues(testId);
        // get rules executed
        List<String> rulesExecuted = getRulesExecuted(triggerValues);
        //calculate success
        String successResponse = successCalc(test, triggerValues, ruleNames);

        // save the information
        test.setTriggerValues(triggerValues);
        test.setSuccessful(successResponse);
        test.setRulesExecuted(rulesExecuted);
        testDetailsRepository.save(test);
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

        // get all triggerID's and rule names of the corresponding rules to the test
        List<Rule> corresRules = getCorrespondingRules(testDetails);
        for (Rule rule : corresRules) {
            ruleNames.add(rule.getName());
            triggerID.add(rule.getTrigger().getId());
        }


        // Get all trigger values for  the test rules between start and end time
        for (int i = 0; i < ruleNames.size(); i++) {
            List<Double> values = new ArrayList<>();
            String ruleName = ruleNames.get(i);
            if (testRepo.findAllByTriggerId(triggerID.get(i)) != null) {
                List<Testing> test = testRepo.findAllByTriggerId(triggerID.get(i));
                for (Testing testing : test) {
                    if (testing.getRule().contains(ruleName)) {
                        LinkedHashMap<String, Double> timeTiggerValue = (LinkedHashMap<String, Double>) testing.getOutput().getOutputMap().get("event_0");
                        LinkedHashMap<String, Long> timeTiggerValMp = (LinkedHashMap<String, Long>) testing.getOutput().getOutputMap().get("event_0");
                        long timeTiggerVal = timeTiggerValMp.get("time");
                        if (timeTiggerVal >= startTime && timeTiggerVal <= endTime) {
                            values.add(timeTiggerValue.get("value"));
                        }


                    }
                }
                if (values.size() > 0) {
                    testValues.put(ruleName, values);
                }
            }

        }
        return testValues;
    }


    /**
     * returns all rules that belong to a sensor that is part of the test and thus also belongs to the tested IoT-application.
     *
     * @param test to be executed test
     * @return list of all rules corresponding to the specific test
     */
    public List<Rule> getCorrespondingRules(TestDetails test) {
        // Get the rules selected by the user with their information about the last execution,..
        List<Rule> corresRules = new ArrayList<>(test.getRules());


        // go through all rule triggers and check if the sensor id is included
        List<RuleTrigger> allRules = ruleTriggerRepository.findAll();
        for (int i = 0; i < test.getSensor().size(); i++) {
            for (RuleTrigger trigger : allRules) {
                Sensor sensor = test.getSensor().get(i);
                String sensorID = sensor.getId();
                if (trigger.getQuery().contains(sensorID)) {
                    for (Rule nextRule : ruleRepository.findAll()) {
                        if (nextRule.getTrigger().getId().equals(trigger.getId())) {
                            if (!corresRules.contains(nextRule)) {
                                corresRules.add(nextRule);
                            }
                        }
                    }
                }
            }
        }

        return corresRules;
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

}

    
