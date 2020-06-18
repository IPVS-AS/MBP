package org.citopt.connde.web.rest.event_handler;

import org.citopt.connde.domain.component.Actuator;
import org.citopt.connde.domain.component.Sensor;
import org.citopt.connde.domain.testing.TestDetails;
import org.citopt.connde.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Event handler for operations that are performed on tests.
 */
@Component
@RepositoryEventHandler
public class TestDetailsEventHandler {
    @Autowired
    private SensorRepository sensorRepository;
    @Autowired
    private DeviceRepository deviceRepository;
    @Autowired
    private AdapterRepository adapterRepository;

    @Autowired
    private ActuatorRepository actuatorRepository;

    /**
     * Called in case an test is supposed to be created. This method takes care, if all components
     * which are used exists. If they don't the components are registered here.
     *
     * @param testDetails Test with details to be created
     */
    @HandleBeforeCreate
    public void testStarted(TestDetails testDetails) {
        Sensor sensorX;
        Sensor sensorY;
        Sensor sensorZ;
        List<Sensor> sensorArray = new ArrayList<>();

        // checks if the sensors for the test are registered. If not this will be done in the following.
        switch (testDetails.getType()) {
            case "TestingGPSSensor":
                sensorX = sensorRepository.findByName("TestingGPSLatitude");
                sensorY = sensorRepository.findByName("TestingGPSLongitude");
                sensorZ = sensorRepository.findByName("TestingGPSHight");
                if (sensorX == null) {
                    sensorX = new Sensor();
                    sensorX.setName("TestingGPSLatitude");
                    sensorX.setDevice(deviceRepository.findByName(""));
                    sensorX.setAdapter(adapterRepository.findByName("TestingGPSLatitude"));
                    sensorRepository.insert(sensorX);
                }

                if (sensorY == null) {
                    sensorY = new Sensor();
                    sensorY.setName("TestingGPSLongitude");
                    sensorY.setDevice(deviceRepository.findByName("TestingDevice"));
                    sensorY.setAdapter(adapterRepository.findByName("TestingGPSLongitude"));
                    sensorRepository.insert(sensorY);
                }
                if (sensorZ == null) {
                    sensorZ = new Sensor();
                    sensorZ.setName("TestingGPSHight");
                    sensorZ.setDevice(deviceRepository.findByName("TestingDevice"));
                    sensorZ.setAdapter(adapterRepository.findByName("TestingGPSHight"));
                    sensorRepository.insert(sensorZ);
                }
                sensorArray.add(sensorX);
                sensorArray.add(sensorY);
                sensorArray.add(sensorZ);
                break;
            case "TestingGPSSensorPl":
                sensorX = sensorRepository.findByName("TestingGPSLatitudePl");
                sensorY = sensorRepository.findByName("TestingGPSLongitudePl");
                sensorZ = sensorRepository.findByName("TestingGPSHightPl");
                if (sensorX == null) {
                    sensorX = new Sensor();
                    sensorX.setName("TestingGPSLatitudePl");
                    sensorX.setDevice(deviceRepository.findByName("TestingDevice"));
                    sensorX.setAdapter(adapterRepository.findByName("TestingGPSLatitudePl"));
                    sensorRepository.insert(sensorX);
                }

                if (sensorY == null) {
                    sensorY = new Sensor();
                    sensorY.setName("TestingGPSLongitudePl");
                    sensorY.setDevice(deviceRepository.findByName("TestingDevice"));
                    sensorY.setAdapter(adapterRepository.findByName("TestingGPSLongitudePl"));
                    sensorRepository.insert(sensorY);
                }
                if (sensorZ == null) {
                    sensorZ = new Sensor();
                    sensorZ.setName("TestingGPSHightPl");
                    sensorZ.setDevice(deviceRepository.findByName("TestingDevice"));
                    sensorZ.setAdapter(adapterRepository.findByName("TestingGPSHightPl"));
                    sensorRepository.insert(sensorZ);
                }

                sensorArray.add(sensorX);
                sensorArray.add(sensorY);
                sensorArray.add(sensorZ);
                break;
            case "TestingBeschleunigungsSensor":
                sensorX = sensorRepository.findByName("TestingAccelerationX");
                sensorY = sensorRepository.findByName("TestingAccelerationY");
                sensorZ = sensorRepository.findByName("TestingAccelerationZ");

                if (sensorX == null) {
                    sensorX = new Sensor();
                    sensorX.setName("TestingAccelerationX");
                    sensorX.setDevice(deviceRepository.findByName("TestingDevice"));
                    sensorX.setAdapter(adapterRepository.findByName("TestingAccelerationX"));
                    sensorRepository.insert(sensorX);
                }

                if (sensorY == null) {
                    sensorY = new Sensor();
                    sensorY.setName("TestingAccelerationY");
                    sensorY.setDevice(deviceRepository.findByName("TestingDevice"));
                    sensorY.setAdapter(adapterRepository.findByName("TestingAccelerationY"));
                    sensorRepository.insert(sensorY);
                }
                if (sensorZ == null) {
                    sensorZ = new Sensor();
                    sensorZ.setName("TestingAccelerationZ");
                    sensorZ.setDevice(deviceRepository.findByName("TestingDevice"));
                    sensorZ.setAdapter(adapterRepository.findByName("TestingAccelerationZ"));
                    sensorRepository.insert(sensorZ);
                }

                sensorArray.add(sensorX);
                sensorArray.add(sensorY);
                sensorArray.add(sensorZ);
                break;
            case "TestingBeschleunigungsSensorPl":
                sensorX = sensorRepository.findByName("TestingAccelerationPlX");
                sensorY = sensorRepository.findByName("TestingAccelerationPlY");
                sensorZ = sensorRepository.findByName("TestingAccelerationPlZ");
                if (sensorX == null) {
                    sensorX = new Sensor();
                    sensorX.setName("TestingAccelerationPlX");
                    sensorX.setDevice(deviceRepository.findByName("TestingDevice"));
                    sensorX.setAdapter(adapterRepository.findByName("TestingAccelerationPlX"));
                    sensorRepository.insert(sensorX);
                }

                if (sensorY == null) {
                    sensorY = new Sensor();
                    sensorY.setName("TestingAccelerationPlY");
                    sensorY.setDevice(deviceRepository.findByName("TestingDevice"));
                    sensorY.setAdapter(adapterRepository.findByName("TestingAccelerationPlY"));
                    sensorRepository.insert(sensorY);
                }
                if (sensorZ == null) {
                    sensorZ = new Sensor();
                    sensorZ.setName("TestingAccelerationPlZ");
                    sensorZ.setDevice(deviceRepository.findByName("TestingDevice"));
                    sensorZ.setAdapter(adapterRepository.findByName("TestingAccelerationPlZ"));
                    sensorRepository.insert(sensorZ);
                }


                sensorArray.add(sensorX);
                sensorArray.add(sensorY);
                sensorArray.add(sensorZ);
                break;
            default:
                /**
                Sensor sensor = sensorRepository.findByName(testDetails.getType());
                if (sensor == null) {
                    sensor = new Sensor();
                    sensor.setName(testDetails.getType());
                    sensor.setDevice(deviceRepository.findByName("TestingDevice"));
                    sensor.setAdapter(adapterRepository.findByName(testDetails.getType()));
                    sensorRepository.insert(sensor);
                }
                sensorArray.add(sensor);**/
                break;
        }


        // checks if the testing actuator is registered
        Actuator actuator = actuatorRepository.findByName("TestingActuator");

        /**
        // if not the actuator will be registered in the following
        if (actuator == null) {
            actuator = new Actuator();
            actuator.setName("TestingActuator");
            actuator.setAdapter(adapterRepository.findByName("TestingAdapterAcutator"));
            actuator.setComponentType("Speaker");
            actuator.setDevice(deviceRepository.findByName("TestingDevice"));

            actuatorRepository.insert(actuator);
        }
**/
        testDetails.setSensor(sensorArray);
    }


}
