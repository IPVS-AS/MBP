package de.ipvs.as.mbp.service.testing.analyzer;

import de.ipvs.as.mbp.domain.component.Sensor;
import de.ipvs.as.mbp.domain.rules.Rule;
import de.ipvs.as.mbp.domain.rules.RuleTrigger;
import de.ipvs.as.mbp.domain.testing.TestDetails;
import de.ipvs.as.mbp.domain.testing.TestReport;
import de.ipvs.as.mbp.domain.testing.Testing;
import de.ipvs.as.mbp.domain.valueLog.ValueLog;
import de.ipvs.as.mbp.repository.*;
import de.ipvs.as.mbp.service.receiver.ValueLogReceiver;
import de.ipvs.as.mbp.service.receiver.ValueLogReceiverObserver;
import de.ipvs.as.mbp.service.testing.PropertiesService;
import de.ipvs.as.mbp.service.testing.executor.TestExecutor;
import de.ipvs.as.mbp.web.rest.helper.DeploymentWrapper;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

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
    private RuleRepository ruleRepository;

    @Autowired
    private TestExecutor testExecutor;

    @Autowired
    private final PropertiesService propertiesService;

    @Autowired
    private DeploymentWrapper deploymentWrapper;

    @Autowired
    private TestReportRepository testReportRepository;

    private final String RERUN_IDENTIFIER;

    /**
     * Registers the TestEngine as an Observer to the ValueLogReceiver which then will be notified about incoming value logs.
     *
     * @param valueLogReceiver The value log receiver instance to use
     */
    @Autowired
    private TestAnalyzer(ValueLogReceiver valueLogReceiver) throws IOException {
        propertiesService = new PropertiesService();
        RERUN_IDENTIFIER = propertiesService.getPropertiesString("testingTool.RerunIdentifier");
        valueLogReceiver.registerObserver(this);
    }


    // List of all active Tests/testValues
    Map<String, LinkedHashMap<Long, Document>> testValues = new HashMap<>();


    /**
     * Returns a list of all incoming values of the sensors of the activated tests.
     *
     * @return list of test values
     */
    public Map<String, LinkedHashMap<Long, Document>> getTestValues() {
        return testValues;
    }

    /**
     * Sets a list of all incoming values of the sensors of the activated tests.
     *
     * @param testValues list of test values
     */
    public void setTestValues(Map<String, LinkedHashMap<Long, Document>> testValues) {
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
            LinkedHashMap<Long, Document> newList = new LinkedHashMap<>();
            newList.put(valueLog.getTime().getEpochSecond(), valueLog.getValue());
            testValues.put(valueLog.getIdref(), newList);
        } else {
            Map<Long, Document> oldList = testValues.get(valueLog.getIdref());
            oldList.put(valueLog.getTime().getEpochSecond(), valueLog.getValue());
        }
    }

    /**
     * Checks if the sensors of the specific test are running and so the test is running.
     *
     * @return boolean, if test is still running
     */
    public boolean areSensorsRunning(List<Sensor> testSensors) {
        boolean response = false;
        for (Sensor sensor : testSensors) {
            boolean sensorRunning = deploymentWrapper.isComponentRunning(sensor);
            if (sensorRunning) {
                response = true;
            }
        }

        return response;
    }

    /**
     * Sets the End time of the test, if every Sensor of a test is finished.
     *
     * @param reportId Id of the the report in which to save the information of the test end time
     * @return value-list of the simulated Sensor
     */
    public Map<String, LinkedHashMap<Long, Document>> isFinished(String reportId, String testId, Boolean useNewData) {
        boolean response = true;
        //Try to find specific test report and test
        Optional<TestReport> testReportOptional = testReportRepository.findById(reportId);
        Optional<TestDetails> testDetailsOptional = testDetailsRepository.findById(testId);

        if (testReportOptional.isPresent() && testDetailsOptional.isPresent()) {
            TestReport testReport = testReportOptional.get();
            TestDetails test = testDetailsOptional.get();

            while (response) {
                // testRunning
                if (useNewData) {
                    response = areSensorsRunning(test.getSensor());
                } else {
                    response = areSensorsRunning(testReport.getSensor());
                }
            }
            // set and save end time
            testReport.setEndTestTimeNow();
            testReportRepository.save(testReport);
        }

        return testEngine.getTestValues();
    }


    /**
     * Checks if only the rules selected by the user were triggered during the test run. In this case the test would be successful, otherwise not.
     *
     * @param triggerValuesMap map of all trigger values of a specific test
     * @param ruleNames        Names of all rules regarding to the test
     * @return String information about the success
     */
    public String successCalc(TestDetails test, Map<String, List<Document>> triggerValuesMap, List<String> ruleNames) {
        String success = "Not Successful";
        boolean triggerRules = test.isTriggerRules();

        if (triggerRules) {
            success = compareTriggeredRules(triggerValuesMap, ruleNames);
        } else {
            if (triggerValuesMap.size() == 0) {
                success = "Successful";
            }
        }

        return success;
    }

    /**
     * Compares the rules to be triggered and the actually triggered rules during the test for the success calculation.
     *
     *
     * @param triggerValuesMap List of all triggered rules and values during the test
     * @param ruleNames names of the rules to be triggered during the test
     * @return if test was successful or not
     */
    private String compareTriggeredRules(Map<String, List<Document>> triggerValuesMap, List<String> ruleNames) {
        String success = "Not Successful";
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
        return success;
    }


    /**
     * Saved information about the success, executed Rules and the trigger-values.
     *
     * @param testId ID of the executed test
     */
    public void testSuccess(String testId, String reportId) {
        //Try to find specific test report and test
        Optional<TestReport> testReportOptional = testReportRepository.findById(reportId);
        Optional<TestDetails> testDetailsOptional = testDetailsRepository.findById(testId);

        if (testReportOptional.isPresent() && testDetailsOptional.isPresent()) {
            TestReport testReport = testReportOptional.get();
            TestDetails test = testDetailsOptional.get();

            // Calculate report information
            List<String> ruleNames = getRuleNames(testReport);
            Map<String, List<Document>> triggerValues = getTriggerValues(reportId);
            List<String> rulesExecuted = getRulesExecuted(triggerValues);
            String successResponse = successCalc(test, triggerValues, ruleNames);

            // Save Report information
            testReport.setTriggerValues(triggerValues);
            testReport.setSuccessful(successResponse);
            testReport.setRulesExecuted(rulesExecuted);
            testReportRepository.save(testReport);
        }

    }

    /**
     * Returns the correct list of rule names for the success calculation.
     *
     * @param testReport of the test execution with all relevant information
     * @return list of rule names needed for the success calculation
     */
    private List<String> getRuleNames(TestReport testReport) {
        List<String> ruleNames = new ArrayList<>();
        List<Rule> ruleList = testReport.getRules();

        // add all  names and triggerIDs of the rules of the application  tested
        if (testReport.getUseNewData()) {
            for (Rule rule : ruleList) {
                ruleNames.add(rule.getName());
            }
        } else {
            for (Rule rule : ruleList) {
                ruleNames.add(rule.getName());
            }
        }
        return ruleNames;
    }


    /**
     * Returns a list of all values that triggered the selected rules in the test, between start and end time.
     *
     * @return List of trigger-values
     */
    public Map<String, List<Document>> getTriggerValues(String reportId) {
        Map<String, List<Document>> testValues = new HashMap<>();

        //Try to find specific test report
        Optional<TestReport> testReportOptional = testReportRepository.findById(reportId);

        if (testReportOptional.isPresent()) {
            TestReport testReport = testReportOptional.get();

            // get start & end times of the test
            Integer startTime = testReport.getStartTimeUnix();
            long endTime = testReport.getEndTimeUnix();


            List<String> ruleNames = new ArrayList<>();
            List<String> triggerID = new ArrayList<>();

            // get all triggerID's and rule names of the corresponding rules to the test
            List<Rule> corresRules = getCorrespondingRules(testReport.getRules(), testReport.getSensor());
            for (Rule rule : corresRules) {
                ruleNames.add(rule.getName());
                triggerID.add(rule.getTrigger().getId());
            }

            testValues = extractTriggerValues(testValues, startTime, endTime, ruleNames, triggerID);
        }


        return testValues;
    }

    /**
     * Extract the correct trigger values saved in the database (Testing) for the rules included in a specific test that occurred between the start and end times.
     *
     * @param testValues list of all
     * @param startTime of the executed test
     * @param endTime of the executed test
     * @param ruleNames which should be observed during the test
     * @param triggerID trigger id's of the rules to be observed
     * @return list of trigger values
     */
    private  Map<String, List<Document>> extractTriggerValues(Map<String, List<Document>> testValues, Integer startTime, long endTime, List<String> ruleNames, List<String> triggerID) {
        // Get all trigger values for  the test rules between start and end time
        for (int i = 0; i < ruleNames.size(); i++) {
            List<Document> values = new ArrayList<>();
            String ruleName = ruleNames.get(i);
            if (testRepo.findAllByTriggerId(triggerID.get(i)) != null) {
                // Only check the list of trigger values of the triggers included in the test
                List<Testing> test = testRepo.findAllByTriggerId(triggerID.get(i));
                for (Testing testing : test) {
                    if (testing.getRule().contains(ruleName)) {
                        long timeTriggerVal = 0;

                        // Get all value documents that belong to the triggered rule as well as a timestamp
                        List<Document> documentsForThisRule = new ArrayList<>();
                        for (Map.Entry<String, ValueLog> e : testing.getValueLogEventNameMap().entrySet()) {
                            documentsForThisRule.add(e.getValue().getValue());
                            if (timeTriggerVal == 0) {
                                timeTriggerVal = e.getValue().getTime().getEpochSecond();
                            }
                        }

                        // check if trigger value occurred during the test
                        if (timeTriggerVal >= startTime && timeTriggerVal <= endTime) {
                            /* TODO: Currently all trigger values are stored in a list. It would be make more sense if...
                             * for each trigger value the event name is mapped to the trigger value
                             */
                            values.addAll(testing.getValueLogEventNameMap().values().stream().map(ValueLog::getValue).collect(Collectors.toList()));
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
     * Returns all rules that belong to a sensor that is part of the test and thus also belongs to the tested IoT-application.
     *
     * @param testRules  to be executed test
     * @param sensorList list of sensors of the test
     * @return list of all rules corresponding to the specific test
     */
    public List<Rule> getCorrespondingRules(List<Rule> testRules, List<Sensor> sensorList) {
        // Get the rules selected by the user with their information about the last execution,..
        List<Rule> corresRules = new ArrayList<>(testRules);


        // go through all rule triggers and check if the sensor id is included
        List<RuleTrigger> allRules = ruleTriggerRepository.findAll();
        for (Sensor value : sensorList) {
            for (RuleTrigger trigger : allRules) {
                String sensorID = value.getId();
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
    public List<String> getRulesExecuted(Map<String, List<Document>> triggerValues) {

        List<String> executedRules = new ArrayList<>();
        triggerValues.forEach((k, v) -> executedRules.add(k));

        return executedRules;
    }


}

