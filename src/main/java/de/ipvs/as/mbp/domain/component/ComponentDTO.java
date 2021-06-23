package de.ipvs.as.mbp.domain.component;

import de.ipvs.as.mbp.domain.user_entity.UserEntityRequestDTO;

public class ComponentDTO extends UserEntityRequestDTO {

    private String name;

    private String componentType;

    private String operatorId;

    private String deviceId;

    // - - -

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComponentType() {
        return componentType;
    }

    public void setComponentType(String componentType) {
        this.componentType = componentType;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
