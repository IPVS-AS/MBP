package org.citopt.connde.web.rest;

import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.adapter.parameters.ParameterInstance;
import org.citopt.connde.domain.rules.Rule;
import org.citopt.connde.domain.testing.TestDetails;
import org.citopt.connde.repository.TestDetailsRepository;
import org.citopt.connde.service.testing.*;
import org.citopt.connde.service.testing.analyzer.TestAnalyzer;
import org.citopt.connde.service.testing.executor.TestExecutor;
import org.citopt.connde.service.testing.rerun.TestRerunService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;


@RestController
@RequestMapping(value = RestConfiguration.BASE_PATH)
public class TestingController {

    @Autowired
    private TestDetailsRepository testDetailsRepository;

    @Autowired
    private TestEngine testEngine;

    @Autowired
    private TestExecutor testExecutor;

    @Autowired
    private TestRerunService testRerunService;

    @Autowired
    private TestAnalyzer testAnalyzer;


    /**
     * Starts the selected Test and creates the TestReport with the corresponding line chart.
     *
     * @param testId ID of the test to be executed
     * @return list of the simulated values
     */
    @PostMapping(value = "/test-details/test/{testId}")
    public ResponseEntity executeTest(@PathVariable(value = "testId") String testId) {
        TestDetails testDetails = testDetailsRepository.findOne(testId);
        try {
            // Start the test and get Map of sensor values
            testExecutor.executeTest(testDetails);
        } catch (Exception e) {
            return new ResponseEntity<>("An Error occurred during the test", HttpStatus.OK);
        }
        return new ResponseEntity<>("Test was completed successfully.", HttpStatus.OK);
    }


    /**
     * Stops the sensors of the running test.
     *
     * @param testId ID of the test to be stopped
     * @return if stopping the sensors was successful
     */
    @PostMapping(value = "/test-details/test/stop/{testId}")
    public ResponseEntity<String> stopTest(@PathVariable(value = "testId") String testId) {
        try {
            testExecutor.stopTest(testId);
        } catch (Exception e) {
            return new ResponseEntity<>("An Error occurred during stopping the test.", HttpStatus.OK);
        }

        return new ResponseEntity<>("Test was stopped successfully.", HttpStatus.OK);
    }


    /**
     * Adds or deletes the rerun Components for the specific test.
     *
     * @param testId ID of the specific test
     * @return ResponseEntity (if successful or not)
     */
    @PostMapping(value = "/test-details/rerun-components/{testId}")
    public ResponseEntity editRerunComponents(@PathVariable(value = "testId") String testId) {
        TestDetails testDetails = testDetailsRepository.findOne(testId);
        ResponseEntity responseEntity;
        try {
            // Add or deletes Rerun Components for the specific test
            testRerunService.editRerunComponents(testDetails);
            responseEntity = new ResponseEntity(HttpStatus.OK);
        } catch (Exception e) {
            responseEntity = new ResponseEntity(HttpStatus.NOT_FOUND);
        }

        return responseEntity;
    }


    /**
     * Returns a HashMap with date and path to of all Test Reports regarding to a specific test.
     *
     * @param testId ID of the test from which all reports are to be found
     * @return HttpsStatus and HashMap with all Reports regarding to the specific test
     */
    @GetMapping(value = "/test-details/pdfList/{testId}")
    public ResponseEntity getPDFList(@PathVariable(value = "testId") String testId) {
        return testEngine.getPDFList(testId);
    }

    @GetMapping(value = "/test-details/ruleList/{testId}")
    public List<Rule> ruleList(@PathVariable(value = "testId") String testId) {
        TestDetails test = testDetailsRepository.findOne(testId);

        // get  information about the status of the rules before the execution of the test
        return testAnalyzer.getCorrespondingRules(test);
    }


    /**
     * Opens the selected Test-Report from the Testlist
     *
     * @return HttpStatus
     */
    @GetMapping(value = "/test-details/downloadPDF/{path}")
    public ResponseEntity openPDF(@PathVariable(value = "path") String path) throws IOException {
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
     * Changes the value "UseNewData", changed with the switch button, in the database.
     *
     * @param testId     ID of the test in which the configuration is to be changed.
     * @param useNewData boolean, whether a new data set should be used or not
     * @return edited configuration
     */
    @PostMapping(value = "/test-details/editConfig/{testId}")
    public ResponseEntity<List<List<ParameterInstance>>> editConfig(@PathVariable(value = "testId") String testId,
                                                                    @RequestBody String useNewData) {
        List<List<ParameterInstance>> configList = testRerunService.editUseNewData(testId, useNewData);
        return new ResponseEntity<>(configList, HttpStatus.OK);
    }


    /**
     * Deletes the last test report of the specific test.
     *
     * @param testId id of the test from which the report should be deleted
     * @return if deletion worked or not
     */
    @PostMapping(value = "/test-details/deleteTestreport/{testId}")
    public ResponseEntity deleteTestReport(@PathVariable(value = "testId") String testId) {
        return testEngine.deleteReport(testId);
    }


    /**
     * Changes the value "UseNewData", changed with the switch button, in the database.
     *
     * @return edited configuration
     */
    @RequestMapping(value = "/test-details/addRerunOperator", method = RequestMethod.POST)
    public ResponseEntity<String> addRerunOperator() {
        return testRerunService.addRerunOperators();
    }

    /**
     * Updates the configuration of the whole test, if the user change it.
     *
     * @param testId            of the test to be updated
     * @param updateInformation updates that should be added to the test
     * @return if the updated worked or not
     */
    @RequestMapping(value = "/test-details/updateTest/{testId}", method = RequestMethod.POST)
    public HttpEntity<Object> updateTest(@PathVariable(value = "testId") String testId, @RequestBody String updateInformation) {
        return testEngine.editTestConfig(testId, updateInformation);
    }

    /**
     * Registers the test device used for testing purposes.
     *
     * @return response entity if the registration was successful or not
     */
    @PostMapping(value = "/test-details/registerTestDevice")
    public ResponseEntity registerTestDevice() {
        return testEngine.registerTestDevice();
    }

    /**
     * Registers the test device used for testing purposes.
     *
     * @return response entity if the registration was successful or not
     */
    @PostMapping(value = "/test-details/registerTestActuator")
    public ResponseEntity<String> registerTestActuator() {
        return testEngine.registerTestActuator();
    }

    /**
     * Register a one dimensional sensor simulator used for testing purposes.
     *
     * @return response entity if the registration was successful or not
     */
    @PostMapping(value = "/test-details/registerSensorSimulator")
    public ResponseEntity<String> registerSensorSimulator(@RequestBody String sensorName) {
        return testEngine.registerSensorSimulator(sensorName);
    }


    /**
     * Register a three dimensional sensor simulator used for testing purposes.
     *
     * @return response entity if the registration was successful or not
     */
    @PostMapping(value = "/test-details/registerThreeDimSensorSimulator")
    public ResponseEntity<String> registerThreeDimSensorSimulator(@RequestBody String sensorName) {
        testEngine.registerThreeDimSensorSimulator(sensorName);
        return new ResponseEntity<>("needs to be implemented", HttpStatus.OK);
    }


    /**
     * Opens the selected Test-Report from the Testlist
     *
     * @return HttpStatus
     */
    @GetMapping(value = "/test-details/checkRegistration")
    public Boolean checkRegistration(@RequestBody String sensorName) {
        return testEngine.isSimulatorRegistr(sensorName);
    }


}
