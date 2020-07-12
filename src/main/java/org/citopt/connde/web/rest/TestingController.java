package org.citopt.connde.web.rest;

import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.adapter.parameters.ParameterInstance;
import org.citopt.connde.domain.component.Sensor;
import org.citopt.connde.domain.rules.Rule;
import org.citopt.connde.domain.testing.TestDetails;
import org.citopt.connde.repository.TestDetailsRepository;
import org.citopt.connde.service.testing.GraphPlotter;
import org.citopt.connde.service.testing.TestEngine;
import org.citopt.connde.service.testing.TestReport;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@RestController
@RequestMapping(value = RestConfiguration.BASE_PATH)
public class TestingController {
    @Autowired
    private RestDeploymentController restDeploymentController;

    @Autowired
    private TestDetailsRepository testDetailsRepository;

    @Autowired
    private TestEngine testEngine;

    @Autowired
    private GraphPlotter graphPlotter;

    @Autowired
    private TestReport testReport;


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
        Map<String, List<Double>> valueListTest = testEngine.executeTest(testDetails);

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


    @GetMapping(value = "/test-details/pdfList/{testId}")
    public Map<String, String> getPDFList(@PathVariable(value = "testId") String testId) throws IOException {
        Map<String, String> pdfEntry = new HashMap<>();

        JSONObject parameterObject = new JSONObject();
        JSONArray parameterArray = new JSONArray();
        Pattern pattern = Pattern.compile( "_(.*?).pdf" );
        String patternDate = "dd.MM.yyyy HH:mm:ss";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(patternDate);
        String date = "";


        TestDetails testDetails = testDetailsRepository.findOne(testId);
        Stream<Path> stream = Files.find(Paths.get(testDetails.getPathPDF()), 10, (path, basicFileAttributes) -> {
            File file = path.toFile();
            return !file.isDirectory() &&
                    file.getName().contains(testId+"_");
        });

        List<Path> files = stream.collect(Collectors.toList());

        List<String> files2 = new ArrayList<>();

        for (Path singlePath: files) {
            Matcher machter = pattern.matcher(singlePath.toString());
            if(machter.find()){
                Long dateMilliseconds = Long.valueOf(machter.group(1));
                date = simpleDateFormat.format(new Date(dateMilliseconds*1000));
            }
                //Add properties to object
                pdfEntry.put(date, singlePath.getFileName().toString());



    //        String pathToAdd = singlePath.toString();
     //       files2.add(pathToAdd);
        }

        return pdfEntry;
    }


    /**
     * Opens the selected Test-Report from the Testlist
     *
     * @return HttpStatus
     */
    @GetMapping(value = "/test-details/downloadPDF/{path}")
    public ResponseEntity<String> openPDF(@PathVariable(value = "path") String path) throws IOException {
        return testEngine.downloadPDF( path );

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
    public ResponseEntity<List<ParameterInstance>> editConfig(@PathVariable(value = "testId") String testId,
                                                              @RequestBody String useNewData) {


        TestDetails testDetails = testDetailsRepository.findById(testId);
        List<ParameterInstance> config = testDetails.getConfig();

        for (ParameterInstance parameterInstance : config) {
            if (parameterInstance.getName().equals("useNewData")) {
                parameterInstance.setValue(Boolean.valueOf(useNewData));
            }
        }

        // save the changes in the database
        testDetails.setConfig(config);
        testDetailsRepository.save(testDetails);

        return new ResponseEntity<>(config, HttpStatus.OK);
    }

    @PostMapping(value = "/test-details/deleteTestreport/{testId}")
    public ResponseEntity<String> deleteTestReport(@PathVariable(value = "testId") String testId) {
        ResponseEntity response;

        TestDetails testDetails = testDetailsRepository.findById(testId);

        if(testDetails.isPdfExists()){
            Path pathTestReport = Paths.get(testDetails.getPathPDF());
            Path pathDiagram = Paths.get(pathTestReport.getParent().toString() , testId + ".gif");

            try {
                Files.delete(pathTestReport);
                Files.delete(pathDiagram);
                response = new ResponseEntity<>("Testreport successfully deleted", HttpStatus.OK);
            } catch (NoSuchFileException x) {
                response = new ResponseEntity<>("Testreport doesn't extist.", HttpStatus.NOT_FOUND);
            } catch (IOException x) {
                response = new ResponseEntity<>(x, HttpStatus.CONFLICT);
            }
        } else {
            response = new ResponseEntity<>("No available Testreport for this Test.", HttpStatus.NOT_FOUND);
        }



        return response;


    }

}
