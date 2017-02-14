package org.citopt.connde.domain.location;

import org.citopt.connde.repository.LocationRepository;
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
public class LocationValidator implements Validator {
    
    static LocationRepository repository;
    
    @Autowired
    public void setLocationRepository(LocationRepository locationRepository) {
        System.out.println("autowiring locationRepository to LocationValidator");
        LocationValidator.repository = locationRepository;
    }

    @Override
    public boolean supports(Class<?> type) {
        return Location.class.isAssignableFrom(type);
    }

    @Override
    public void validate(Object o, Errors errors) {
        Location location = (Location) o;

        validate(location, errors);
    }

    public void validate(Location location, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(
                errors, "name", "location.name.empty",
                "The name cannot be empty!");

        Location another;
        if ((another = repository.findByName(location.getName())) != null) {
            if (location.getId() == null
                    || !location.getId().equals(another.getId())) {
                errors.rejectValue("name", "location.name.duplicate",
                        "The name is already registered");
            }
        }
    }

}
