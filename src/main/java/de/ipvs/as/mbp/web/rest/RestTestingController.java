package de.ipvs.as.mbp.web.rest;


import de.ipvs.as.mbp.RestConfiguration;
import de.ipvs.as.mbp.domain.access_control.ACAccessRequest;
import de.ipvs.as.mbp.domain.access_control.ACAccessType;
import de.ipvs.as.mbp.domain.component.Sensor;
import de.ipvs.as.mbp.domain.operator.parameters.ParameterInstance;
import de.ipvs.as.mbp.domain.rules.Rule;
import de.ipvs.as.mbp.domain.testing.TestDetails;
import de.ipvs.as.mbp.domain.testing.TestDetailsDTO;
import de.ipvs.as.mbp.domain.testing.TestReport;
import de.ipvs.as.mbp.error.EntityNotFoundException;
import de.ipvs.as.mbp.error.MissingPermissionException;
import de.ipvs.as.mbp.repository.RuleRepository;
import de.ipvs.as.mbp.repository.SensorRepository;
import de.ipvs.as.mbp.repository.TestDetailsRepository;
import de.ipvs.as.mbp.repository.TestReportRepository;
import de.ipvs.as.mbp.service.UserEntityService;
import de.ipvs.as.mbp.service.testing.TestEngine;
import de.ipvs.as.mbp.service.testing.analyzer.TestAnalyzer;
import de.ipvs.as.mbp.service.testing.executor.TestExecutor;
import de.ipvs.as.mbp.service.testing.rerun.TestRerunService;

import java.util.ArrayList;
import java.util.List;

import java.util.Map;


import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


@RestController
@RequestMapping(RestConfiguration.BASE_PATH + "/test-details")
public class RestTestingController {

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

    @Autowired
    private UserEntityService userEntityService;

    @Autowired
    private RuleRepository ruleRepository;

    @Autowired
    private SensorRepository sensorRepository;

    @Autowired
    private TestReportRepository testReportRepository;

    @GetMapping(produces = "application/hal+json")
    @ApiOperation(value = "Retrieves all existing tests available for the requesting entity.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"),
            @ApiResponse(code = 404, message = "Test not found!")})
    public ResponseEntity<PagedModel<EntityModel<TestDetails>>> all(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @ApiParam(value = "Page parameters", required = true) Pageable pageable) {
        // Parse the access-request information
        ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);

        // Retrieve the corresponding devices (includes access-control)
        List<TestDetails> tests = userEntityService.getPageWithAccessControlCheck(testDetailsRepository, ACAccessType.READ, accessRequest, pageable);

        // Create self link
        Link selfLink = linkTo(methodOn(getClass()).all(accessRequestHeader, pageable)).withSelfRel();


        return ResponseEntity.ok(userEntityService.entitiesToPagedModel(tests, selfLink, pageable));
    }

    @GetMapping(path = "/{testId}", produces = "application/hal+json")
    @ApiOperation(value = "Retrieves an existing tests identified by its id.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"),
            @ApiResponse(code = 401, message = "Not authorized to access the test!"),
            @ApiResponse(code = 404, message = "Test or requesting user not found!")})
    public ResponseEntity<EntityModel<TestDetails>> one(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @PathVariable("testId") String testId) throws EntityNotFoundException, MissingPermissionException {
        // Parse the access-request information
        ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);

        // Retrieve the corresponding test
        TestDetails testDetails = userEntityService.getForIdWithAccessControlCheck(testDetailsRepository, testId, ACAccessType.READ, accessRequest);
        return ResponseEntity.ok(userEntityService.entityToEntityModel(testDetails));
    }


    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/hal+json")
    @ApiOperation(value = "Retrieves an existing test entity identified by its id if it's available.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"),
            @ApiResponse(code = 409, message = "Test already exists!")})
    public ResponseEntity<EntityModel<TestDetails>> create(
            @RequestBody TestDetailsDTO requestDto
    ) throws EntityNotFoundException {


        List<Rule> rules = new ArrayList<>();
        List<Sensor> sensors = new ArrayList<>();

        for (String ruleName : requestDto.getRuleNames()) {
            if (ruleRepository.findByName(ruleName).isPresent()) {
                rules.add(ruleRepository.findByName(ruleName).get());
            }
        }

        for (String sensorName : requestDto.getType()) {
            if (sensorRepository.findByName(sensorName).isPresent()) {
                sensors.add(sensorRepository.findByName(sensorName).get());
            }
        }


        // Create sensor from request DTO
        TestDetails testDetails = new TestDetails();
        testDetails.setName(requestDto.getName());
        testDetails.setTriggerRules(requestDto.getTriggerRules());
        testDetails.setType(requestDto.getType());
        testDetails.setRuleNames(requestDto.getRuleNames());
        testDetails.setConfig(requestDto.getConfig());
        testDetails.setUseNewData(requestDto.isUseNewData());
        testDetails.setRules(rules);
        testDetails.setSensor(sensors);
        testDetails.setAccessControlPolicyIds(requestDto.getAccessControlPolicyIds());

        // Save test in the database
        TestDetails createdTest = userEntityService.create(testDetailsRepository, testDetails);

        return ResponseEntity.ok(userEntityService.entityToEntityModel(createdTest));
    }


    @DeleteMapping(value = "/{testId}")
    @ApiOperation(value = "Deletes an existing test entity identified by its id if it's available for the requesting entity.")
    @ApiResponses({@ApiResponse(code = 204, message = "Success!"),
            @ApiResponse(code = 401, message = "Not authorized to delete the test!"),
            @ApiResponse(code = 404, message = "Test or requesting user not found!")})
    public ResponseEntity<Void> delete(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @PathVariable("testId") String testId) throws MissingPermissionException, EntityNotFoundException {

        if(testDetailsRepository.findById(testId).isPresent()){
            testRerunService.deleteRerunComponents(testDetailsRepository.findById(testId).get());
            testEngine.deleteAllReports(testId);
        }

        // Parse the access-request information
        ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);

        // Delete the device (includes access-control)
        userEntityService.deleteWithAccessControlCheck(testDetailsRepository, testId, accessRequest);
        return ResponseEntity.noContent().build();
    }


    /**
     * Starts the selected Test and creates the TestReport with the corresponding line chart.
     *
     * @param testId ID of the test to be executed
     * @return list of the simulated values
     */
    @PostMapping(value = "/test/{testId}")
    @ApiOperation(value = "Deletes an existing test entity identified by its id if it's available for the requesting entity.")
    @ApiResponses({@ApiResponse(code = 204, message = "Success!"),
            @ApiResponse(code = 401, message = "Not authorized to delete the test!"),
            @ApiResponse(code = 404, message = "Test or requesting user not found!")})
    public ResponseEntity<Boolean> executeTest(@PathVariable(value = "testId") String testId

    ) {
        try {
            TestDetails testDetails = testDetailsRepository.findById(testId).get();

            // Start the test and get Map of sensor values
            testExecutor.executeTest(testDetails);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
        return ResponseEntity.status(HttpStatus.OK).body(true);

    }


    /**
     * Starts the selected Test and creates the TestReport with the corresponding line chart.
     *
     * @param testId ID of the test to be executed
     * @return list of the simulated values
     */
    @PostMapping(value = "/rerun-test/{testId}/{testReportId}")
    @ApiOperation(value = "Deletes an existing test entity identified by its id if it's available for the requesting entity.")
    @ApiResponses({@ApiResponse(code = 204, message = "Success!"),
            @ApiResponse(code = 401, message = "Not authorized to delete the test!"),
            @ApiResponse(code = 404, message = "Test or requesting user not found!")})
    public ResponseEntity<Boolean> rerunTest(@PathVariable(value = "testId") String testId,
                                             @PathVariable(value = "testReportId") String testReportId

    ) {
        try {
            TestDetails testDetails = testDetailsRepository.findById(testId).get();

            testExecutor.rerunTest(testDetails, testReportId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
        return ResponseEntity.status(HttpStatus.OK).body(true);
    }


    /**
     * Stops the sensors of the running test.
     *
     * @param testId ID of the test to be stopped
     * @return if stopping the sensors was successful
     */
    @PostMapping(value = "/test/stop/{testId}")
    public ResponseEntity<Boolean> stopTest(@PathVariable(value = "testId") String testId) {
        try {
            testExecutor.stopTest(testId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
        return ResponseEntity.status(HttpStatus.OK).body(true);
    }



    /**
     * Returns a HashMap with date and path to of all Test Reports regarding to a specific test.
     *
     * @param testId ID of the test from which all reports are to be found
     * @return HttpsStatus and HashMap with all Reports regarding to the specific test
     */
    @GetMapping(value = "/pdfList/{testId}")
    public ResponseEntity<Map<Long, TestReport>> getPDFList(@PathVariable(value = "testId") String testId) {

        return testEngine.getPDFList(testId);
    }

    /**
     * Returns a specific Test Report regarding to a specific test.
     *
     * @param reportId ID of the test report to be found
     * @return Https Status with the Report regarding to the specific test
     */
    @GetMapping(value = "/test-report/{reportId}")
    public Map<String, ArrayList> getTestReport(@PathVariable(value = "reportId") String reportId) {

        return testEngine.getSimulationValues(reportId);
    }


    @GetMapping(value = "/ruleList/{testId}")
    public List<Rule>ruleList(@PathVariable(value = "testId") String testId) {
        // get  information about the status of the rules before the execution of the test
        List<Rule> ruleList = testAnalyzer.getCorrespondingRules(testDetailsRepository.findById(testId).get());
        return  ruleList;
    }


    /**
     * Checks if pdf for the specific test exists.
     *
     * @param testId ID of the specific test
     * @return boolean, if pdf exists
     */
    @GetMapping(value = "/pdfExists/{testId}")
    public void pdfExists(@PathVariable(value = "testId") String testId) {
        TestDetails test = testDetailsRepository.findById(testId).get();
        // return test.isPdfExists();
    }

    /**
     * Deletes a specific test report of the specific test defined by the user.
     *
     * @param reportId id of the test report which should be deleted
     * @return if deletion worked or not
     */
    @PostMapping(value = "/deleteTestReport/{reportId}")
    public ResponseEntity<Boolean> deleteTestReport(@PathVariable(value = "reportId") String reportId) {
        return testEngine.deleteReport(reportId);
    }


    /**
     * Changes the value "UseNewData", changed with the switch button, in the database.
     *
     * @param testId     ID of the test in which the configuration is to be changed.
     * @param useNewData boolean, whether a new data set should be used or not
     * @return edited configuration
     */
    @PostMapping(value = "/editConfig/{testId}")
    public ResponseEntity<List<List<ParameterInstance>>> editConfig(@PathVariable(value = "testId") String testId,
                                                                    @RequestBody boolean useNewData) {
        List<List<ParameterInstance>> configList = testRerunService.editUseNewData(testId, useNewData);
        return new ResponseEntity<>(configList, HttpStatus.OK);
    }

    /**
     * Updates the configuration of the whole test, if the user change it.
     *
     * @param testId            of the test to be updated
     * @param updateInformation updates that should be added to the test
     * @return if the updated worked or not
     */
    @PostMapping(value = "/updateTest/{testId}")
    public ResponseEntity<Boolean> updateTest(@PathVariable(value = "testId") String testId, @RequestBody String updateInformation) {
        return testEngine.editTestConfig(testId, updateInformation);

    }


}