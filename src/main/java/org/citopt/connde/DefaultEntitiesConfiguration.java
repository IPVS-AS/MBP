package org.citopt.connde;

import org.citopt.connde.domain.entity_type.ActuatorType;
import org.citopt.connde.domain.entity_type.DeviceType;
import org.citopt.connde.domain.entity_type.SensorType;
import org.citopt.connde.repository.ActuatorTypeRepository;
import org.citopt.connde.repository.DeviceTypeRepository;
import org.citopt.connde.repository.SensorTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.citopt.connde.domain.entity_type.DeviceType.createDeviceType;

/**
 * Configuration for entities that are supposed to be available as default.
 */
@Configuration
public class DefaultEntitiesConfiguration {

    private static final String IMAGE_PATH = "images/";

    /**
     * Sets up a list of default device types and adds them to the device type repository.
     *
     * @param deviceTypeRepository The device type repository (auto-wired)
     */
    @Autowired
    public void defaultDeviceTypes(DeviceTypeRepository deviceTypeRepository) {

        String iconPath = IMAGE_PATH + "device_types/";

        List<DeviceType> deviceTypes = Arrays.asList(
                createDeviceType("Raspberry Pi", true, iconPath + "raspberry-pi.png"),
                createDeviceType("Arduino", false, iconPath + "arduino.png"),
                createDeviceType("Computer", true, iconPath + "computer.png"),
                createDeviceType("Audio System", false, iconPath + "audio-system.png"),
                createDeviceType("Camera", false, iconPath + "camera.png"),
                createDeviceType("Laptop", true, iconPath + "laptop.png"),
                createDeviceType("Smartphone", false, iconPath + "smartphone.png"),
                createDeviceType("Smartwatch", false, iconPath + "smartwatch.png"),
                createDeviceType("TV", false, iconPath + "tv.png"),
                createDeviceType("Voice Controller", false, iconPath + "voice-controller.png"),
                createDeviceType("Virtual Machine", true, iconPath + "default.png")
        );

        //Only add if repository is empty
        if (deviceTypeRepository.count() <= 0) {
            deviceTypeRepository.insert(deviceTypes);
        }
    }

    /**
     * Sets up a list of default actuator types and adds them to the actuator type repository.
     *
     * @param actuatorTypeRepository The actuator type repository (auto-wired)
     */
    @Autowired
    public void defaultActuatorTypes(ActuatorTypeRepository actuatorTypeRepository) {
        List<ActuatorType> actuatorTypes = Arrays.asList(
                new ActuatorType("Buzzer"),
                new ActuatorType("Speaker"),
                new ActuatorType("Switch"),
                new ActuatorType("LED"),
                new ActuatorType("Light"),
                new ActuatorType("Vibration"),
                new ActuatorType("Motor"),
                new ActuatorType("Heater"),
                new ActuatorType("Air Conditioner")
        );

        //Only add if repository is empty
        if (actuatorTypeRepository.count() <= 0) {
            actuatorTypeRepository.insert(actuatorTypes);
        }
    }

    /**
     * Sets up a list of default sensor types and adds them to the sensor type repository.
     *
     * @param sensorTypeRepository The sensor type repository (auto-wired)
     */
    @Autowired
    public void defaultSensorTypes(SensorTypeRepository sensorTypeRepository) {
        List<SensorType> sensorTypes = Arrays.asList(
                new SensorType("Temperature"),
                new SensorType("Motion"),
                new SensorType("Camera"),
                new SensorType("Location"),
                new SensorType("Gas"),
                new SensorType("Sound"),
                new SensorType("Touch"),
                new SensorType("Humidity"),
                new SensorType("Vibration"),
                new SensorType("Gyroscope"),
                new SensorType("Pressure"),
                new SensorType("Proximity"),
                new SensorType("Light")
        );

        //Only add if repository is empty
        if (sensorTypeRepository.count() <= 0) {
            sensorTypeRepository.insert(sensorTypes);
        }
    }

    /**
     * Creates a bean representing a whitelist of paths to directories of operators that are supposed
     * to be available as default operators.
     *
     * @return The path whitelist bean
     */
    @Bean(name = "defaultOperatorWhitelist")
    public List<String> defaultOperatorWhitelist() {
        List<String> operatorPaths = Arrays.asList("/operators/extraction/temperature_stub");
        return Collections.unmodifiableList(operatorPaths);
    }
}
