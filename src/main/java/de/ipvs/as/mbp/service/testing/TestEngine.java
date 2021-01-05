package de.ipvs.as.mbp.service.testing;


import de.ipvs.as.mbp.domain.component.Actuator;
import de.ipvs.as.mbp.domain.component.Sensor;
import de.ipvs.as.mbp.domain.device.Device;
import de.ipvs.as.mbp.domain.operator.Operator;
import de.ipvs.as.mbp.domain.operator.parameters.ParameterInstance;
import de.ipvs.as.mbp.domain.rules.Rule;
import de.ipvs.as.mbp.repository.*;
import de.ipvs.as.mbp.domain.testing.TestDetails;
import de.ipvs.as.mbp.repository.SensorRepository;
import org.apache.commons.io.filefilter.WildcardFileFilter;
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
public class TestEngine {


    @Autowired
    private TestDetailsRepository testDetailsRepository;

    @Autowired
    private RuleRepository ruleRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private PropertiesService propertiesService;

    @Autowired
    ActuatorRepository actuatorRepository;

    @Autowired
    OperatorRepository operatorRepository;

    @Autowired
    private SensorRepository sensorRepository;


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
     * @param testID  Id of the test to be modified
     * @param changes to be included
     * @return if update was successful or not
     */
    public HttpEntity<Object> editTestConfig(String testID, String changes) {
        try {
            TestDetails testToUpdate = testDetailsRepository.findById(testID).get();

            // Clear the configuration and rules field of the specific test
            testToUpdate.getConfig().clear();
            testToUpdate.getRules().clear();

            // convert the string of the request body to a JSONObject in order to continue working with it
            JSONObject updateInfos = new JSONObject(changes);

            List<List<ParameterInstance>> newConfig = updateSenorConfig(updateInfos.get("config"));
            // Update the rules to be observed in the test
            List<Rule> newRuleList = updateRuleInformation(updateInfos);

            testToUpdate.setConfig(newConfig);
            testToUpdate.setRules(newRuleList);
            // Update the information if the selected rules be triggered during the test or not
            testToUpdate.setTriggerRules(updateInfos.getBoolean("triggerRules"));


            testDetailsRepository.save(testToUpdate);

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Creates a list of the updated rules that should be observed within the test.
     *
     * @param updateInfos update/editUseNewData information for the rules and the trigger rules
     * @return List of updated rules
     */
    private List<Rule> updateRuleInformation(JSONObject updateInfos) throws JSONException {
        Pattern pattern = Pattern.compile("rules/(.*)$");
        JSONArray rules = (JSONArray) updateInfos.get("rules");
        List<Rule> newRules = new ArrayList<>();
        if (rules != null) {
            for (int i = 0; i < rules.length(); i++) {
                Matcher m = pattern.matcher(rules.getString(i));
                if (m.find()) {
                    newRules.add(ruleRepository.findById(m.group(1)).get());
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
            test = testDetailsRepository.findById(m.group(1)).get();
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
    public ResponseEntity deleteReport(String testId, Object path) {
        String fileName = String.valueOf(path);
        ResponseEntity response;




        TestDetails testDetails = testDetailsRepository.findById(testId).get();


        //Path pathTestReport = Paths.get(testDetails.getPathPDF()+ ""fileName);
        try {
            if (testDetails.isPdfExists()) {
                File dir = new File(testDetails.getPathPDF());
                FileFilter fileFilter = new WildcardFileFilter(fileName);
                File[] files = dir.listFiles(fileFilter);
                for (final File file : files) {
                    if (!file.delete()) {
                        System.err.println("Can't remove " + file.getAbsolutePath());
                    }
                }


                //  Files.delete(pathTestReport);
                response = new ResponseEntity<>("Test report successfully deleted", HttpStatus.OK);

            } else {
                response = new ResponseEntity<>("No available Test report for this Test.", HttpStatus.NOT_FOUND);
            }
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            response = new ResponseEntity<>("Error during deletion.",HttpStatus.CONFLICT);
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
        TestDetails testDetails = testDetailsRepository.findById(testId).get();
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
        Device testDevice = null;


        try {
            // Check if device with this name is already registered
            testDevice = getTestDevice();

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
            // errors = new BeanPropertyBindingResult(testDevice, "device");
            //deviceCreateValidator.validate(device, errors); TODO

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
        Device testDevice;


        try {
            Actuator testingActuator = actuatorRepository.findByName(ACTUATOR_NAME).get();
            Operator testActuatorAdapter = operatorRepository.findByName(ACTUATOR_NAME).get();
            testDevice = getTestDevice();

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
                        testingActuator.setOperator(testActuatorAdapter);
                        testingActuator.setComponentType("Buzzer");

                        //Validate device
                        errors = new BeanPropertyBindingResult(testingActuator, "component");
                        // actuatorValidator.validate(testingActuator, errors);

                        actuatorRepository.insert(testingActuator);

                        responseEntity = new ResponseEntity("Testing Actuator successfully created.", HttpStatus.CREATED);

                    }
                }
            }


        } catch (Exception e) {
            responseEntity = new ResponseEntity("Error during creation of the Actuator.", HttpStatus.CONFLICT);
        }
        return responseEntity;
    }

    /**
     * Checks if the Testing Device is already registered and turn back this device or null.
     *
     * @return the test Device if existing
     */
    private Device getTestDevice() {
        Device testDevice = null;

        // List of all registered Devices
        List<Device> testDeviceList = deviceRepository.findAll();

        // Go through the List of devices and check if testing Device is available
        Iterator iterator = testDeviceList.listIterator();
        while (iterator.hasNext()) {
            Device tempDevice = (Device) iterator.next();
            if (tempDevice.getName().equals(TEST_DEVICE)) {
                testDevice = tempDevice;
                break;
            }
        }
        return testDevice;
    }

    /**
     * Checks if given Sensor Simulator is already registered and turn back this sensor or null.
     *
     * @return the sensor simulator if existing
     */
    private Sensor getSensorSimulator(String sensorName) {
        Sensor sensorSimulator = null;

        // List of all registered Sensors
        List<Sensor> sensorList = sensorRepository.findAll();

        // Go through the List of sensors and check if specific sensor is available
        Iterator iterator = sensorList.listIterator();
        while (iterator.hasNext()) {
            Sensor tempSensor = (Sensor) iterator.next();
            if (tempSensor.getName().equals(sensorName)) {
                sensorSimulator = tempSensor;
            }

        }

        return sensorSimulator;
    }


    /**
     * Registers the wished sensor simulator if the corresponding adapter is already registered
     *
     * @param sensorName Name of the sensor simulator to be registered
     * @return ResponseEntity if the registration was successful or not
     */
    public ResponseEntity<String> registerSensorSimulator(String sensorName) {
        ResponseEntity<String> responseEntity;

        Operator sensorAdapter = operatorRepository.findByName(sensorName).get();
        Device testingDevice = getTestDevice();
        Sensor sensorSimulator = getSensorSimulator(sensorName);

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

                sensorSimulator.setOperator(sensorAdapter);
                sensorSimulator.setDevice(testingDevice);

                //Validate device
                //errors = new BeanPropertyBindingResult(sensorSimulator, "component");
                // sensorValidator.validate(sensorSimulator, errors);

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
            Sensor sensorX = getSensorSimulator(dimX);
            Sensor sensorY = getSensorSimulator(dimY);
            Sensor sensorZ = getSensorSimulator(dimZ);

            if (sensorX != null && sensorY != null && sensorZ != null) {
                registered = true;
            }
        } else {
            if (getSensorSimulator(sensor) != null) {
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