package de.ipvs.as.mbp.domain.component;

import de.ipvs.as.mbp.domain.user_entity.UserEntityRequestDTO;

public class ComponentRequestDTO extends UserEntityRequestDTO {

    private String name;

    private String componentType;

    private String operatorId;

    private String deviceId;
    
    // - - -

    public String getName() {
        return name;
    }

    public String getComponentType() {
        return componentType;
    }
    
    public String getOperatorId() {
		return operatorId;
	}
    
    public String getDeviceId() {
		return deviceId;
	}

}
