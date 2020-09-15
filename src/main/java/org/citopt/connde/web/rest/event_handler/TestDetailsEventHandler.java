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

        List<Sensor> sensorArray = new ArrayList<>();


        // checks if the sensors for the test are registered. If not this will be done in the following, if the specific operators are existing.


        for (String sensType : testDetails.getType()) {

            Sensor sensor = sensorRepository.findByName(sensType);

            sensorArray.add(sensor);
        }
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

