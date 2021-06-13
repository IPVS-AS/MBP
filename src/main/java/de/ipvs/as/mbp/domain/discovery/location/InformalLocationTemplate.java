package de.ipvs.as.mbp.domain.discovery.location;

import de.ipvs.as.mbp.domain.discovery.operators.StringAttributeOperator;
import de.ipvs.as.mbp.domain.user_entity.MBPEntity;

/**
 * Objects of this class represent location templates for informal location descriptions.
 */
@MBPEntity(createValidator = InformalLocationTemplateCreateValidator.class)
public class InformalLocationTemplate extends LocationTemplate {
    private StringAttributeOperator operator;
    private String match;

    public InformalLocationTemplate() {
        super();
    }

    public StringAttributeOperator getOperator() {
        return operator;
    }

    public InformalLocationTemplate setOperator(StringAttributeOperator operator) {
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
