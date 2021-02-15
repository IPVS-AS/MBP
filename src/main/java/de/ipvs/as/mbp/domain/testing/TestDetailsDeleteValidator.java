package de.ipvs.as.mbp.domain.testing;

import de.ipvs.as.mbp.service.validation.IDeleteValidator;

public class TestDetailsDeleteValidator implements IDeleteValidator<TestDetails> {
    @Override
    public void validateDeletable(TestDetails entity) {
        String entityName = entity.getName();
    }
}
