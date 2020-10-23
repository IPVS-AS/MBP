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

import static org.citopt.connde.domain.entity_type.ActuatorType.createActuatorType;
import static org.citopt.connde.domain.entity_type.DeviceType.createDeviceType;
import static org.citopt.connde.domain.entity_type.SensorType.createSensorType;

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

        //Put together the path to the device icons
        String iconPath = IMAGE_PATH + "device_types/";

        //Create list of device types
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

        //Put together the path to the actuator icons
        String iconPath = IMAGE_PATH + "actuator_types/";

        //Create list of actuator types
        List<ActuatorType> actuatorTypes = Arrays.asList(
                createActuatorType("Buzzer", iconPath + "buzzer.png"),
                createActuatorType("Speaker", iconPath + "speaker.png"),
                createActuatorType("Switch", iconPath + "switch.png"),
                createActuatorType("LED", iconPath + "led.png"),
                createActuatorType("Light", iconPath + "light.png"),
                createActuatorType("Vibration", iconPath + "vibration.png"),
                createActuatorType("Motor", iconPath + "motor.png"),
                createActuatorType("Heater", iconPath + "heater.png"),
                createActuatorType("Air Conditioner", iconPath + "air-conditioner.png")
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

        //Put together the path to the device icons
        String iconPath = IMAGE_PATH + "sensor_types/";

        //Create list of sensor types
        List<SensorType> sensorTypes = Arrays.asList(
                createSensorType("Temperature", iconPath + "temperature.png"),
                createSensorType("Motion", iconPath + "motion.png"),
                createSensorType("Camera", iconPath + "camera.png"),
                createSensorType("Location", iconPath + "location.png"),
                createSensorType("Gas", iconPath + "gas.png"),
                createSensorType("Sound", iconPath + "sound.png"),
                createSensorType("Touch", iconPath + "touch.png"),
                createSensorType("Humidity", iconPath + "humidity.png"),
                createSensorType("Vibration", iconPath + "vibration.png"),
                createSensorType("Gyroscope", iconPath + "gyroscope.png"),
                createSensorType("Pressure", iconPath + "default.png"),
                createSensorType("Proximity", iconPath + "proximity.png"),
                createSensorType("Light", iconPath + "light.png")
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
