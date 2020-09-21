package org.citopt.connde;

import org.citopt.connde.domain.entity_type.DeviceType;
import org.citopt.connde.repository.DeviceTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Configuration for entities that are supposed to be available as default.
 */
@Configuration
public class DefaultEntitiesConfiguration {

    private DeviceTypeRepository deviceTypeRepository;

    @Autowired
    public DefaultEntitiesConfiguration(DeviceTypeRepository deviceTypeRepository) {
        List<DeviceType> deviceTypes = Arrays.asList(
                new DeviceType("Raspberry Pi"),
                new DeviceType("Arduino"),
                new DeviceType("Computer"),
                new DeviceType("Audio System"),
                new DeviceType("Camera"),
                new DeviceType("Gateway"),
                new DeviceType("Laptop"),
                new DeviceType("Smartphone"),
                new DeviceType("Smartwatch"),
                new DeviceType("TV"),
                new DeviceType("Voice Controller"),
                new DeviceType("Virtual Machine")
        );

        this.deviceTypeRepository = deviceTypeRepository;
        deviceTypeRepository.insert(deviceTypes);
    }

    /**
     * Creates a bean holding a list of default device types.
     *
     * @return The device types bean
     */
    @Bean(name = "defaultDeviceTypes")
    public List<DeviceType> defaultDeviceTypes() {
        //TODO
        return Collections.unmodifiableList(Arrays.asList());
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
