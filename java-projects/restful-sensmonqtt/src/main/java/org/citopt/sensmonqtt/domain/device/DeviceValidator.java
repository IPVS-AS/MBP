package org.citopt.sensmonqtt.domain.device;

import org.citopt.sensmonqtt.domain.device.Device;
import org.citopt.sensmonqtt.domain.device.DeviceValidator;
import org.citopt.sensmonqtt.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 *
 * @author rafaelkperes
 */
@Component
public class DeviceValidator implements Validator {

    static DeviceRepository repository;

    @Autowired
    public void setDeviceRepository(DeviceRepository deviceRepository) {
        System.out.println("autowiring deviceRepository to DeviceValidator");
        DeviceValidator.repository = deviceRepository;
    }

    @Override
    public boolean supports(Class<?> type) {
        return Device.class.isAssignableFrom(type);
    }

    @Override
    public void validate(Object o, Errors errors) {
        Device device = (Device) o;

        validate(device, errors);
    }

    public void validate(Device device, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(
                errors, "name", "device.name.empty",
                "The name cannot be empty!");

        Device another;
        if ((another = repository.findByName(device.getName())) != null) {
            if (device.getId() == null
                    || !device.getId().equals(another.getId())) {
                errors.rejectValue("name", "device.name.duplicate",
                        "The name is already registered");
            }
        }
    }
}
