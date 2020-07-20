package org.citopt.connde.web.rest.event_handler;

import com.mongodb.util.JSON;
import org.apache.commons.codec.binary.Base64;
import org.citopt.connde.domain.component.Actuator;
import org.citopt.connde.domain.component.Sensor;
import org.citopt.connde.domain.device.Device;
import org.citopt.connde.domain.testing.TestDetails;
import org.citopt.connde.repository.ActuatorRepository;
import org.citopt.connde.repository.AdapterRepository;
import org.citopt.connde.repository.DeviceRepository;
import org.citopt.connde.repository.SensorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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
            case "TestingGPSSensor":
                sensorX = sensorRepository.findByName("TestingGPSLatitude");
                sensorY = sensorRepository.findByName("TestingGPSLongitude");
                sensorZ = sensorRepository.findByName("TestingGPSHight");
                if (sensorX == null) {
                    if (adapterRepository.findByName("TestingGPSLat") != null) {
                        sensorX = new Sensor();
                        sensorX.setName("TestingGPSLatitude");
                        sensorX.setDevice(deviceRepository.findByName("TestingDevice"));
                        sensorX.setAdapter(adapterRepository.findByName("TestingGPSLat"));
                        sensorRepository.insert(sensorX);

                    }
                }

                if (sensorY == null) {
                    if (adapterRepository.findByName("TestingGPSLong") != null) {
                        sensorY = new Sensor();
                        sensorY.setName("TestingGPSLongitude");
                        sensorY.setDevice(deviceRepository.findByName("TestingDevice"));
                        sensorY.setAdapter(adapterRepository.findByName("TestingGPSLong"));
                        sensorRepository.insert(sensorY);
                    }
                }
                if (sensorZ == null) {
                    if (adapterRepository.findByName("TestingGPSHight") != null) {
                        sensorZ = new Sensor();
                        sensorZ.setName("TestingGPSHight");
                        sensorZ.setDevice(deviceRepository.findByName("TestingDevice"));
                        sensorZ.setAdapter(adapterRepository.findByName("TestingGPSHight"));
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
                sensorX = sensorRepository.findByName("TestingGPSLatitudePl");
                sensorY = sensorRepository.findByName("TestingGPSLongitudePl");
                sensorZ = sensorRepository.findByName("TestingGPSHightPl");
                if (sensorX == null) {
                    if (adapterRepository.findByName("TestingGPSLatitudePl") != null) {
                        sensorX = new Sensor();
                        sensorX.setName("TestingGPSLatitudePl");
                        sensorX.setDevice(deviceRepository.findByName("TestingDevice"));
                        sensorX.setAdapter(adapterRepository.findByName("TestingGPSLatitudePl"));
                        sensorRepository.insert(sensorX);
                    }
                }

                if (sensorY == null) {
                    if (adapterRepository.findByName("TestingGPSLongitudePl") != null) {
                        sensorY = new Sensor();
                        sensorY.setName("TestingGPSLongitudePl");
                        sensorY.setDevice(deviceRepository.findByName("TestingDevice"));
                        sensorY.setAdapter(adapterRepository.findByName("TestingGPSLongitudePl"));
                        sensorRepository.insert(sensorY);
                    }
                }
                if (sensorZ == null) {
                    if (adapterRepository.findByName("TestingGPSHightPl") != null) {
                        sensorZ = new Sensor();
                        sensorZ.setName("TestingGPSHightPl");
                        sensorZ.setDevice(deviceRepository.findByName("TestingDevice"));
                        sensorZ.setAdapter(adapterRepository.findByName("TestingGPSHightPl"));
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
                sensorX = sensorRepository.findByName("TestingAccelerationX");
                sensorY = sensorRepository.findByName("TestingAccelerationY");
                sensorZ = sensorRepository.findByName("TestingAccelerationZ");

                if (sensorX == null) {
                    if (adapterRepository.findByName("TestingAccelerationX") != null) {
                        sensorX = new Sensor();
                        sensorX.setName("TestingAccelerationX");
                        sensorX.setDevice(deviceRepository.findByName("TestingDevice"));
                        sensorX.setAdapter(adapterRepository.findByName("TestingAccelerationX"));
                        sensorRepository.insert(sensorX);
                    }

                }

                if (sensorY == null) {
                    if (adapterRepository.findByName("TestingAccelerationY") != null) {
                        sensorY = new Sensor();
                        sensorY.setName("TestingAccelerationY");
                        sensorY.setDevice(deviceRepository.findByName("TestingDevice"));
                        sensorY.setAdapter(adapterRepository.findByName("TestingAccelerationY"));
                        sensorRepository.insert(sensorY);
                    }

                }
                if (sensorZ == null) {
                    if (adapterRepository.findByName("TestingAccelerationZ") != null) {
                        sensorZ = new Sensor();
                        sensorZ.setName("TestingAccelerationZ");
                        sensorZ.setDevice(deviceRepository.findByName("TestingDevice"));
                        sensorZ.setAdapter(adapterRepository.findByName("TestingAccelerationZ"));
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
                sensorX = sensorRepository.findByName("TestingAccelerationPlX");
                sensorY = sensorRepository.findByName("TestingAccelerationPlY");
                sensorZ = sensorRepository.findByName("TestingAccelerationPlZ");
                if (sensorX == null) {
                    if (adapterRepository.findByName("TestingAccelerationPlX") != null) {
                        sensorX = new Sensor();
                        sensorX.setName("TestingAccelerationPlX");
                        sensorX.setDevice(deviceRepository.findByName("TestingDevice"));
                        sensorX.setAdapter(adapterRepository.findByName("TestingAccelerationPlX"));
                        sensorRepository.insert(sensorX);
                    }

                }

                if (sensorY == null) {
                    if (adapterRepository.findByName("TestingAccelerationPlY") != null) {
                        sensorY = new Sensor();
                        sensorY.setName("TestingAccelerationPlY");
                        sensorY.setDevice(deviceRepository.findByName("TestingDevice"));
                        sensorY.setAdapter(adapterRepository.findByName("TestingAccelerationPlY"));
                        sensorRepository.insert(sensorY);
                    }
                }
                if (sensorZ == null) {
                    if (adapterRepository.findByName("TestingAccelerationPlZ") != null) {
                        sensorZ = new Sensor();
                        sensorZ.setName("TestingAccelerationPlZ");
                        sensorZ.setDevice(deviceRepository.findByName("TestingDevice"));
                        sensorZ.setAdapter(adapterRepository.findByName("TestingAccelerationPlZ"));
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

                Sensor sensor = sensorRepository.findByName(testDetails.getType());
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


        // checks if the testing actuator is registered
        Actuator actuator = actuatorRepository.findByName("TestingActuator");

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
}
