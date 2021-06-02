package de.ipvs.as.mbp.domain.device;

import de.ipvs.as.mbp.domain.user_entity.UserEntityRequestDTO;

public class DeviceDTO extends UserEntityRequestDTO {

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

	public void setName(String name) {
		this.name = name;
	}

	public void setComponentType(String componentType) {
		this.componentType = componentType;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setKeyPairId(String keyPairId) {
		this.keyPairId = keyPairId;
	}

}