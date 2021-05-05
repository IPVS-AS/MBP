package de.ipvs.as.mbp.domain.testing;

import de.ipvs.as.mbp.service.validation.IDeleteValidator;
import org.springframework.stereotype.Service;

@Service
public class TestDetailsDeleteValidator implements IDeleteValidator<TestDetails> {
    @Override
    public void validateDeletable(TestDetails entity) {
        String entityName = entity.getName();
    }
}
