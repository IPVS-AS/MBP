package org.citopt.connde.service.testing;

import org.citopt.connde.domain.adapter.Adapter;
import org.citopt.connde.domain.adapter.parameters.ParameterInstance;
import org.citopt.connde.domain.component.Sensor;
import org.citopt.connde.domain.component.SensorValidator;
import org.citopt.connde.domain.device.Device;
import org.citopt.connde.domain.rules.Rule;
import org.citopt.connde.domain.rules.RuleTrigger;
import org.citopt.connde.domain.testing.TestDetails;
import org.citopt.connde.repository.*;
import org.citopt.connde.web.rest.event_handler.SensorEventHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TestRerunService {
    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private SensorRepository sensorRepository;

    @Autowired
    private AdapterRepository adapterRepository;

    @Autowired
    private TestDetailsRepository testDetailsRepository;

    @Autowired
    private TestAnalyzer testAnalyzer;

    @Autowired
    private RuleTriggerRepository ruleTriggerRepository;


    @Autowired
    private RuleRepository ruleRepository;


    @Autowired
    private TestRerunOperatorService rerunOperatorService;


    @Autowired
    private SensorValidator sensorValidator;

    @Autowired
    private SensorEventHandler sensorEventHandler;


    String[] sensorSim = {"TestingTemperaturSensor", "TestingTemperaturSensorPl", "TestingFeuchtigkeitsSensor", "TestingFeuchtigkeitsSensorPl", "TestingGPSSensorPl", "TestingGPSSensor", "TestingBeschleunigungsSensor", "TestingBeschleunigungsSensorPl"};
    List<String> sensorSimulators = Arrays.asList(sensorSim);



    /**
     * Update the UseNewData field of the test and edits the rerun components.
     *
     * @param testId of the test to be
     * @param useNewData information if a test should be repeated
     * @return the updated configuration list
     */
    public List<List<ParameterInstance>> editUseNewData(String testId, String useNewData) {
        TestDetails testDetails = testDetailsRepository.findById(testId);
        List<List<ParameterInstance>> configList = testDetails.getConfig();


        if (!Boolean.parseBoolean(useNewData)) {
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
                    parameterInstance.setValue(Boolean.valueOf(useNewData));
                }
            }
        }

        testDetails.setConfig(configList);
        testDetails.setUseNewData(Boolean.parseBoolean(useNewData));

        // save the changes in the database
        testDetailsRepository.save(testDetails);
        return configList;
    }



    /**
     * Adds rerun Components if the test should be repeated. Otherwise they will be deleted.
     *
     * @param test to be repeated
     */
    public void editRerunComponents(TestDetails test) {
        if (!test.isUseNewData()) {
            // add components needed for rerun the test
            addRerunComponents(test);
        } else {
            // delete components not needed outside a test rerun
            deleteRerunComponents(test);
        }
    }

    /**
     * Adds operators, sensors and rules for repeating the test.
     *
     * @param test to be repeated
     */
    public void addRerunComponents(TestDetails test) {
        // add rerun operator if nt existing
        addRerunOperators();

        // add rerun sensor for every sensor with a configuration in the test
        for (List<ParameterInstance> config : test.getConfig()) {
            for (ParameterInstance parameterInstance : config) {
                if (parameterInstance.getName().equals("ConfigName")) {
                    if (!sensorSimulators.contains(parameterInstance.getValue().toString())) {
                        addRerunSensors(parameterInstance.getValue().toString(), test);
                    }
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
                if (parameterInstance.getName().equals("ConfigName")) {
                    if (!sensorSimulators.contains(parameterInstance.getValue().toString())) {
                        // Delete Reuse Operator
                        String reuseName = "RERUN_" + parameterInstance.getValue();
                        Sensor sensorReuse = sensorRepository.findByName(reuseName);
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
    public void addRerunSensors(String realSensorName, TestDetails testDetails) {
        Sensor newSensor = new Sensor();
        newSensor.setOwner(null);

        Adapter rerunOperator = adapterRepository.findByName("RERUN_OPERATOR");
        Device testingDevice = deviceRepository.findByName("TestingDevice");
        String newSensorName = "RERUN_" + realSensorName;

        try {
            if (sensorRepository.findByName(newSensorName) == null) {
                if (rerunOperator != null && testingDevice != null) {
                    // Set all relevant information
                    newSensor.setName(newSensorName);
                    newSensor.setComponentType("Computer");
                    newSensor.setDevice(testingDevice);
                    newSensor.setAdapter(rerunOperator);

                    //Insert new sensor into repository
                    sensorRepository.insert(newSensor);
                    //Validation errors
                    Errors errors = new BeanPropertyBindingResult(newSensor, "component");
                    sensorValidator.validate(newSensor, errors);
                    sensorRepository.save(newSensor);
                    sensorEventHandler.afterSensorCreate(newSensor);

                }
            }
            if (!testDetails.getSensor().contains(sensorRepository.findByName(newSensorName))) {
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
     * @param sensorName to be saved/added
     * @param test       in which the sensor should be added to
     */
    public void addSensor(String sensorName, TestDetails test) {
        List<Sensor> sensors = test.getSensor();
        sensors.add(sensorRepository.findByName(sensorName));
        test.setSensor(sensors);
        testDetailsRepository.save(test);
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
            if (ruleRepository.findByName("RERUN_" + rule.getName()) == null) {
                // create new rule
                Rule rerunRule = new Rule();
                rerunRule.setName("RERUN_" + rule.getName());
                rerunRule.setOwner(null);
                rerunRule.setActions(rule.getActions());

                // create/adjust trigger querey
                if (ruleTriggerRepository.findByName("RERUN_" + rule.getTrigger().getName()) == null) {
                    // create new trigger
                    RuleTrigger newTrigger = new RuleTrigger();
                    newTrigger.setDescription(rule.getTrigger().getDescription());
                    newTrigger.setName("RERUN_" + rule.getTrigger().getName());

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
                                Sensor rerunSensor = sensorRepository.findByName("RERUN_" + realSensor.getName());
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
                    rerunRule.setTrigger(ruleTriggerRepository.findByName("RERUN_" + rule.getTrigger().getName()));
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
            if (rule.getName().contains("RERUN_")) {
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
            if (adapterRepository.findByName("RERUN_OPERATOR") == null) {
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
