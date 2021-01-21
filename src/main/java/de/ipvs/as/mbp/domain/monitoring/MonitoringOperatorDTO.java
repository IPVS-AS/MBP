package de.ipvs.as.mbp.domain.monitoring;

import de.ipvs.as.mbp.domain.operator.Code;
import de.ipvs.as.mbp.domain.operator.parameters.Parameter;
import de.ipvs.as.mbp.domain.user_entity.UserEntityRequestDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for monitoring operators.
 */
public class MonitoringOperatorDTO extends UserEntityRequestDTO {

    private String name;

    private String description;

    private String unit;

    private List<Code> routines;

    private List<Parameter> parameters;

    private ArrayList<String> deviceTypes;

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getUnit() {
        return unit;
    }

    public List<Code> getRoutines() {
        return routines;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public ArrayList<String> getDeviceTypes() {
        return deviceTypes;
    }
}
