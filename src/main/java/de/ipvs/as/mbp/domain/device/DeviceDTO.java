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

	public Integer getPort() {
		return port;
	}
}