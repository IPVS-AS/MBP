package de.ipvs.as.mbp.service.testing.rerun;


import de.ipvs.as.mbp.domain.component.Sensor;
import de.ipvs.as.mbp.domain.device.Device;
import de.ipvs.as.mbp.domain.operator.Operator;
import de.ipvs.as.mbp.domain.operator.parameters.ParameterInstance;
import de.ipvs.as.mbp.domain.rules.Rule;
import de.ipvs.as.mbp.domain.rules.RuleTrigger;
import de.ipvs.as.mbp.domain.testing.TestDetails;
import de.ipvs.as.mbp.repository.*;
import de.ipvs.as.mbp.service.cep.trigger.CEPTriggerService;
import de.ipvs.as.mbp.service.settings.DefaultOperatorService;
import de.ipvs.as.mbp.service.testing.PropertiesService;
import de.ipvs.as.mbp.service.testing.analyzer.TestAnalyzer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TestRerunService {
    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private OperatorRepository operatorRepository;

    @Autowired
    private TestDetailsRepository testDetailsRepository;

    @Autowired
    private TestAnalyzer testAnalyzer;

    @Autowired
    private RuleTriggerRepository ruleTriggerRepository;

    @Autowired
    private RuleRepository ruleRepository;

    @Autowired
    private DefaultOperatorService defaultOperatorService;

    @Autowired
    private SensorRepository sensorRepository;

    @Autowired
    private CEPTriggerService triggerService;

    @Autowired
    private PropertiesService propertiesService;

    @Autowired
    private List<String> defaultRerunOperatorWhitelist;

    //To resolve ${} in @Value
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Value("#{'${testingTool.sensorSimulators}'.split(',')}")
    List<String> SIMULATOR_LIST;

    private final String RERUN_IDENTIFIER;
    private final String RERUN_OPERATOR;
    private final String TESTING_DEVICE;
    private final String CONFIG_SENSOR_NAME_KEY;


    public TestRerunService() throws IOException {
        propertiesService = new PropertiesService();
        RERUN_IDENTIFIER = propertiesService.getPropertiesString("testingTool.RerunIdentifier");
        RERUN_OPERATOR = propertiesService.getPropertiesString("testingTool.rerunOperator");
        TESTING_DEVICE = propertiesService.getPropertiesString("testingTool.testDeviceName");
        CONFIG_SENSOR_NAME_KEY = propertiesService.getPropertiesString("testingTool.ConfigSensorNameKey");
    }


    /**
     * Update the UseNewData field of the test and edits the rerun components.
     *
     * @param testId     of the test to be
     * @param useNewData information if a test should be repeated
     * @return the updated configuration list
     */
    public ResponseEntity<List<List<ParameterInstance>>> editUseNewData(String testId, boolean useNewData) {
        Optional<TestDetails> testDetailsOptional = testDetailsRepository.findById(testId);

        if (testDetailsOptional.isPresent()) {
            TestDetails testDetails = testDetailsOptional.get();
            List<List<ParameterInstance>> configList = testDetails.getConfig();

            testDetails.setUseNewData(useNewData);

            // add or deletes rerun components
            editRerunComponents(useNewData, testDetails);

            // Change value for the configuration of every sensor simulator of the test
            for (List<ParameterInstance> config : configList) {
                for (ParameterInstance parameterInstance : config) {
                    if (parameterInstance.getName().equals("useNewData")) {
                        parameterInstance.setValue(useNewData);
                    }
                }
            }

            testDetails.setConfig(configList);

            // save the changes in the database
            testDetailsRepository.save(testDetails);

            return new ResponseEntity<>(configList, HttpStatus.OK);
        }


        return new ResponseEntity<>(null, HttpStatus.OK);
    }


    /**
     * Adds rerun Components if the test should be repeated. Otherwise they will be deleted.
     *
     * @param test to be repeated
     */
    public void editRerunComponents(boolean useNewData, TestDetails test) {
        if (useNewData) {  // delete components not needed outside a test rerun
            removeRerunComponents(test);

        } else {
            // add components needed for rerun the test
            addRerunComponents(test);
        }
    }


    /**
     * Adds operators, sensors and rules for repeating the test.
     */
    public void addRerunComponents(TestDetails test) {

        // add rerun operator if not existing
        addRerunOperators();

        // add rerun sensor for every sensor with a configuration in the test
        for (List<ParameterInstance> config : test.getConfig()) {
            for (ParameterInstance parameterInstance : config) {
                if (parameterInstance.getName().equals(CONFIG_SENSOR_NAME_KEY)) {
                    addRerunSensors(parameterInstance.getValue().toString(), test);

                }
            }
        }

        // add rerun Rules for the test
        addRerunRule(test);
    }

    /**
     * Delete the rerun components registered for the specific test
     *
     * @param testDetails test for which the rerun components should be deleted
     */
    public void deleteRerunComponents(TestDetails testDetails) {
        deleteRerunRules(testDetails);
        deleteRerunSensors(testDetails);
    }


    /**
     * Removes sensors and rules from the test when it shouldn't be repeated.
     *
     * @param testDetails to be repeated
     */
    public void removeRerunComponents(TestDetails testDetails) {
        TestDetails updatedTest = removeRerunRule(testDetails);
        removeRerunSensors(updatedTest);

    }

    /**
     * Remove the rerun sensors of a specific test from the configuration
     *
     * @param testDetails test from which the rerun sensors should be removed
     */
    public void removeRerunSensors(TestDetails testDetails) {
        List<Sensor> newSensorList = testDetails.getSensor();

        newSensorList.removeIf(sensor -> sensor.getName().contains(RERUN_IDENTIFIER));

        testDetails.setSensor(newSensorList);
        testDetailsRepository.save(testDetails);
    }

    /**
     *  Delete the rerun sensors of a specific test, if they are not used in any other test.
     *  
     * @param testDetails from which the rerun sensors should be deleted.
     */
    public void deleteRerunSensors(TestDetails testDetails) {
        for(Sensor sensor: testDetails.getSensor()){
            if(sensor.getName().contains(RERUN_IDENTIFIER)){
                if(!sensorExistsInMultipleTest(sensor.getId())){
                    sensorRepository.delete(sensor);
                }
            }
        }
    }


    /**
     * Generates new Sensors for the test rerun with real Sensors.
     *
     * @param realSensorName name of the real sensor
     */
    public void addRerunSensors(String realSensorName, TestDetails testDetails) {
        Sensor newSensor = new Sensor();
        newSensor.setOwner(null);

        Optional<Operator> rerunOperatorOptional = operatorRepository.findByName(RERUN_OPERATOR);
        Optional<Device> testingDeviceOptional = deviceRepository.findByName(TESTING_DEVICE);
        String newSensorName = RERUN_IDENTIFIER + realSensorName;

        try {
            if (!sensorRepository.findByName(newSensorName).isPresent() && rerunOperatorOptional.isPresent() && testingDeviceOptional.isPresent()) {

                // Set all relevant information
                newSensor.setName(newSensorName);
                newSensor.setComponentType("Computer");
                newSensor.setDevice(testingDeviceOptional.get());
                newSensor.setOperator(rerunOperatorOptional.get());

                //Insert new sensor into repository
                sensorRepository.insert(newSensor);
                sensorRepository.save(newSensor);
                triggerService.registerComponentEventType(newSensor);

            }
            if (!testDetails.getSensor().contains(sensorRepository.findByName(newSensorName).get())) {
                // add sensor to the test sensor list
                addSensor(newSensorName, testDetails);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Adds and saves a sensor to the sensor list of the test
     *
     * @param sensorName  to be saved/added
     * @param testDetails in which the sensor should be added to
     */
    public void addSensor(String sensorName, TestDetails testDetails) {
        List<Sensor> sensors = testDetails.getSensor();
        if (sensorRepository.findByName(sensorName).isPresent()) {
            Sensor rerunSensor = sensorRepository.findByName(sensorName).get();
            if (!sensors.contains(rerunSensor)) {
                sensors.add(rerunSensor);
                testDetails.setSensor(sensors);
                testDetailsRepository.save(testDetails);
            }
        }
    }


    /**
     * Creates the same rules as they are contained in the test, but in the rule trigger the sensor is adapted to the
     * rerun sensor. This allows results to be analyzed in rerun mode.
     *
     * @param test to be repeated
     */
    public void addRerunRule(TestDetails test) {
        // Get a list of every rule belonging to the IoT-Application
        List<Rule> applicationRules = testAnalyzer.getCorrespondingRules(test.getRules(), test.getSensor());
        List<Rule> testRules = test.getRules();
        boolean notRegister = false;

        for (Rule rule : applicationRules) {
            if (!rule.getName().contains(RERUN_IDENTIFIER)) {
                if (!ruleRepository.findByName(RERUN_IDENTIFIER + rule.getName()).isPresent()) {
                    // create new rule
                    Rule rerunRule = new Rule();
                    rerunRule.setName(RERUN_IDENTIFIER + rule.getName());
                    rerunRule.setOwner(null);
                    rerunRule.setActions(rule.getActions());

                    // create/adjust trigger querey
                    if (!ruleTriggerRepository.findByName(RERUN_IDENTIFIER + rule.getTrigger().getName()).isPresent()) {
                        // create new trigger
                        RuleTrigger newTrigger = new RuleTrigger();
                        newTrigger.setDescription(rule.getTrigger().getDescription());
                        newTrigger.setName(RERUN_IDENTIFIER + rule.getTrigger().getName());

                        // adjust trigger query of the sensor of the test
                        String triggerQuery = rule.getTrigger().getQuery();
                        // Regex to get out the sensor ID
                        Pattern pattern = Pattern.compile("(?<=sensor_)(.*)(?=\\)])");
                        Matcher matcher = pattern.matcher(triggerQuery);
                        while (matcher.find()) {
                            String sensorID = matcher.group();
                            List<Sensor> realSensors = sensorRepository.findAll();
                            for (Sensor realSensor : realSensors) {
                                if (realSensor.getId().equals(sensorID)) {
                                    if (sensorRepository.findByName(RERUN_IDENTIFIER + realSensor.getName()).isPresent()) {
                                        Sensor rerunSensor = sensorRepository.findByName(RERUN_IDENTIFIER + realSensor.getName()).get();
                                        // replace the sensor id in the trigger query with the rerun sensor id
                                        triggerQuery = triggerQuery.replace(realSensor.getId(), rerunSensor.getId());
                                        newTrigger.setQuery(triggerQuery);
                                        ruleTriggerRepository.insert(newTrigger);
                                    } else {
                                        notRegister = true;
                                        break;
                                    }


                                }
                            }
                            if (notRegister) {
                                break;
                            } else {
                                // set the created trigger of the rerun rule
                                rerunRule.setTrigger(newTrigger);
                            }
                        }
                    } else {
                        // set existing trigger of the rerun rule
                        rerunRule.setTrigger(ruleTriggerRepository.findByName(RERUN_IDENTIFIER + rule.getTrigger().getName()).get());
                    }
                    if (!notRegister) {
                        ruleRepository.insert(rerunRule);
                    }


                }
                if (ruleRepository.findByName(RERUN_IDENTIFIER + rule.getName()).isPresent()) {
                    if (!testRules.contains(ruleRepository.findByName(RERUN_IDENTIFIER + rule.getName()).get())) {

                        testRules.add(ruleRepository.findByName(RERUN_IDENTIFIER + rule.getName()).get());
                    }


                }
            }


        }
        test.setRules(testRules);
        testDetailsRepository.save(test);
    }

    public TestDetails removeRerunRule(TestDetails testDetails) {
        List<Rule> testRules = testAnalyzer.getCorrespondingRules(testDetails.getRules(), testDetails.getSensor());

        for (Rule rule : testRules) {
            if (rule.getName().contains(RERUN_IDENTIFIER)) {
                testDetails.getRules().remove(rule);
            }
        }
        testDetailsRepository.save(testDetails);
        return testDetails;
    }

    /**
     * Delete the rerun rules not needed in a test that should not be repeated.
     *
     * @param test for which the rerun rules should be deleted
     */
    public void deleteRerunRules(TestDetails test) {
        List<Rule> testRules = testAnalyzer.getCorrespondingRules(test.getRules(), test.getSensor());


        for (Rule rule : testRules) {
            if (rule.getName().contains(RERUN_IDENTIFIER)) {
               if(!ruleExistsInMultipleTests(rule.getId())){
                   if (rule.getTrigger() != null) {
                       ruleTriggerRepository.delete(rule.getTrigger());
                   }
                   ruleRepository.delete(rule);
               }

            }
        }
    }

    /**
     * Checks if the rule exists in more than one test.
     *
     * @param ruleId rule for which the existence in the tests should be checked.
     * @return true if rule exists in more then one test
     */
    private Boolean ruleExistsInMultipleTests(String ruleId){
        List<TestDetails> existingInTests = testDetailsRepository.findAllByRulesId(ruleId);
        return existingInTests.size() > 1;
    }

    /**
     * Checks if the sensor exists in more than one test.
     *
     * @param sensorId sensor for which the existence in the tests should be checked.
     * @return true if sensor exists in more then one test
     */
    private Boolean sensorExistsInMultipleTest(String sensorId){
        List<TestDetails> existingInTest = testDetailsRepository.findAllBySensorId(sensorId);
        return existingInTest.size() > 1;
    }

    /**
     * Called when the client wants to load default operators and make them available for usage
     * in actuators and sensors by all users.
     *
     * @return An action response containing the result of the request
     */
    public ResponseEntity<String> addRerunOperators() {
        ResponseEntity<String> response;
        try {
            if (!operatorRepository.findByName(RERUN_OPERATOR).isPresent()) {
                //Call corresponding service function
                defaultOperatorService.addDefaultOperators(defaultRerunOperatorWhitelist);
                response = new ResponseEntity<>("Adapter successfully created", HttpStatus.OK);
            } else {
                response = new ResponseEntity<>("Adapter already exists.", HttpStatus.OK);
            }
        } catch (Exception e) {
            response = new ResponseEntity<>("Error during creation of the Adapter.", HttpStatus.INTERNAL_SERVER_ERROR);
        }


        return response;
    }
}

