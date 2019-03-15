package org.citopt.connde.domain.monitoring;

import org.citopt.connde.domain.adapter.Adapter;
import org.citopt.connde.domain.adapter.AdapterValidator;
import org.citopt.connde.domain.adapter.parameters.Parameter;
import org.citopt.connde.repository.MonitoringAdapterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * Objects of this class work as validators for monitoring adapter objects.
 *
 * @author Jan
 */
@Component
public class MonitoringAdapterValidator extends AdapterValidator {
    static MonitoringAdapterRepository repository;

    /**
     * Injects the repository for monitoring adapters into this object.
     *
     * @param monitoringAdapterRepository The repository to inject
     */
    @Autowired
    public void setTypeRepository(MonitoringAdapterRepository monitoringAdapterRepository) {
        MonitoringAdapterValidator.repository = monitoringAdapterRepository;
    }

    /**
     * Checks whether objects of a specific class are supported by this validator.
     *
     * @param type The class type to check
     * @return True, if objects of the given class are supported; false otherwise
     */
    @Override
    public boolean supports(Class<?> type) {
        return Adapter.class.isAssignableFrom(type);
    }

    @Override
    public void validate(Object o, Errors errors) {
        //Cast to adapter
        MonitoringAdapter adapter = (MonitoringAdapter) o;

        //Use super class for validation
        super.validate(adapter, errors);

        /*
        More specific validation goes here
         */

        //Check if device types were provided
        if (adapter.getDeviceTypesNumber() < 1) {
            errors.rejectValue("deviceTypes", "adapter.deviceTypes.empty",
                    "At least one device type needs to be selected.");
        }
    }
}
