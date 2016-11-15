package org.citopt.websensor.domain.validator;

import org.citopt.websensor.dao.ScriptDao;
import org.citopt.websensor.domain.Script;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
public class ScriptValidator implements Validator {

    @Autowired
    ScriptDao scriptDao;

    @Override
    public boolean supports(Class<?> clazz) {
        return Script.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object obj, Errors errors) {
        Script script = (Script) obj;

        validate(script, errors);
    }

    public void validate(Script script, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(
                errors, "name", "script.name.empty",
                "The name cannot be empty!");

        Script another;
        if ((another = scriptDao.findByName(script.getName())) != null) {
            if (script.getId() == null
                    || !script.getId().equals(another.getId())) {
                errors.rejectValue("name", "script.name.duplicate",
                        "The name is already registered");
            }
        }
    }

}
