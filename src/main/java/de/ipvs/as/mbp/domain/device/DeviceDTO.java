package de.ipvs.as.mbp.domain.device;

import javax.enterprise.inject.Default;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import de.ipvs.as.mbp.domain.user_entity.UserEntityRequestDTO;
import de.ipvs.as.mbp.util.validation.OptionalPort;

public class DeviceDTO extends UserEntityRequestDTO {

    private String name;

    private String componentType;

    private String ipAddress;

    private String username;

    private String password;

    private String keyPairId;

    @OptionalPort
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