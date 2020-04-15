package org.citopt.connde.domain.key;

import org.citopt.connde.repository.KeyPairRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;


/**
 * Validator for key pair objects.
 */
@Component
public class KeyPairValidator implements Validator {

    private static KeyPairRepository keyPairRepository;

    @Autowired
    public void setDeviceRepository(KeyPairRepository keyPairRepository) {
        KeyPairValidator.keyPairRepository = keyPairRepository;
    }

    /**
     * Checks whether the validator can be applied to objects of a given class. However, this validator can
     * only be applied to key pair objects.
     *
     * @param type The class to check
     * @return True, if the validator can be applied to objects of this class; false otherwise
     */
    @Override
    public boolean supports(Class<?> type) {
        return KeyPair.class.equals(type);
    }

    /**
     * Validates the fields of a key pair object and adds error messages to the errors object accordingly.
     *
     * @param o      The key pair object to validate
     * @param errors The errors object to add the error messages to
     */
    @Override
    public void validate(Object o, Errors errors) {
        //Cast to device
        KeyPair keyPair = (KeyPair) o;

        //Check if name was provided (mandatory)
        ValidationUtils.rejectIfEmptyOrWhitespace(
                errors, "name", "keypair.name.empty",
                "The name must not be empty.");


        //Private key is mandatory
        ValidationUtils.rejectIfEmptyOrWhitespace(
                errors, "privateKey", "keypair.privateKey.empty",
                "No private key provided.");


        //Check if name is unique
        KeyPair anotherPair = keyPairRepository.findByName(keyPair.getName());
        if (anotherPair != null) {
            errors.rejectValue("name", "keypair.name.duplicate",
                    "The name is already registered.");
        }
    }
}
