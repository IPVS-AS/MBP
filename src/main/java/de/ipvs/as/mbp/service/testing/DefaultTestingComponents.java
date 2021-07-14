package de.ipvs.as.mbp.service.testing;

import de.ipvs.as.mbp.domain.component.Actuator;
import de.ipvs.as.mbp.domain.component.ComponentCreateEventHandler;
import de.ipvs.as.mbp.domain.component.ComponentCreateValidator;
import de.ipvs.as.mbp.domain.component.Sensor;
import de.ipvs.as.mbp.domain.device.Device;
import de.ipvs.as.mbp.domain.device.DeviceCreateEventHandler;
import de.ipvs.as.mbp.domain.device.DeviceCreateValidator;
import de.ipvs.as.mbp.domain.operator.Operator;
import de.ipvs.as.mbp.domain.rules.RuleTrigger;
import de.ipvs.as.mbp.domain.testing.TestDetails;
import de.ipvs.as.mbp.domain.testing.TestReport;
import de.ipvs.as.mbp.repository.*;
import de.ipvs.as.mbp.repository.projection.ComponentExcerpt;
import de.ipvs.as.mbp.service.settings.DefaultOperatorService;
import de.ipvs.as.mbp.web.rest.helper.DeploymentWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
public class DefaultTestingComponents {

    @Autowired
    private final List<String> defaultTestComponentsWhiteList;

    @Autowired
    private final TestDetailsRepository testDetailsRepository;

    @Autowired
    private TestReportRepository testReportRepository;

    @Autowired
    private final OperatorRepository operatorRepository;

    @Autowired
    private final ActuatorRepository actuatorRepository;

    @Autowired
    private final DeviceRepository deviceRepository;

    @Autowired
    private final SensorRepository sensorRepository;

    @Autowired
    private final DeviceCreateValidator deviceCreateValidator;

    @Autowired
    private final PropertiesService propertiesService;

    @Autowired
    private final ComponentCreateValidator componentCreateValidator;

    @Autowired
    private final ComponentCreateEventHandler componentCreateEventHandler;

    @Autowired
    private final DeviceCreateEventHandler deviceCreateEventHandler;

    @Autowired
    private DeploymentWrapper deploymentWrapper;

    @Autowired
    private final DefaultOperatorService defaultOperatorService;


    @Autowired
    RuleTriggerRepository ruleTriggerRepository;


    private final String TEST_DEVICE;
    private final String TEST_DEVICE_IP;
    private final String TEST_DEVICE_USERNAME;
    private final String TEST_DEVICE_PASSWORD;
    private final String ACTUATOR_NAME;
    private final String TEST_PREFIX;

    private final List<String> SENSOR_SIMULATORS = Arrays.asList("TESTING_TemperatureSensor", "TESTING_TemperatureSensorPl", "TESTING_HumiditySensor", "TESTING_HumiditySensorPl");


    public DefaultTestingComponents(List<String> defaultTestComponentsWhiteList, TestReportRepository testReportRepository, OperatorRepository operatorRepository, DeviceRepository deviceRepository, DeviceCreateValidator deviceCreateValidator, ActuatorRepository actuatorRepository, ComponentCreateValidator componentCreateValidator, ComponentCreateEventHandler componentCreateEventHandler, DeviceCreateEventHandler deviceCreateEventHandler,
                                    SensorRepository sensorRepository, TestDetailsRepository testDetailsRepository, DefaultOperatorService defaultOperatorService, RuleTriggerRepository ruleTriggerRepository) throws IOException {
        this.testReportRepository = testReportRepository;
        // Get needed Strings out of the properties to create the testing components
        propertiesService = new PropertiesService();
        TEST_DEVICE = propertiesService.getPropertiesString("testingTool.testDeviceName");
        TEST_DEVICE_IP = propertiesService.getPropertiesString("testingTool.ipAddressTestDevice");
        TEST_DEVICE_USERNAME = propertiesService.getPropertiesString("testingTool.testDeviceUserName");
        TEST_DEVICE_PASSWORD = propertiesService.getPropertiesString("testingTool.testDevicePassword");
        ACTUATOR_NAME = propertiesService.getPropertiesString("testingTool.actuatorName");
        TEST_PREFIX = propertiesService.getPropertiesString("testingTool.testComponentIdentifier");


        this.defaultTestComponentsWhiteList = defaultTestComponentsWhiteList;
        this.operatorRepository = operatorRepository;
        this.actuatorRepository = actuatorRepository;
        this.deviceRepository = deviceRepository;
        this.sensorRepository = sensorRepository;
        this.deviceCreateValidator = deviceCreateValidator;
        this.componentCreateValidator = componentCreateValidator;
        this.componentCreateEventHandler = componentCreateEventHandler;
        this.deviceCreateEventHandler = deviceCreateEventHandler;
        this.ruleTriggerRepository = ruleTriggerRepository;
        this.testDetailsRepository = testDetailsRepository;
        this.testReportRepository = testReportRepository;
        this.defaultOperatorService = defaultOperatorService;


        registerDevice();
        defaultOperatorService.addDefaultOperators(defaultTestComponentsWhiteList);
        registerActuatorSimulator();
        registerAllSensorSimulators();

    }


    /**
     * Registers the wished sensor simulator if the corresponding adapter is already registered
     *
     * @param simulatorName name of the simulator to register
     */
    public void registerSensorSimulator(String simulatorName) {

        try {
            if (!sensorRepository.findByName(simulatorName).isPresent()) {
                Sensor sensorSimulator = new Sensor();

                // Enrich sensor with details
                sensorSimulator.setName(simulatorName);
                sensorSimulator.setOwner(null);
                sensorSimulator.setComponentType(componentType(simulatorName));
                if (operatorRepository.findByName(simulatorName).isPresent() && deviceRepository.findByName(TEST_DEVICE).isPresent()) {
                    sensorSimulator.setOperator(operatorRepository.findByName(simulatorName).get());
                    sensorSimulator.setDevice(deviceRepository.findByName(TEST_DEVICE).get());
                }

                // Validate, insert and create a new event handler for the new sensor simulator
                componentCreateValidator.validateCreatable(sensorSimulator);
                sensorRepository.insert(sensorSimulator);
                componentCreateEventHandler.onCreate(sensorSimulator);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Registers all sensor simulators if the corresponding adapter is already registered.
     */
    private void registerAllSensorSimulators() {
        for (String sensorName : SENSOR_SIMULATORS) {
            registerSensorSimulator(sensorName);
        }
    }


    /**
     * Returns the type of the sensor simulator that can be "Temperature" or "Humidity".
     *
     * @param simulatorName name of the senor simulator to be registered
     * @return type of sensor
     */
    public String componentType(String simulatorName) {
        if (simulatorName.contains("Temperature")) {
            return "Temperature";
        } else {
            return "Humidity";
        }
    }


    /**
     * Registers the testing device which is used for testing purposes.
     */
    private void registerDevice() {
        Device testDevice;

        try {
            // Check if device with this name is already registered
            if (!deviceRepository.existsByName(TEST_DEVICE)) {

                //Enrich device for details
                testDevice = new Device();
                testDevice.setName(TEST_DEVICE);
                testDevice.setComponentType("Computer");
                testDevice.setIpAddress(TEST_DEVICE_IP);
                testDevice.setDate(LocalDateTime.now().toString());
                testDevice.setUsername(TEST_DEVICE_USERNAME);
                testDevice.setPassword(TEST_DEVICE_PASSWORD);

                // Validate, insert and create a new event handler for the new testing device
                deviceCreateValidator.validateCreatable(testDevice);
                deviceRepository.save(testDevice);
                deviceCreateEventHandler.onCreate(testDevice);
            }

        } catch (Exception exception) {
            exception.printStackTrace();
        }

    }


    /**
     * Registers the Testing Actuator which is used for testing purposes and doesn't make any real actions.
     */
    public void registerActuatorSimulator() {
        Actuator testingActuator;

        try {
            // Check if Actuator is already existing
            if (!actuatorRepository.existsByName(ACTUATOR_NAME)) {
                // Check if the corresponding adapter is registered
                if (operatorRepository.existsByName(ACTUATOR_NAME)) {

                    //Enrich actuator for details
                    testingActuator = new Actuator();
                    testingActuator.setName(ACTUATOR_NAME);
                    testingActuator.setOwner(null);
                    testingActuator.setComponentType("Buzzer");
                    boolean deviceoptional = deviceRepository.findFirstByName(TEST_DEVICE).isPresent();
                    System.out.println(deviceoptional);
                    if (deviceRepository.findFirstByName(TEST_DEVICE).isPresent() && operatorRepository.findFirstByName(ACTUATOR_NAME).isPresent()) {
                        testingActuator.setDevice(deviceRepository.findFirstByName(TEST_DEVICE).get());
                        testingActuator.setOperator(operatorRepository.findFirstByName(ACTUATOR_NAME).get());
                    }

                    // Validate, insert and add event handler for the new actuator
                    componentCreateValidator.validateCreatable(testingActuator);
                    actuatorRepository.insert(testingActuator);
                    componentCreateEventHandler.onCreate(testingActuator);

                }
            }
        } catch (Exception exception) {

            exception.printStackTrace();

        }
    }


    /**
     * Replaces the installed sensor simulator with a new one, replaces the old test device
     * and replaces the sensor simulators in the tests and rules that uses this simulator.
     *
     * @return if the replacement was successful or not.
     */
    public ResponseEntity<Void> replaceSensorSimulators() {
        try {

            // Replace each sensor simulator and their occurrences in the tests and rules one after the other.
            for (String sensorName : SENSOR_SIMULATORS) {
                List<TestDetails> affectedTestDetails = null;
                List<TestReport> affectedtTestReports = null;
                String oldSensorId = null;

                // Get a list of the tests which uses the specific sensor simulator to be replaced
                if (sensorRepository.findByName(sensorName).isPresent()) {
                    oldSensorId = sensorRepository.findByName(sensorName).get().getId();
                    affectedTestDetails = testDetailsRepository.findAllBySensorId(oldSensorId);
                    affectedtTestReports = testReportRepository.findAllBySensorId(oldSensorId);
                }

                // Delete the sensor simulator
                if (sensorRepository.existsByName(sensorName)) {
                    sensorRepository.delete(sensorRepository.findByName(sensorName).get());
                }

                // Install the sensor simulator
                registerSensorSimulator(sensorName);

                // Replace the reinstalled sensor simulator in the affected tests
                if (affectedTestDetails != null && affectedTestDetails.size() >= 1) {
                    replaceSimulatorInTest(affectedTestDetails);
                    replaceSimulatorInReport(affectedtTestReports);
                }

                // Replace the reinstalled sensor simulator in the affected rules.
                replaceSimulatorInRule(oldSensorId, sensorName);
            }

        } catch (Exception exception) {
            exception.printStackTrace();
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok().build();
    }


    /**
     * Replaces the reinstalled sensor simulator in the rules in which the sensor simulator is used in the conditions.
     *
     * @param oldSensorId Old Id of the reinstalled sensor, to find the rule conditions which still uses the old sensor simulator
     * @param sensorName  Name of the reinstalled sensor
     */
    private void replaceSimulatorInRule(String oldSensorId, String sensorName) {
        List<RuleTrigger> triggerList = ruleTriggerRepository.findAll();
        for (RuleTrigger ruleTrigger : triggerList) {
            if (ruleTrigger.getQuery().contains(oldSensorId)) {

                // adjust trigger query of the sensor of the test
                String triggerQuery = ruleTrigger.getQuery();

                // Regex to get out the sensor ID
                Pattern pattern = Pattern.compile("(?<=sensor_)([0-9a-zA-Z]*)");
                Matcher matcher = pattern.matcher(triggerQuery);
                while (matcher.find()) {
                    String sensorID = matcher.group();
                    if (sensorID.contains(oldSensorId)) {
                        if (sensorRepository.findByName(sensorName).isPresent()) {
                            Sensor updatedSensor = sensorRepository.findByName(sensorName).get();
                            // replace the sensor id in the trigger query with the rerun sensor id
                            triggerQuery = triggerQuery.replace(oldSensorId, updatedSensor.getId());
                            ruleTrigger.setQuery(triggerQuery);
                            ruleTriggerRepository.save(ruleTrigger);
                        }

                    }
                }
            }
        }
    }

    /**
     * Replaces the reinstalled sensor simulator in the test reports in which the sensor simulator is used.
     *
     * @param affectedReports List of the test reports affected by the sensor simulator reinstall
     */
    public void replaceSimulatorInReport(List<TestReport> affectedReports) {
        for (TestReport report : affectedReports) {
            // Save the modified test
            report.setSensor(placedInList(report.getSensor()));
            testReportRepository.save(report);
        }
    }


    /**
     * Replaces the reinstalled sensor simulator in the tests in which the sensor simulator is used.
     *
     * @param affectedTests List of the tests affected by the sensor simulator reinstall
     */
    public void replaceSimulatorInTest(List<TestDetails> affectedTests) {
        for (TestDetails test : affectedTests) {
            // Save the modified test
            test.setSensor(placedInList(test.getSensor()));
            testDetailsRepository.save(test);
        }
    }

    /**
     * Replace the reinstalled sensor simulator in the sensor list.
     *
     * @param sensorList which should be updated
     * @return updated sensor list with the reinstalled sensors
     */
    private List<Sensor> placedInList(List<Sensor> sensorList) {
        for (Sensor sensor : sensorList) {
            if (sensorRepository.findByName(sensor.getName()).isPresent()) {
                // Get the index of the reinstalled sensor and replace it with the new one
                int index = sensorList.indexOf(sensor);
                Sensor replacedSensor = sensorRepository.findByName(sensor.getName()).get();
                sensorList.set(index, replacedSensor);
            }
        }
        return sensorList;
    }

    /**
     * Replaces the test device used for test purposes and replaces the device in the rerun sensors.
     *
     * @return if the replacement was successful or not.
     */
    public ResponseEntity<Void> replaceTestDevice() {
        try {
            String oldDeviceId = null;
            // Delete the testing-device if exists
            if (deviceRepository.findByName(TEST_DEVICE).isPresent()) {
                oldDeviceId = deviceRepository.findByName(TEST_DEVICE).get().getId();
                deviceRepository.delete(deviceRepository.findByName(TEST_DEVICE).get());
            }
            registerDevice();
            replaceDeviceInRerun(oldDeviceId);
            // Add the new testing-device
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Replaces the reinstalled test device in the rerun sensors.
     *
     * @param oldDeviceId old device id to find the rerun sensors using the replaced test device
     */
    public void replaceDeviceInRerun(String oldDeviceId) {
        List<ComponentExcerpt> sensorList = sensorRepository.findAllByDeviceId(oldDeviceId);
        for (ComponentExcerpt senor : sensorList) {
            if (senor.getName().contains("RERUN_")) {
                if (sensorRepository.findByName(senor.getName()).isPresent() && deviceRepository.findByName(TEST_DEVICE).isPresent()) {
                    Sensor sensorUpdate = sensorRepository.findByName(senor.getName()).get();
                    sensorUpdate.setDevice(deviceRepository.findByName(TEST_DEVICE).get());
                    sensorRepository.save(sensorUpdate);

                }

            }
        }

    }

    /**
     * Reinstall the operators of the default testing components.
     */
    public void replaceOperators() {
        try {
            List<Operator> allOperators = operatorRepository.findAll();
            // Delete all operators needed for the simulators via the prefix "TESTING_" that all testing components contains
            for (Operator operator : allOperators) {
                if (operator.getName().contains(TEST_PREFIX)) {
                    operatorRepository.delete(operator);
                }
            }
            // Install all operators needed for the testing simulators
            defaultOperatorService.addDefaultOperators(defaultTestComponentsWhiteList);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }


    /**
     * Replaces the testing actuator with a new one.
     *
     * @return if the replacement was successful or not.
     */
    public ResponseEntity<Void> replaceTestingActuator() {
        try {
            // Delete the testing-device if existing
            if (actuatorRepository.findByName(ACTUATOR_NAME).isPresent()) {
                actuatorRepository.delete(actuatorRepository.findByName(ACTUATOR_NAME).get());
            }

            registerActuatorSimulator();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        return ResponseEntity.status(HttpStatus.OK).build();
    }


    /**
     * Redeploys all default components needed by the testing-tool.
     *
     * @return http status whether it was successful or not
     */
    public ResponseEntity<Void> redeployComponents() {
        Actuator testingActuator = null;
        try {
            if (actuatorRepository.findByName(ACTUATOR_NAME).isPresent()) {
                testingActuator = actuatorRepository.findByName(ACTUATOR_NAME).get();
            }

            List<Sensor> allSensors = sensorRepository.findAll();

            // Check if Actuator is currently running and redeploy it
            if (testingActuator != null) {
                if (deploymentWrapper.isComponentRunning(testingActuator)) {
                    deploymentWrapper.undeployComponent(testingActuator);
                }
                deploymentWrapper.deployComponent(testingActuator);
            }

            // Delete all sensor simulators via the prefix "TESTING_" that all testing components contains
            for (Sensor sensor : allSensors) {
                if (sensor.getName().contains(TEST_PREFIX)) {
                    if (deploymentWrapper.isComponentRunning(sensor)) {
                        deploymentWrapper.undeployComponent(sensor);
                    }
                    deploymentWrapper.deployComponent(sensor);
                }
            }

        } catch (Exception e) {
            ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
