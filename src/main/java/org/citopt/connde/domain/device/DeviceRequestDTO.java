package org.citopt.connde.domain.device;

import org.citopt.connde.domain.user_entity.UserEntityRequestDTO;

public class DeviceRequestDTO extends UserEntityRequestDTO {

    private String name;

    private String componentType;

    private String ipAddress;

    private String username;

    private String password;

    private String keyPairId;
    
    // - - -

	public String getName() {
		return name;
	}

	public String getComponentType() {
		return componentType;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getKeyPairId() {
		return keyPairId;
	}

}