package de.ipvs.as.mbp.domain.discovery.location.informal;

import de.ipvs.as.mbp.domain.discovery.location.LocationTemplate;
import de.ipvs.as.mbp.domain.discovery.operators.StringOperator;
import de.ipvs.as.mbp.domain.user_entity.MBPEntity;

/**
 * Objects of this class represent location templates for informal location descriptions.
 */
@MBPEntity(createValidator = InformalLocationTemplateCreateValidator.class)
public class InformalLocationTemplate extends LocationTemplate {
    private StringOperator operator;
    private String match;

    public InformalLocationTemplate() {
        super();
    }

    public StringOperator getOperator() {
        return operator;
    }

    public InformalLocationTemplate setOperator(StringOperator operator) {
        this.operator = operator;
        return this;
    }

    public String getMatch() {
        return match;
    }

    public InformalLocationTemplate setMatch(String match) {
        this.match = match;
        return this;
    }
}
