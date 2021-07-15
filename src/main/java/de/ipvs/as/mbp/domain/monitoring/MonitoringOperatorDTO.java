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

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public List<Code> getRoutines() {
        return routines;
    }

    public void setRoutines(List<Code> routines) {
        this.routines = routines;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    public ArrayList<String> getDeviceTypes() {
        return deviceTypes;
    }

    public void setDeviceTypes(ArrayList<String> deviceTypes) {
        this.deviceTypes = deviceTypes;
    }
}
