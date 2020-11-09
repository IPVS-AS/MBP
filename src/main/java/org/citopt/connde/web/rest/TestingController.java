package org.citopt.connde.web.rest;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.citopt.connde.RestConfiguration;
import org.citopt.connde.constants.Constants;
import org.citopt.connde.domain.adapter.Adapter;
import org.citopt.connde.domain.adapter.parameters.ParameterInstance;
import org.citopt.connde.domain.component.Sensor;
import org.citopt.connde.domain.rules.Rule;
import org.citopt.connde.domain.testing.TestDetails;
import org.citopt.connde.repository.AdapterRepository;
import org.citopt.connde.repository.RuleRepository;
import org.citopt.connde.repository.SensorRepository;
import org.citopt.connde.repository.TestDetailsRepository;
import org.citopt.connde.service.testing.GraphPlotter;
import org.citopt.connde.service.testing.TestEngine;
import org.citopt.connde.service.testing.TestReport;
import org.citopt.connde.service.testing.TestRerunOperatorService;
import org.citopt.connde.web.rest.response.ActionResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@RestController
@RequestMapping(value = RestConfiguration.BASE_PATH)
public class TestingController {
    @Autowired
    private RestDeploymentController restDeploymentController;

    @Autowired
    private TestDetailsRepository testDetailsRepository;

    @Autowired
    private RuleRepository ruleRepository;

    @Autowired
    private TestEngine testEngine;

    @Autowired
    private GraphPlotter graphPlotter;

    @Autowired
    private TestReport testReport;

    @Autowired
    private AdapterRepository adapterRepository;

    @Autowired
    private SensorRepository sensorRepository;

    @Autowired
    private TestRerunOperatorService rerunOperatorService;

    String[] sensorSim = {"TestingTemperaturSensor", "TestingTemperaturSensorPl", "TestingFeuchtigkeitsSensor", "TestingFeuchtigkeitsSensorPl", "TestingGPSSensorPl", "TestingGPSSensor", "TestingBeschleunigungsSensor", "TestingBeschleunigungsSensorPl"};
    List<String> sensorSimulators = Arrays.asList(sensorSim);



    /**
     * Called when the client wants to load default operators and make them available for usage
     * in actuators and sensorss by all users.
     *
     * @return An action response containing the result of the request
     */
    @PostMapping(value = "/test-details/rerun-operators")
    @Secured({Constants.ADMIN})
    @ApiOperation(value = "Loads default operators from the resource directory of the MBP and makes them available for usage in actuators and sensors by all users.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 403, message = "Not authorized to perform this action"), @ApiResponse(code = 500, message = "Default operators could not be added")})
    public ResponseEntity<ActionResponse> addRerunOperators() {
        //Call corresponding service function
        ActionResponse response = rerunOperatorService.addDefaultOperators();

        //Check for success
        if (response.isSuccess()) {
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        //No success
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    /**
     * Starts the selected Test and creates the TestReport with the corresponding line chart.
     *
     * @param testId ID of the test to be executed
     * @return list of the simulated values
     * @throws Exception
     */
    @PostMapping(value = "/test-details/test/{testId}")
    public String executeTest(@PathVariable(value = "testId") String testId) throws Exception {
        TestDetails testDetails = testDetailsRepository.findOne(testId);

        // Set the exact start time of the test
        testDetails.setStartTestTimeNow();
        testDetailsRepository.save(testDetails);

        // get  informations about the status of the rules before the execution of the test
        List<Rule> rulesbefore = testEngine.getStatRulesBefore(testDetails);

        // Start the test and get Map of sensor values
        Map<String, LinkedHashMap<Long, Double>> valueListTest = testEngine.executeTest(testDetails);

        // Check the test for success
        testEngine.testSuccess(testId);
        TestDetails testDetails3 = testDetailsRepository.findOne(testId);

        // Create test report with graph of sensor values and pdf
        graphPlotter.createTestReport(testDetails3);
        String pdfPath = testReport.generateTestreport(testDetails3.getId(), rulesbefore);
        testDetails3.setPathPDF(pdfPath);
        testDetails3.setPdfExists(true);

        // save success and path of test report to database
        testDetailsRepository.save(testDetails3);

        //return new ResponseEntity<>(valueListTest, HttpStatus.OK); Map<String, List<Double>>
        return pdfPath;
    }


    /**
     * Returns a Hashmap with date and path to of all Test Reports regarding to a specific test.
     *
     * @param testId ID of the test from which all reports are to be found
     * @return HttpsStatus and Hashmap with all Reports regarding to the specific test
     */
    @GetMapping(value = "/test-details/pdfList/{testId}")
    public ResponseEntity<Map<Long, String>> getPDFList(@PathVariable(value = "testId") String testId) throws IOException {
        return testEngine.getPDFList(testId);
    }

    @GetMapping(value = "/test-details/ruleList/{testId}")
    public List<Rule> ruleList(@PathVariable(value = "testId") String testId) throws IOException {
        TestDetails testDetails = testDetailsRepository.findOne(testId);

        // get  information about the status of the rules before the execution of the test
        return testEngine.getStatRulesBefore(testDetails);
    }


    /**
     * Opens the selected Test-Report from the Testlist
     *
     * @return HttpStatus
     */
    @GetMapping(value = "/test-details/downloadPDF/{path}")
    public ResponseEntity<String> openPDF(@PathVariable(value = "path") String path) throws IOException {
        return testEngine.downloadPDF(path);
    }

    /**
     * Checks if pdf for the specific test exists.
     *
     * @param testId ID of the specific test
     * @return boolean, if pdf exists
     */
    @GetMapping(value = "/test-details/pdfExists/{testId}")
    public boolean pdfExists(@PathVariable(value = "testId") String testId) {
        TestDetails test = testDetailsRepository.findById(testId);
        return test.isPdfExists();
    }


    /**
     * Stops the sensor of the running test.
     *
     * @param testId ID of the test to be openedID of the test to be openedID of the test to be opened
     * @return HttpStatus
     */
    @PostMapping(value = "/test-details/test/stop/{testId}")
    public ResponseEntity<Boolean> stopTest(@PathVariable(value = "testId") String testId) {
        TestDetails test = testDetailsRepository.findById(testId);
        // Stop every sensor running for the specific test
        for (Sensor sensor : test.getSensor()) {
            restDeploymentController.stopSensor(sensor.getId());
        }

        return new ResponseEntity<>(true, HttpStatus.OK);
    }


    /**
     * Changes the value "UseNewData", changed with the switch button, in the database.
     *
     * @param testId     ID of the test in which the configuration is to be changed.
     * @param useNewData boolean, wether a new data set should be used or not
     * @return edited configuration
     */
    @PostMapping(value = "/test-details/editConfig/{testId}")
    public ResponseEntity<List<List<ParameterInstance>>> editConfig(@PathVariable(value = "testId") String testId,
                                                                    @RequestBody String useNewData) {



        TestDetails testDetails = testDetailsRepository.findById(testId);
        List<List<ParameterInstance>> configList = testDetails.getConfig();

        if (!Boolean.valueOf(useNewData)) {
            testDetails.setUseNewData(false);
            testDetailsRepository.save(testDetails);
            addRerunOperators();
            for (List<ParameterInstance> config : configList) {
                for (ParameterInstance parameterInstance : config) {
                    if (parameterInstance.getName().equals("ConfigName")) {
                        if(!sensorSimulators.contains(parameterInstance.getValue())){
                            testEngine.addRerunSensor(parameterInstance.getValue().toString(), testDetails);
                            testEngine.addRerunRule(testDetails);
                        }
                    }
                }
            }
        } else{
            testDetails.setUseNewData(true);
            testDetailsRepository.save(testDetails);
            // Delete the Reuse Adapters and Sensors for each real sensor if the data should not be reused
            for (List<ParameterInstance> config : configList) {
                Adapter adapterReuse = adapterRepository.findByName("RERUN_OPERATOR");
                adapterRepository.delete(adapterReuse);
                for (ParameterInstance parameterInstance : config) {
                    if (parameterInstance.getName().equals("ConfigName")) {
                        if(!sensorSimulators.contains(parameterInstance.getValue())){
                            //Delete rerun rules
                            testEngine.deleteRerunRules(testDetails);

                            // Delete Reuse Operator
                            String reuseName = "RERUN_" + parameterInstance.getValue();
                            Sensor sensorReuse = sensorRepository.findByName(reuseName);
                            if(sensorReuse != null){
                                sensorRepository.delete(sensorReuse);
                                testDetails.getSensor().remove(sensorReuse);
                                testDetailsRepository.save(testDetails);
                                //TODO schau ob das so geht
                            }
                        }
                    }
                }
            }
        }


        // Change value for the configuration of every sensor simulator of the test
        for (List<ParameterInstance> config : configList) {
            for (ParameterInstance parameterInstance : config) {
                if (parameterInstance.getName().equals("useNewData")) {
                    parameterInstance.setValue(Boolean.valueOf(useNewData));
                }
            }
        }
        // save the changes in the database
        testDetails.setConfig(configList);
        testDetails.setUseNewData(Boolean.valueOf(useNewData));
        testDetailsRepository.save(testDetails);


        return new ResponseEntity<>(configList, HttpStatus.OK);
    }



    @PostMapping(value = "/test-details/deleteTestreport/{testId}")
    public ResponseEntity deleteTestReport(@PathVariable(value = "testId") String testId) {
        ResponseEntity response;

        TestDetails testDetails = testDetailsRepository.findById(testId);

        if (testDetails.isPdfExists()) {
            Path pathTestReport = Paths.get(testDetails.getPathPDF());
            Path pathDiagram = Paths.get(pathTestReport.getParent().toString(), testId + ".gif");

            try {
                Files.delete(pathTestReport);
                Files.delete(pathDiagram);
                response = new ResponseEntity<>("Testreport successfully deleted", HttpStatus.OK);
            } catch (NoSuchFileException x) {
                response = new ResponseEntity<>("Testreport doesn't exist.", HttpStatus.NOT_FOUND);
            } catch (IOException x) {
                response = new ResponseEntity<>(x, HttpStatus.CONFLICT);
            }
        } else {
            response = new ResponseEntity<>("No available Testreport for this Test.", HttpStatus.NOT_FOUND);
        }


        return response;


    }


    @RequestMapping(value = "/test-details/updateTest/{testId}", method = RequestMethod.POST)
    public HttpEntity<Object> updateTest(@PathVariable(value = "testId") String testId, @RequestBody String test) {
        try {
            ParameterInstance instance;
            TestDetails testToUpdate = testDetailsRepository.findById(testId);

            // Clear the configuration and rules field of the specific test
            testToUpdate.getConfig().clear();
            testToUpdate.getRules().clear();

            // convert the string of the request body to a JSONObject in order to continue working with it
            JSONObject updateInfos = new JSONObject(test);

            Object config = updateInfos.get("config");
            JSONArray configEntries = (JSONArray) config;

            List<List<ParameterInstance>> newConfig = new ArrayList<>();
            if (configEntries != null) {
                for (int i = 0; i < configEntries.length(); i++) {
                    JSONArray singleConfig = (JSONArray) configEntries.get(i);
                    List<ParameterInstance> newConfigInner = new ArrayList<>();
                    for (int j = 0; j < singleConfig.length(); j++) {
                        String edöfn = singleConfig.getJSONObject(j).getString("value");
                        instance = new ParameterInstance(singleConfig.getJSONObject(j).getString("name"), singleConfig.getJSONObject(j).getString("value"));
                        newConfigInner.add(instance);
                    }
                    newConfig.add(newConfigInner);

                }
            }
            testToUpdate.setConfig(newConfig);

            // Update the rules to be observed in the test
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
            testToUpdate.setRules(newRules);

            // Update the information if the rules which should be observed should be triggered while the test or not
            testToUpdate.setTriggerRules(updateInfos.getBoolean("triggerRules"));

            // Save all updates
            testDetailsRepository.save(testToUpdate);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e, HttpStatus.BAD_REQUEST);
        }


    }

}
