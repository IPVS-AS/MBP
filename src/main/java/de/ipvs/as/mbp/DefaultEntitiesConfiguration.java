package de.ipvs.as.mbp;

import de.ipvs.as.mbp.domain.entity_type.ActuatorType;
import de.ipvs.as.mbp.domain.entity_type.DeviceType;
import de.ipvs.as.mbp.domain.entity_type.SensorType;
import de.ipvs.as.mbp.domain.user.User;
import de.ipvs.as.mbp.repository.ActuatorTypeRepository;
import de.ipvs.as.mbp.repository.DeviceTypeRepository;
import de.ipvs.as.mbp.repository.SensorTypeRepository;
import de.ipvs.as.mbp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static de.ipvs.as.mbp.domain.entity_type.ActuatorType.createActuatorType;
import static de.ipvs.as.mbp.domain.entity_type.DeviceType.createDeviceType;
import static de.ipvs.as.mbp.domain.entity_type.SensorType.createSensorType;

/**
 * Configuration for entities that are supposed to be available as default.
 */
@Configuration
public class DefaultEntitiesConfiguration {
    //Default admin password
    private static final String DEFAULT_ADMIN_PASSWORD = "12345";

    //Path to images of entity types
    private static final String IMAGE_PATH = "images/";

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

    /**
     * Creates a bean representing a whitelist of paths to directories of operators that are used
     * to rerun a test in the testing-tool.
     *
     * @return The path whitelist bean
     */
    @Bean(name = "rerunOperatorWhitelist")
    public List<String> rerunOperatorWhitelist() {
        List<String> operatorPaths = Arrays.asList("/operators/extraction/simulators/rerun_adapter");
        return Collections.unmodifiableList(operatorPaths);
    }

    /**
     * Creates a bean representing a whitelist of paths to directories of operators that are supposed
     * to be available as default operators for the testing tool.
     *
     * @return The path whitelist bean
     */
    @Bean(name = "defaultTestComponentsWhiteList")
    public List<String> defaultTestComponentsWhiteList() {
        List<String> operatorPaths = Arrays.asList("/operators/extraction/simulators/sensoradapter_temp_planned",
                "/operators/extraction/simulators/sensoradapter_temp",
                "/operators/extraction/simulators/sensoradapter_hum",
                "/operators/extraction/simulators/sensoradapter_hum_planned",
                "/operators/control/simulators/actuator_adapter_testing"
        );
        return Collections.unmodifiableList(operatorPaths);
    }


    /**
     * Sets up a list of default users and adds them to the user repository if not already existing.
     *
     * @param userRepository  The user repository (auto-wired)
     * @param passwordEncoder Password encoder component for encoding default passwords
     */
    @Autowired
    public void defaultUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        //Create list of default users
        List<User> defaultUsers = Arrays.asList(
                //Admin user
                new User().setUsername("admin")
                        .setFirstName("admin")
                        .setLastName("admin")
                        .setPassword(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD))
                        .setAdmin(true)
                        .setSystemUser(true),
                //MBP user
                new User().setUsername("mbp")
                        .setFirstName("MBP")
                        .setLastName("Platform")
                        .setPassword(passwordEncoder.encode(UUID.randomUUID().toString()))
                        .setSystemUser(true)
                        .setLoginable(false),
                //Device client user for OAuth
                new User().setUsername("device-client")
                        .setFirstName("Device")
                        .setLastName("Client")
                        .setPassword(passwordEncoder.encode(UUID.randomUUID().toString()))
                        .setSystemUser(true)
                        .setLoginable(false)
        );

        //Iterate over all default users
        for (User user : defaultUsers) {
            //Try to find default user by username
            Optional<User> foundUser = userRepository.findByUsername(user.getUsername());

            //Check whether user was found and whether it is a system user
            if ((!foundUser.isPresent()) || (!foundUser.get().isSystemUser())) {
                //Add user to repository
                userRepository.insert(user);
            }
        }
    }

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
}
