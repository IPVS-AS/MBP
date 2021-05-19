package de.ipvs.as.mbp.service.testing.rerun;


import de.ipvs.as.mbp.domain.component.Sensor;
import de.ipvs.as.mbp.domain.device.Device;
import de.ipvs.as.mbp.domain.operator.Operator;
import de.ipvs.as.mbp.domain.operator.parameters.ParameterInstance;
import de.ipvs.as.mbp.domain.rules.Rule;
import de.ipvs.as.mbp.domain.rules.RuleTrigger;
import de.ipvs.as.mbp.domain.testing.TestDetails;
import de.ipvs.as.mbp.domain.testing.TestReport;
import de.ipvs.as.mbp.repository.*;
import de.ipvs.as.mbp.service.cep.trigger.CEPTriggerService;
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
    private TestReportRepository testReportRepository;

    @Autowired
    private TestRerunOperatorService rerunOperatorService;

    @Autowired
    private SensorRepository sensorRepository;

    @Autowired
    private CEPTriggerService triggerService;

    @Autowired
    private PropertiesService propertiesService;

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
   /* public List<List<ParameterInstance>> editUseNewData(String testId, boolean useNewData) {
        TestDetails testDetails = testDetailsRepository.findById(testId).get();
        List<List<ParameterInstance>> configList = testDetails.getConfig();


        if (!useNewData) {
            testDetails.setUseNewData(false);

        } else {
            testDetails.setUseNewData(true);
        }

        // add or deletes rerun components
        editRerunComponents(testDetails);

        // Change value for the configuration of every sensor simulator of the test
        for (List<ParameterInstance> config : configList) {
            for (ParameterInstance parameterInstance : config) {
                if (parameterInstance.getName().equals("useNewData")) {
                    parameterInstance.setValue(useNewData);
                }
            }
        }

        testDetails.setConfig(configList);
        testDetails.setUseNewData(useNewData);

        // save the changes in the database
        testDetailsRepository.save(testDetails);
        return configList;
    }*/


    /**
     * Adds rerun Components if the test should be repeated. Otherwise they will be deleted.
     *
     * @param test to be repeated
     */
    /*public void editRerunComponents(TestDetails test) {
        if (!test.isUseNewData()) {
            // add components needed for rerun the test
            addRerunComponents(test);
        } else {
            // delete components not needed outside a test rerun
            deleteRerunComponents(test);
        }
    }
*/

    /**
     * Adds operators, sensors and rules for repeating the test.
     *
     * @param testReportId to be repeated
     */
    public void addRerunComponents(String testReportId, TestDetails test) {
        TestReport testReport = testReportRepository.findById(testReportId).get();

        // add rerun operator if not existing
        addRerunOperators();


        // add rerun sensor for every sensor with a configuration in the test
        for (List<ParameterInstance> config : testReport.getConfig()) {
            for (ParameterInstance parameterInstance : config) {
                if (parameterInstance.getName().equals(CONFIG_SENSOR_NAME_KEY)) {
                    addRerunSensors(parameterInstance.getValue().toString(), testReport);

                }
            }
        }
        // add rerun Rules for the test
        addRerunRule(test);
    }

    /**
     * Deletes operators, sensors and rules when test should be not repeated.
     *
     * @param test to be repeated
     */
    public void deleteRerunComponents(TestDetails test) {
        //Delete rerun rules
        deleteRerunRules(test);

        // Delete the Reuse Adapters and Sensors for each real sensor if the data should not be reused
        for (List<ParameterInstance> config : test.getConfig()) {
            for (ParameterInstance parameterInstance : config) {
                if (parameterInstance.getName().equals(CONFIG_SENSOR_NAME_KEY)) {
                    if (!SIMULATOR_LIST.contains(parameterInstance.getValue().toString())) {
                        // Delete Reuse Operator
                        String reuseName = RERUN_IDENTIFIER + parameterInstance.getValue();
                        Sensor sensorReuse = sensorRepository.findByName(reuseName).get();
                        if (sensorReuse != null) {
                            sensorRepository.delete(sensorReuse);
                            test.getSensor().remove(sensorReuse);
                            testDetailsRepository.save(test);

                        }
                    }
                }
            }
        }
    }


    /**
     * Generates new Sensors for the test rerun with real Sensors.
     *
     * @param realSensorName name of the real sensor
     */
    public void addRerunSensors(String realSensorName, TestReport testReport) {
        Sensor newSensor = new Sensor();
        newSensor.setOwner(null);

        Operator rerunOperator = operatorRepository.findByName(RERUN_OPERATOR).get();
        Device testingDevice = deviceRepository.findByName(TESTING_DEVICE).get();
        String newSensorName = RERUN_IDENTIFIER + realSensorName;

        try {
            if (!sensorRepository.findByName(newSensorName).isPresent()) {
                if (rerunOperator != null && testingDevice != null) {
                    // Set all relevant information
                    newSensor.setName(newSensorName);
                    newSensor.setComponentType("Computer");
                    newSensor.setDevice(testingDevice);
                    newSensor.setOperator(rerunOperator);

                    //Insert new sensor into repository
                    sensorRepository.insert(newSensor);
                    sensorRepository.save(newSensor);
                    triggerService.registerComponentEventType(newSensor);
                }
            }
            if (!testReport.getSensor().contains(sensorRepository.findByName(newSensorName))) {
                // add sensor to the test sensor list
                addSensor(newSensorName, testReport);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Adds and saves a sensor to the sensor list of the test
     *
     * @param sensorName to be saved/added
     * @param testReport in which the sensor should be added to
     */
    public void addSensor(String sensorName, TestReport testReport) {
        List<Sensor> sensors = testReport.getSensor();
        sensors.add(sensorRepository.findByName(sensorName).get());
        testReport.setSensor(sensors);
        testReportRepository.save(testReport);
    }


    /**
     * Creates the same rules as they are contained in the test, but in the rule trigger the sensor is adapted to the
     * rerun sensor. This allows results to be analyzed in rerun mode.
     *
     * @param test to be repeated
     */
    public void addRerunRule(TestDetails test) {
        // Get a list of every rule belonging to the IoT-Application
        List<Rule> applicationRules = testAnalyzer.getCorrespondingRules(test);
        boolean notRegister = false;

        for (Rule rule : applicationRules) {
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
                                Sensor rerunSensor = sensorRepository.findByName(RERUN_IDENTIFIER + realSensor.getName()).get();
                                if (rerunSensor != null) {
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
        }
    }


    /**
     * Delete the rerun rules not needed in a test that should not be repeated.
     *
     * @param test for which the rerun rules should be deleted
     */
    public void deleteRerunRules(TestDetails test) {
        List<Rule> testRules = testAnalyzer.getCorrespondingRules(test);

        for (Rule rule : testRules) {
            if (rule.getName().contains(RERUN_IDENTIFIER)) {
                if (rule.getTrigger() != null) {
                    ruleTriggerRepository.delete(rule.getTrigger());
                }
                ruleRepository.delete(rule);
            }
        }
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
                rerunOperatorService.addRerunOperators();
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

