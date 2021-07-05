package de.ipvs.as.mbp.domain.device;

import de.ipvs.as.mbp.domain.user_entity.UserEntityRequestDTO;

public class DeviceDTO extends UserEntityRequestDTO {

    private String name;

    private String componentType;

    private String ipAddress;

    private String username;

    private String password;

    private String keyPairId;

    private Integer port;

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

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getKeyPairId() {
		return keyPairId;
	}

	public void setKeyPairId(String keyPairId) {
		this.keyPairId = keyPairId;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}
}