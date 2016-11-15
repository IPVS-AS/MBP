package org.citopt.websensor.web.file;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class FileValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return FileBucket.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object obj, Errors errors) {
        FileBucket file = (FileBucket) obj;
        
        errors.rejectValue("file", "missing.file");
        if (file.getFile() == null || file.getFile().getSize() == 0) {
            errors.rejectValue("file", "missing.file");
        }
    }
}
