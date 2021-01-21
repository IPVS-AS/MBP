package de.ipvs.as.mbp.domain.rules;

import de.ipvs.as.mbp.domain.user_entity.UserEntityRequestDTO;

import java.util.List;

/**
 * DTO for rules.
 */
public class RuleDTO extends UserEntityRequestDTO {

    private String name;

    private String trigger;

    private List<String> actions;

    public String getName() {
        return name;
    }

    public String getTrigger() {
        return trigger;
    }

    public List<String> getActions() {
        return actions;
    }
}
