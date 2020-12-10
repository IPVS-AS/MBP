package org.citopt.connde.service.testing;


import org.citopt.connde.domain.adapter.Adapter;
import org.citopt.connde.domain.adapter.parameters.ParameterInstance;
import org.citopt.connde.domain.component.Actuator;
import org.citopt.connde.domain.component.ActuatorValidator;
import org.citopt.connde.domain.component.Sensor;
import org.citopt.connde.domain.component.SensorValidator;
import org.citopt.connde.domain.device.Device;
import org.citopt.connde.domain.device.DeviceValidator;
import org.citopt.connde.domain.rules.Rule;
import org.citopt.connde.domain.testing.TestDetails;
import org.citopt.connde.repository.*;
import org.citopt.connde.web.rest.RestRuleController;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;


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

@Component
public class TestEngine<THREE_DIM_SIMULATOR_LIST> {


    @Autowired
    private TestDetailsRepository testDetailsRepository;

    @Autowired
    private RuleRepository ruleRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private DeviceValidator deviceValidator;

    @Autowired
    private ActuatorValidator actuatorValidator;

    @Autowired
    private PropertiesService propertiesService;

    @Autowired
    ActuatorRepository actuatorRepository;

    @Autowired
    AdapterRepository adapterRepository;

    @Autowired
    SensorRepository sensorRepository;

    @Autowired
    SensorValidator sensorValidator;


    @Value("#{'${testingTool.threeDimensionalSensor}'.split(',')}")
    List<String> THREE_DIM_SIMULATOR_LIST;


    //To resolve ${} in @Value
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    private final String TEST_DEVICE;
    private final String TEST_DEVICE_IP;
    private final String TEST_DEVICE_USERNAME;
    private final String TEST_DEVICE_PASSWORD;
    private final String ACTUATOR_NAME;


    public TestEngine() throws IOException {
        propertiesService = new PropertiesService();
        TEST_DEVICE = propertiesService.getPropertiesString("testingTool.testDeviceName");
        TEST_DEVICE_IP = propertiesService.getPropertiesString("testingTool.ipAddressTestDevice");
        TEST_DEVICE_USERNAME = propertiesService.getPropertiesString("testingTool.testDeviceUserName");
        TEST_DEVICE_PASSWORD = propertiesService.getPropertiesString("testingTool.testDevicePassword");
        ACTUATOR_NAME = propertiesService.getPropertiesString("testingTool.actuatorName");
    }


    /**
     * Update the test configurations redefined by the user.
     *
     * @param testID Id of the test to be modified
     * @param changes to be included
     * @return if update was successful or not
     */
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
    public ResponseEntity<Serializable> downloadPDF(String path) throws IOException {
        TestDetails test = null;
        ResponseEntity<Serializable> respEntity;
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

            respEntity = new ResponseEntity<>(out, responseHeaders, HttpStatus.OK);
            inputStream.close();
        } else {
            respEntity = new ResponseEntity<>("File Not Found", HttpStatus.NOT_FOUND);
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
    public ResponseEntity<Map<Long, String>> getPDFList(String testId) {
        ResponseEntity<Map<Long, String>> pdfList;
        Map<Long, String> nullList = new TreeMap<>();
        TestDetails testDetails = testDetailsRepository.findOne(testId);
        try {
            if (testDetails.isPdfExists()) {
                Stream<Path> pathStream = Files.find(Paths.get(testDetails.getPathPDF()), 10, (path, basicFileAttributes) -> {
                    File file = path.toFile();
                    return !file.isDirectory() &&
                            file.getName().contains(testId + "_");
                });

                pdfList = new ResponseEntity<>(generateReportList(pathStream), HttpStatus.OK);
            } else {
                pdfList = new ResponseEntity<>(nullList, HttpStatus.OK);
            }


        } catch (IOException e) {
            pdfList = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return pdfList;
    }


    /**
     * Generates a HashMap where the entries consist of the creation date of the report and the path to it.
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

    /**
     * Registers the Testing Device which is used for testing purposes.
     *
     * @return response entity if insertion was successful or not
     */
    public ResponseEntity registerTestDevice() {
        ResponseEntity responseEntity;
        //Validation errors
        Errors errors;

        try {
            // Check if device with this name is already registered
            Device testDevice = deviceRepository.findByName(TEST_DEVICE);
            if (testDevice == null) {
                //Enrich device for details
                testDevice = new Device();
                testDevice.setName(TEST_DEVICE);
                testDevice.setComponentType("Computer");
                testDevice.setIpAddress(TEST_DEVICE_IP);
                testDevice.setUsername(TEST_DEVICE_USERNAME);
                testDevice.setPassword(TEST_DEVICE_PASSWORD);
            }

            // Insert the new testing device into the device repository
            deviceRepository.insert(testDevice);

            //Validate device
            errors = new BeanPropertyBindingResult(testDevice, "device");
            deviceValidator.validate(testDevice, errors);

            responseEntity = new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception exception) {
            responseEntity = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return responseEntity;


    }

    /**
     * Registers the Testing Actuator which is used for testing purposes and does't make any real actions.
     *
     * @return response entity if insertion was successful or not
     */
    public ResponseEntity<String> registerTestActuator() {
        //Validation errors
        Errors errors;
        ResponseEntity responseEntity = null;

        try {
            Actuator testingActuator = actuatorRepository.findByName(ACTUATOR_NAME);
            Device testDevice = deviceRepository.findByName(TEST_DEVICE);
            Adapter testActuatorAdapter = adapterRepository.findByName(ACTUATOR_NAME);

            // Check if testing device and actuator are already registered
            if (testDevice == null) {
                // Register the Testing device automatically if not existing
                registerTestDevice();
            } else {
                // Check if Actuator is already existing
                if (testingActuator == null) {
                    // Check if the corresponding adapter is registered
                    if (testActuatorAdapter != null) {
                        //Enrich actuator for details
                        testingActuator = new Actuator();
                        testingActuator.setName(ACTUATOR_NAME);
                        testingActuator.setOwner(null);
                        testingActuator.setDevice(testDevice);
                        testingActuator.setAdapter(testActuatorAdapter);
                        testingActuator.setComponentType("Buzzer");

                        //Validate device
                        errors = new BeanPropertyBindingResult(testingActuator, "component");
                        actuatorValidator.validate(testingActuator, errors);

                        actuatorRepository.insert(testingActuator);

                        responseEntity = new ResponseEntity("Testing Actuator successfully created.", HttpStatus.CREATED);

                    }
                    responseEntity = new ResponseEntity("Please register the Testing Actuator first.", HttpStatus.NOT_FOUND);
                }
                responseEntity = new ResponseEntity("Testing Actuator already exists.", HttpStatus.CONFLICT);
            }


        } catch (Exception e) {
            responseEntity = new ResponseEntity("Error during creation of the Actuator.", HttpStatus.CONFLICT);
        }
        return responseEntity;
    }

    /**
     * Registers the wished sensor simulator if the corresponding adapter is already registered
     *
     * @param sensorName Name of the sensor simulator to be registered
     * @return ResponseEntity if the registration was successful or not
     */
    public ResponseEntity<String> registerSensorSimulator(String sensorName) {
        ResponseEntity<String> responseEntity;

        //Validation errors
        Errors errors;

        Adapter sensorAdapter = adapterRepository.findByName(sensorName);
        Device testingDevice = deviceRepository.findByName(TEST_DEVICE);
        Sensor sensorSimulator = sensorRepository.findByName(sensorName);

        try {
            // Check if corresponding adapter exists
            if (sensorAdapter == null) {
                return new ResponseEntity<>("Cloud not create Sensor.", HttpStatus.CONFLICT);
            } else if (testingDevice == null) {
                registerTestDevice();
            } else if (sensorSimulator == null) {
                //Enrich actuator for details
                sensorSimulator = new Sensor();
                sensorSimulator.setName(sensorName);
                sensorSimulator.setOwner(null);

                if (sensorName.contains("Temperature")) {
                    sensorSimulator.setComponentType("Temperature");
                } else if (sensorName.contains("Humidity")) {
                    sensorSimulator.setComponentType("Humidity");
                } else {
                    sensorSimulator.setComponentType("Motion");
                }

                sensorSimulator.setAdapter(sensorAdapter);
                sensorSimulator.setDevice(testingDevice);

                //Validate device
                errors = new BeanPropertyBindingResult(sensorSimulator, "component");
                sensorValidator.validate(sensorSimulator, errors);

                sensorRepository.insert(sensorSimulator);

            }
            responseEntity = new ResponseEntity<>("Sensor successfully created", HttpStatus.OK);
        } catch (Exception e) {
            responseEntity = new ResponseEntity<>("Error during creation of the Sensor.", HttpStatus.CONFLICT);
        }

        return responseEntity;
    }

    /**
     * Checks if the one and three dimensional sensor simulators are already registered.
     *
     * @param sensor Name of the sensor to be checked
     * @return Boolean if the sensor is already registered or not
     */
    public Boolean isSimulatorRegistr(String sensor) {
        Boolean registered = false;
        String dimX = sensor + "X";
        String dimY = sensor + "Y";
        String dimZ = sensor + "Z";

        if (THREE_DIM_SIMULATOR_LIST.contains(sensor)) {
            Sensor sensorX = sensorRepository.findByName(dimX);
            Sensor sensorY = sensorRepository.findById(dimY);
            Sensor sensorZ = sensorRepository.findByName(dimZ);

            if (sensorX != null && sensorY != null && sensorZ != null) {
                registered = true;
            }
        } else {
            if (sensorRepository.findByName(sensor) != null) {
                registered = true;
            }
        }

        return registered;
    }


    /**
     * Registers the wished three dimensional sensor simulator if the corresponding adapter is already registered.
     *
     * @param sensorName of the three dimensional sensor to be registered
     * @return Response entity if registration was successful
     */
    public void registerThreeDimSensorSimulator(String sensorName) {
       //TODO

    }




}