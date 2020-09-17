package org.citopt.connde.web.rest.event_handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.citopt.connde.domain.component.Sensor;
import org.citopt.connde.domain.testing.TestDetails;
import org.citopt.connde.repository.ActuatorRepository;
import org.citopt.connde.repository.AdapterRepository;
import org.citopt.connde.repository.DeviceRepository;
import org.citopt.connde.repository.SensorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;


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
     * Called in case an test is supposed to be created. This method checks, if the required components are registered.
     * If the TestDevice does not exist, it will be automatically created. Sensor/Actuator Simulators will be automatically registered, if the operators are existing.
     *
     * @param testDetails Test with details to be created
     */
    @HandleBeforeCreate
    public void testStarted(TestDetails testDetails) throws IOException {
        Sensor sensorX;
        Sensor sensorY;
        Sensor sensorZ;
        List<Sensor> sensorArray = new ArrayList<>();


        // checks if the sensors for the test are registered. If not this will be done in the following, if the specific operators are existing.
        switch (testDetails.getType()) {
        	// TODO: Repository return objects should be checked - i temporarily added .orElse(null) to each function,
        	//       since this is equivalent to the former implementation of the repositories.
            case "TestingGPSSensor":
                sensorX = sensorRepository.findByName("TestingGPSLatitude").orElse(null);
                sensorY = sensorRepository.findByName("TestingGPSLongitude").orElse(null);
                sensorZ = sensorRepository.findByName("TestingGPSHight").orElse(null);
                if (sensorX == null) {
                    if (adapterRepository.findByName("TestingGPSLat").isPresent()) {
                        sensorX = new Sensor();
                        sensorX.setName("TestingGPSLatitude");
                        sensorX.setDevice(deviceRepository.findByName("TestingDevice").orElse(null));
                        sensorX.setAdapter(adapterRepository.findByName("TestingGPSLat").orElse(null));
                        sensorRepository.insert(sensorX);

                    }
                }

                if (sensorY == null) {
                    if (adapterRepository.findByName("TestingGPSLong").isPresent()) {
                        sensorY = new Sensor();
                        sensorY.setName("TestingGPSLongitude");
                        sensorY.setDevice(deviceRepository.findByName("TestingDevice").orElse(null));
                        sensorY.setAdapter(adapterRepository.findByName("TestingGPSLong").orElse(null));
                        sensorRepository.insert(sensorY);
                    }
                }
                if (sensorZ == null) {
                    if (adapterRepository.findByName("TestingGPSHight").isPresent()) {
                        sensorZ = new Sensor();
                        sensorZ.setName("TestingGPSHight");
                        sensorZ.setDevice(deviceRepository.findByName("TestingDevice").orElse(null));
                        sensorZ.setAdapter(adapterRepository.findByName("TestingGPSHight").orElse(null));
                        sensorRepository.insert(sensorZ);
                    }
                }

                if (sensorX != null & sensorY != null & sensorZ != null) {
                    sensorArray.add(sensorX);
                    sensorArray.add(sensorY);
                    sensorArray.add(sensorZ);
                    testDetails.setSensor(sensorArray);
                }
                break;
            case "TestingGPSSensorPl":
                sensorX = sensorRepository.findByName("TestingGPSLatitudePl").orElse(null);
                sensorY = sensorRepository.findByName("TestingGPSLongitudePl").orElse(null);
                sensorZ = sensorRepository.findByName("TestingGPSHightPl").orElse(null);
                if (sensorX == null) {
                    if (adapterRepository.findByName("TestingGPSLatitudePl") != null) {
                        sensorX = new Sensor();
                        sensorX.setName("TestingGPSLatitudePl");
                        sensorX.setDevice(deviceRepository.findByName("TestingDevice").orElse(null));
                        sensorX.setAdapter(adapterRepository.findByName("TestingGPSLatitudePl").orElse(null));
                        sensorRepository.insert(sensorX);
                    }
                }

                if (sensorY == null) {
                    if (adapterRepository.findByName("TestingGPSLongitudePl") != null) {
                        sensorY = new Sensor();
                        sensorY.setName("TestingGPSLongitudePl");
                        sensorY.setDevice(deviceRepository.findByName("TestingDevice").orElse(null));
                        sensorY.setAdapter(adapterRepository.findByName("TestingGPSLongitudePl").orElse(null));
                        sensorRepository.insert(sensorY);
                    }
                }
                if (sensorZ == null) {
                    if (adapterRepository.findByName("TestingGPSHightPl") != null) {
                        sensorZ = new Sensor();
                        sensorZ.setName("TestingGPSHightPl");
                        sensorZ.setDevice(deviceRepository.findByName("TestingDevice").orElse(null));
                        sensorZ.setAdapter(adapterRepository.findByName("TestingGPSHightPl").orElse(null));
                        sensorRepository.insert(sensorZ);
                    }
                }

                if (sensorX != null & sensorY != null & sensorZ != null) {
                    sensorArray.add(sensorX);
                    sensorArray.add(sensorY);
                    sensorArray.add(sensorZ);
                    testDetails.setSensor(sensorArray);
                }

                break;
            case "TestingBeschleunigungsSensor":
                sensorX = sensorRepository.findByName("TestingAccelerationX").orElse(null);
                sensorY = sensorRepository.findByName("TestingAccelerationY").orElse(null);
                sensorZ = sensorRepository.findByName("TestingAccelerationZ").orElse(null);

                if (sensorX == null) {
                    if (adapterRepository.findByName("TestingAccelerationX") != null) {
                        sensorX = new Sensor();
                        sensorX.setName("TestingAccelerationX");
                        sensorX.setDevice(deviceRepository.findByName("TestingDevice").orElse(null));
                        sensorX.setAdapter(adapterRepository.findByName("TestingAccelerationX").orElse(null));
                        sensorRepository.insert(sensorX);
                    }

                }

                if (sensorY == null) {
                    if (adapterRepository.findByName("TestingAccelerationY") != null) {
                        sensorY = new Sensor();
                        sensorY.setName("TestingAccelerationY");
                        sensorY.setDevice(deviceRepository.findByName("TestingDevice").orElse(null));
                        sensorY.setAdapter(adapterRepository.findByName("TestingAccelerationY").orElse(null));
                        sensorRepository.insert(sensorY);
                    }

                }
                if (sensorZ == null) {
                    if (adapterRepository.findByName("TestingAccelerationZ") != null) {
                        sensorZ = new Sensor();
                        sensorZ.setName("TestingAccelerationZ");
                        sensorZ.setDevice(deviceRepository.findByName("TestingDevice").orElse(null));
                        sensorZ.setAdapter(adapterRepository.findByName("TestingAccelerationZ").orElse(null));
                        sensorRepository.insert(sensorZ);
                    }
                }

                if (sensorX != null & sensorY != null & sensorZ != null) {
                    sensorArray.add(sensorX);
                    sensorArray.add(sensorY);
                    sensorArray.add(sensorZ);
                    testDetails.setSensor(sensorArray);
                }

                break;
            case "TestingBeschleunigungsSensorPl":
                sensorX = sensorRepository.findByName("TestingAccelerationPlX").orElse(null);
                sensorY = sensorRepository.findByName("TestingAccelerationPlY").orElse(null);
                sensorZ = sensorRepository.findByName("TestingAccelerationPlZ").orElse(null);
                if (sensorX == null) {
                    if (adapterRepository.findByName("TestingAccelerationPlX") != null) {
                        sensorX = new Sensor();
                        sensorX.setName("TestingAccelerationPlX");
                        sensorX.setDevice(deviceRepository.findByName("TestingDevice").orElse(null));
                        sensorX.setAdapter(adapterRepository.findByName("TestingAccelerationPlX").orElse(null));
                        sensorRepository.insert(sensorX);
                    }

                }

                if (sensorY == null) {
                    if (adapterRepository.findByName("TestingAccelerationPlY") != null) {
                        sensorY = new Sensor();
                        sensorY.setName("TestingAccelerationPlY");
                        sensorY.setDevice(deviceRepository.findByName("TestingDevice").orElse(null));
                        sensorY.setAdapter(adapterRepository.findByName("TestingAccelerationPlY").orElse(null));
                        sensorRepository.insert(sensorY);
                    }
                }
                if (sensorZ == null) {
                    if (adapterRepository.findByName("TestingAccelerationPlZ") != null) {
                        sensorZ = new Sensor();
                        sensorZ.setName("TestingAccelerationPlZ");
                        sensorZ.setDevice(deviceRepository.findByName("TestingDevice").orElse(null));
                        sensorZ.setAdapter(adapterRepository.findByName("TestingAccelerationPlZ").orElse(null));
                        sensorRepository.insert(sensorZ);
                    }
                }

                if (sensorX != null & sensorY != null & sensorZ != null) {
                    sensorArray.add(sensorX);
                    sensorArray.add(sensorY);
                    sensorArray.add(sensorZ);
                    testDetails.setSensor(sensorArray);
                }

                break;
            default:

                Sensor sensor = sensorRepository.findByName(testDetails.getType()).orElse(null);
                /**
                 if (sensor == null) {
                 sensor = new Sensor();
                 sensor.setName(testDetails.getType());
                 sensor.setDevice(deviceRepository.findByName("TestingDevice"));
                 if (adapterRepository.findByName(testDetails.getType()) != null) {
                 sensor.setAdapter(adapterRepository.findByName(testDetails.getType()));
                 sensorRepository.insert(sensor);
                 sensorArray.add(sensor);
                 testDetails.setSensor(sensorArray);
                 }

                 } else {
                 sensorArray.add(sensor);
                 testDetails.setSensor(sensorArray);
                 }
                 **/
                sensorArray.add(sensor);
                break;
        }


        // checks if the testing actuator is registered TODO: but then does nothing at all with the result ...
        actuatorRepository.findByName("TestingActuator").orElse(null);

        /**
         // if not the actuator will be registered in the following
         if (actuator == null) {
         actuator = new Actuator();
         actuator.setName("TestingActuator");
         if (adapterRepository.findByName("TestingAdapterAcutator") != null) {
         actuator.setAdapter(adapterRepository.findByName("TestingAdapterAcutator"));
         actuator.setComponentType("Speaker");
         actuator.setDevice(deviceRepository.findByName("TestingDevice"));
         actuatorRepository.insert(actuator);
         }

         }
         **/
        testDetails.setSensor(sensorArray);
    }


}

