package org.citopt.connde.domain.monitoring;

import org.citopt.connde.domain.operator.OperatorValidator;
import org.citopt.connde.repository.MonitoringOperatorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

/**
 * Objects of this class work as validators for monitoring operator objects.
 *
 * @author Jan
 */
@Component
public class MonitoringOperatorValidator extends OperatorValidator {
    static MonitoringOperatorRepository repository;

    /**
     * Injects the repository for monitoring operators into this object.
     *
     * @param monitoringOperatorRepository The repository to inject
     */
    @Autowired
    public void setTypeRepository(MonitoringOperatorRepository monitoringOperatorRepository) {
        MonitoringOperatorValidator.repository = monitoringOperatorRepository;
    }

    /**
     * Checks whether objects of a specific class are supported by this validator.
     *
     * @param type The class type to check
     * @return True, if objects of the given class are supported; false otherwise
     */
    @Override
    public boolean supports(Class<?> type) {
        return MonitoringOperator.class.isAssignableFrom(type);
    }

    @Override
    public void validate(Object o, Errors errors) {
        //Cast to operator
        MonitoringOperator operator = (MonitoringOperator) o;

        //Use super class for validation
        super.validate(operator, errors);

        /*
        More specific validation goes here
         */

        //Check if device types were provided
        if (operator.getDeviceTypesNumber() < 1) {
            errors.rejectValue("deviceTypes", "operator.deviceTypes.empty",
                    "At least one device type needs to be selected.");
        }
    }
}
