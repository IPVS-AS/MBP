package de.ipvs.as.mbp.domain.testing;

import de.ipvs.as.mbp.domain.operator.parameters.ParameterInstance;
import de.ipvs.as.mbp.domain.user_entity.UserEntityRequestDTO;


import java.util.List;

public class TestDetailsDTO extends UserEntityRequestDTO {

    private String name;

    private boolean triggerRules;

    private List<String> type;

    private List<String> ruleNames;

    private boolean useNewData;

    private List<List<ParameterInstance>> config;


    // - - -

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean getTriggerRules() {
        return triggerRules;
    }

    public void setTriggerRules(boolean triggerRules) {
        this.triggerRules = triggerRules;
    }

    public List<String> getType() {
        return type;
    }

    public void setType(List<String> type) {
        this.type = type;
    }

    public List<String> getRuleNames() {
        return ruleNames;
    }

    public void setRuleNames(List<String> ruleNames) {
        this.ruleNames = ruleNames;
    }

    public boolean isUseNewData() {
        return useNewData;
    }

    public void setUseNewData(boolean useNewData) {
        this.useNewData = useNewData;
    }

    public List<List<ParameterInstance>> getConfig() {
        return config;
    }

    public void setConfig(List<List<ParameterInstance>> config) {
        this.config = config;
    }
}
