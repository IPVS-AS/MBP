package org.citopt.sensmonqtt.domain.device;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

    public boolean validateMacAddress(String mac) {
        if (mac == null) {
            return false;
        }
        //Pattern p = Pattern.compile("^([a-fA-F0-9][:-]){5}[a-fA-F0-9][:-]$");
        Pattern p = Pattern.compile("^([a-f0-9]){12}$");
        Matcher m = p.matcher(mac);
        return m.find();
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

        ValidationUtils.rejectIfEmptyOrWhitespace(
                errors, "macAddress", "device.macAddress.empty",
                "The MAC address cannot be empty!");

        if (!validateMacAddress(device.getMacAddress())) {
            errors.rejectValue("macAddress", "device.macAddress.invalid",
                    "The MAC address has an invalid format");
        }

        Device another;

        // Unique name
        if ((another = repository.findByName(device.getName())) != null) {
            if (device.getId() == null
                    || !device.getId().equals(another.getId())) {
                errors.rejectValue("name", "device.name.duplicate",
                        "The name is already registered");
            }
        }

        // Unique macAddress
        if ((another = repository.findByMacAddress(device.getMacAddress())) != null) {
            if (device.getId() == null
                    || !device.getId().equals(another.getId())) {
                errors.rejectValue("macAddress", "device.macAddress.duplicate",
                        "The MAC address is already registered");
            }
        }
    }
}
