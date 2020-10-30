package org.citopt.connde.domain.component;

import org.citopt.connde.domain.user_entity.UserEntityRequestDTO;

public class ComponentRequestDTO extends UserEntityRequestDTO {

    private String name;

    private String componentType;

    private String adapterId;

    private String deviceId;
    
    // - - -

    public String getName() {
        return name;
    }

    public String getComponentType() {
        return componentType;
    }
    
    public String getAdapterId() {
		return adapterId;
	}
    
    public String getDeviceId() {
		return deviceId;
	}

}
