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


        Device testDevice = deviceRepository.findByName("TestingDevice");
        if (testDevice == null) {
            //Open connection and send request
            URL url = new URL("http://localhost:8080/MBP_war_exploded/api/devices");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; utf-8");

            // authentication
            String user_name = "admin";
            String password = "admin";
            String userCredentials = user_name + ":" + password;
            String basicAuth = "Basic " + new String(new Base64().encode(userCredentials.getBytes()));
            connection.setRequestProperty("Authorization", basicAuth);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.3; Win64; x64; rv:27.0) Gecko/20100101 Firefox/27.0.2 Waterfox/27.0");

            String requestParameters = "{\"username\":\"ubuntu\",\"password\":\"\",\"name\":\"TestingDevice\",\"componentType\":\"Computer\",\"ipAddress\":\"192.168.221.167\",\"rsaKey\":\"-----BEGIN RSA PRIVATE KEY-----\nMIIEqgIBAAKCAQEA4zzAJS+xXz00YLyO8lvsSfY+6XX1mQs6pz7yioA6lO30mWMx\n95FP3rZiX2stId3VfEPKdPgLot7CoTMcSnQzDBR8bi1ej8c/FXzELb2kTQzZE7dX\nYaONvNfKGL27EMjhqRpL+rQeTGeyGqmr0WH7BeQ9nE6ylfxXzXAMWkTW6dv7go+j\n52xAS6dM5TZER/A2KvCgXisiQFzwqEHuXnoy9lpWHcSZzQL8Xkd9ZbGAr3ex0pEc\n8220d4KT8oATLBDZo/fJyGiQNR5sab8RlpecbGJoh0QJIdnU3Eq02HYSzAQ7a8cB\nYGEm1xtOQOxV2a4+8f/g9FSC3hjobwmSoNu/nQIDAQABAoIBACy3ytRGk3A7mjAj\nSzo0jsZrWCwXU5KfnBZHk/FflKe0QDtjQvUGOqKIX8mJTONqRVXj/VaRbbDKh6Cz\nbzDTtyv8aBRCh2Zh/m8bE3ww4sFq8tknbmG/jugHyzSdOc/uyEG/9A3NHl1I1sra\ncv6MeprJNLqq3ggYFatPDo9BFs4EZoaIxEMD3plHfENfJOu7IS0xRoe5foXYbnM3\nji7n243OBGPAdCZXJkhYNgRoZmwMeMOJWK7EmiiM60ZKpHl8C4jSuzQ132aK/NDH\n3xgr+1nI8i8CfAWBlP8hfCXJJ8EiS5lE94jnP7u49BhjbPgULaNPDDYpVh5/uTlP\nYV5iAcECggCBAO7y4xBuMc8G57lqepoZHtCjSPpXTEC6hE7+pmnwvi6gUZPV7Tx/\nC7JecekilTy3Z+MjU1jwy7Bu0L3EJsJBn2N5aGYVxGFHGqrfA/qhPeyJsIU2w2UZ\nm1BEgNjP7bMZSMCSYd2CU9mP5dt3vGpEU6oZgwe9jm5QghYVjHaB6NUNAoIAgQDz\nc+sqdCn+rIpE6uovtqXJ9l3psaz+egRLcWS0ZVtrdhmMPBz/rZ16KhqmA+aszjiP\n8JqO3uXiB1LR+ACc6tRzeCWXNWipgzJGvLfgZBwGHeFje+uMyd1nYUq3qd0zP81j\ntpZpIAlHlPc+UREqiUhJJkjP+tEwwznP4zaC4wkQ0QKCAIEA3bMRpf73y8P2X/xB\nQJSqGJ5Haa5xm2TyuXBf6s9pRU2OIwJLmOOvcJFcUxi5Kppok0AFZvITquFGX6uM\n4pOMVPkiOgVcLX2RapR81p+gGsUtuIu1AyqdBf5pJcDWJGQDMlke4Cy5q5RtihEw\nCdDXZ21AO4BOlF+yMtdPeezSoEkCggCBAIBzsiolPp8sRIxWcpgYQ+OLBUQvxjpD\nAQ8ZVmxEancJyjMO6LIS1dtGaeccedLFwFxaNAKcIykeehllRFWHJe+C/jqJKJ8A\nJT/jhRV1XL/xdiG6ma8gN5y7XeQIUTkgOeuZxETVbXACbm3H8knCQ4ytEZADI+sZ\npuBEX1eyGO9xAoIAgQCwh2QkYnGPQkY4d9ra+WcrUz8sb7CEu4IS42XNt8SFkyU4\nfkE7aE4bHufPnnTEZ4jIGk0/E1b8AhLh1suRpg3tltYEj5CJfF1UywoUGuHhQkzP\n7jZaNQ1xdB+0woK3IenLVpDjxWGZbZTxviim1v1lpLSJxfr/HfvW1DJc4x/iug==\n-----END RSA PRIVATE KEY-----\",\"errors\":{}}";
            Object input = JSON.parse(requestParameters);

            byte[] postDataBytes = input.toString().getBytes(StandardCharsets.UTF_8);
            connection.setDoOutput(true);

            try (DataOutputStream writer = new DataOutputStream(connection.getOutputStream())) {
                writer.write(postDataBytes);
                writer.flush();
                writer.close();

                StringBuilder content;
            }
            connection.getResponseCode();

            connection.disconnect();
        }


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
                break;
        }


        // checks if the testing actuator is registered
        Actuator actuator = actuatorRepository.findByName("TestingActuator");

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


    }
}
