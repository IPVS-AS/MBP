package org.citopt.connde.domain.device;

import org.citopt.connde.util.Validation;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;


/**
 * Validator for device objects. It includes checks for valid formats of MAC addresses, IP addresses and private
 * RSA keys.
 * <p>
 * Created by Jan on 10.12.2018.
 */
@org.springframework.stereotype.Component
public class DeviceValidator implements Validator {

    /**
     * Checks whether the validator can be applied to objects of a given class. However, this validator can
     * only be applied to device objects.
     *
     * @param type The class to check
     * @return True, if the validator can be applied to objects of this class; false otherwise
     */
    @Override
    public boolean supports(Class<?> type) {
        return Device.class.equals(type);
    }

    /**
     * Validates the fields of a device object and adds error messages to the errors object accordingly.
     *
     * @param o      The object to validate
     * @param errors The errors object to add the error messages to
     */
    @Override
    public void validate(Object o, Errors errors) {
        //Cast to device
        Device device = (Device) o;

        //Check if name was provided (mandatory)
        ValidationUtils.rejectIfEmptyOrWhitespace(
                errors, "name", "device.name.empty",
                "The name must not be empty.");

      //Check if device type was provided (mandatory)
        ValidationUtils.rejectIfEmptyOrWhitespace(
                errors, "componentType", "component.componentType.empty",
                "The component type cannot be empty!");
        
        //Check if ip address was provided (mandatory)
        ValidationUtils.rejectIfEmptyOrWhitespace(
                errors, "ipAddress", "device.ipAddress.empty",
                "The ip address must not be empty.");

        //Check if user name was provided (mandatory)
        ValidationUtils.rejectIfEmptyOrWhitespace(
                errors, "username", "device.username.empty",
                "The user name must not be empty.");

        //Check if rsa key was provided (mandatory)
        ValidationUtils.rejectIfEmptyOrWhitespace(
                errors, "rsaKey", "device.rsaKey.empty",
                "The RSA key must not be empty!");

        //Retrieve fields that need to be of a certain format
        String ipAddress = device.getIpAddress();
        String macAddress = device.getMacAddress();
        String rsaKey = device.getRsaKey();

        //Validate format of the IP address
        if ((ipAddress != null) && (!Validation.isValidIPAddress(ipAddress))) {
            errors.rejectValue("ipAddress", "device.ipAddress.illegal_format",
                    "Illegal IP address provided.");
        }

        //Validate format of the MAC address (if provided)
        if ((macAddress != null) && (!macAddress.isEmpty()) && (!Validation.isValidUnformattedMACAddress(macAddress))) {
            errors.rejectValue("macAddress", "device.macAddress.illegal_format",
                    "Illegal MAC address provided.");
        }

        //Validate format of the private RSA key
        if ((rsaKey != null) && (!Validation.isValidPrivateRSAKey(rsaKey))) {
            errors.rejectValue("rsaKey", "device.rsaKey.illegal_format",
                    "The provided string does not seem to be a valid private RSA key.");
        }
    }
}
