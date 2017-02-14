package org.citopt.connde.domain.type;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.citopt.connde.repository.TypeRepository;
import org.springframework.stereotype.Component;

/**
 *
 * @author rafaelkperes
 */
@Component
public class TypeValidator implements Validator {
    
    static TypeRepository repository;
    
    @Autowired
    public void setTypeRepository(TypeRepository typeRepository) {
        System.out.println("autowiring type to TypeValidator");
        TypeValidator.repository = typeRepository;
    }

    @Override
    public boolean supports(Class<?> type) {
        return Type.class.isAssignableFrom(type);
    }

    @Override
    public void validate(Object o, Errors errors) {
        Type type = (Type) o;

        validate(type, errors);
    }
    
    public void validate(Type type, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(
                errors, "name", "type.name.empty",
                "The name cannot be empty!");
        
        ValidationUtils.rejectIfEmptyOrWhitespace(
                errors, "description", "type.description.empty",
                "The description cannot be empty!");

        Type another;
        if ((another = repository.findByName(type.getName())) != null) {
            if (type.getId() == null
                    || !type.getId().equals(another.getId())) {
                errors.rejectValue("name", "type.name.duplicate",
                        "The name is already registered");
            }
        }
    }
    
}
