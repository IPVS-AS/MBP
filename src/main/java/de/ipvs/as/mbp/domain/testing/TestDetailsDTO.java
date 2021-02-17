package de.ipvs.as.mbp.domain.testing;

import de.ipvs.as.mbp.domain.operator.parameters.ParameterInstance;


import java.util.List;

public class TestDetailsDTO {

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

    public boolean getTriggerRules() {
        return triggerRules;
    }

    public List<String> getType() {
        return type;
    }

    public List<String> getRuleNames() {
        return ruleNames;
    }

    public boolean isUseNewData() {
        return useNewData;
    }


    public List<List<ParameterInstance>> getConfig() {
        return config;
    }
}
