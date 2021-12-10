package de.ipvs.as.mbp.service.testing;


import de.ipvs.as.mbp.domain.operator.parameters.ParameterInstance;
import de.ipvs.as.mbp.domain.rules.Rule;
import de.ipvs.as.mbp.domain.testing.TestReport;
import de.ipvs.as.mbp.repository.*;
import de.ipvs.as.mbp.domain.testing.TestDetails;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;


import java.util.*;

@Component
public class TestEngine {

    @Autowired
    private TestDetailsRepository testDetailsRepository;

    @Autowired
    private RuleRepository ruleRepository;


    @Autowired
    private TestReportRepository testReportRepository;



    /**
     * Returns a HashMap with date and path to of all Test Reports regarding to a specific test.
     *
     * @param testId ID of the test from which all reports are to be found
     * @return hashMap with the date and path to every report regarding to the specific test
     */
    public ResponseEntity<Map<Long, TestReport>> getPDFList(String testId) {
        ResponseEntity<Map<Long, TestReport>> pdfList;
        Map<Long, TestReport> nullList = new TreeMap<>();
        Optional<TestDetails> testDetailsOptional = testDetailsRepository.findById(testId);

        try {
            if (testDetailsOptional.isPresent()) {
                for (TestReport testReport : testReportRepository.findAllByName(testDetailsOptional.get().getName())) {
                    nullList.put(Long.valueOf(testReport.getStartTimeUnix()), testReport);
                }
            }

            pdfList = new ResponseEntity<>(nullList, HttpStatus.OK);
        } catch (Exception e) {
            pdfList = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return pdfList;
    }

    /**
     * Returns a list of the generated sensor values of a specific test execution with a specific format to display them in the Highchart.
     *
     * @param reportId of the report for which the sensor values should be returned
     * @return list of sensor values for all sensors within the test report
     */
    public Map<String, ArrayList> getSimulationValues(String reportId) {
        Map<String, ArrayList> simulationValues = new HashMap<>();

        if (testReportRepository.findById(reportId).isPresent()) {
            TestReport testReport = testReportRepository.findById(reportId).get();

            Map<String, LinkedHashMap<Long, Document>> simulationList = testReport.getSimulationList();

            if (simulationList != null && simulationList.size() > 0) {
                for (Map.Entry<String, LinkedHashMap<Long, Document>> entry : simulationList.entrySet()) {
                    ArrayList tupelList = new ArrayList();


                    String key = entry.getKey();
                    LinkedHashMap<Long, Document> valueList = entry.getValue();
                    for (Map.Entry<Long, Document> list : valueList.entrySet()) {
                        List timeValueTupel = new ArrayList<>();
                        timeValueTupel.add(list.getKey() * 1000);
                        timeValueTupel.add(list.getValue());
                        tupelList.add(timeValueTupel);
                    }

                    simulationValues.put(key, tupelList);

                }
            }


        }
        return simulationValues;
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
            Optional<TestDetails> testDetailsOptional = testDetailsRepository.findById(testID);
            if (testDetailsOptional.isPresent()) {
                TestDetails testToUpdate = testDetailsOptional.get();

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
            }


        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(false);
        }

        return ResponseEntity.status(HttpStatus.OK).body(true);
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
                if (ruleRepository.findByName(ruleName).isPresent()) {
                    newRules.add(ruleRepository.findByName(ruleName).get());
                }
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
     * Delete a specific test report of a test.
     *
     * @param reportId of the specific report to be deleted
     * @return if successful or not
     */
    public ResponseEntity<Void> deleteReport(String reportId) {
        try {
            Optional<TestReport> testReportOptional = testReportRepository.findById(reportId);
            testReportOptional.ifPresent(testReport -> testReportRepository.delete(testReport));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Deletes all test reports corresponding to a specific test.
     *
     * @param testId of the test from which the reports should be deleted.
     */
    public void deleteAllReports(String testId) {
        Optional<TestDetails> testDetailsOptional = testDetailsRepository.findById(testId);

        if (testDetailsOptional.isPresent()) {
            List<TestReport> testReportList = testReportRepository.findAllByName(testDetailsOptional.get().getName());
            testReportRepository.deleteAll(testReportList);
        }


    }
}