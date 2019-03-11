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
 * @author Jan
 */
@Component
public class MonitoringAdapterValidator extends AdapterValidator {
    static MonitoringAdapterRepository repository;

    @Autowired
    public void setTypeRepository(MonitoringAdapterRepository adapterRepository) {
        MonitoringAdapterValidator.repository = adapterRepository;
    }

    @Override
    public boolean supports(Class<?> type) {
        return Adapter.class.isAssignableFrom(type);
    }

    @Override
    public void validate(Object o, Errors errors) {
        MonitoringAdapter adapter = (MonitoringAdapter) o;

        //Use super class for validation
        super.validate(adapter, errors);

        //Further validation
        //TODO
    }
}
