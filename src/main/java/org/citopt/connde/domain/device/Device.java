package org.citopt.connde.domain.device;

import javax.persistence.GeneratedValue;

import org.citopt.connde.domain.access_control.ACAttributeValue;
import org.citopt.connde.domain.key_pair.KeyPair;
import org.citopt.connde.domain.user_entity.MBPEntity;
import org.citopt.connde.domain.user_entity.UserEntity;
import org.citopt.connde.repository.DeviceRepository;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@MBPEntity(repository = DeviceRepository.class)
@ApiModel(description = "Model for device entities")
public class Device extends UserEntity {

    @Id
    @GeneratedValue
    @ApiModelProperty(notes = "Device ID", example = "5c8f7ad66f9e3c1bacb0fa99", accessMode = ApiModelProperty.AccessMode.READ_ONLY, readOnly = true)
    private String id;

    @ACAttributeValue
    @Indexed(unique = true)
    @ApiModelProperty(notes = "Device name", example = "My Device", required = true)
    private String name;

    @Indexed
    @ApiModelProperty(notes = "Device type", example = "Raspberry Pi", required = true)
    private String componentType;

    @ApiModelProperty(notes = "MAC address", example = "ABCABCABCABC")
    private String macAddress;

    @ApiModelProperty(notes = "Network IP address", example = "192.168.209.174", required = true)
    private String ipAddress;

    @ApiModelProperty(notes = "Creation date", example = "yyyy-MM-dd HH:mm:ss", accessMode = ApiModelProperty.AccessMode.READ_ONLY, readOnly = true)
    private String date;

    @ApiModelProperty(notes = "OS username to use on the device", example = "ubuntu", required = true)
    private String username;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ApiModelProperty(notes = "OS user password to use on the device", example = "secret")
    private String password;

    @DBRef
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ApiModelProperty(notes = "Key pair for SSH connections to the device", required = false)
    private KeyPair keyPair;

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ip) {
        this.ipAddress = ip;
    }

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

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
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

    public KeyPair getKeyPair() {
        return keyPair;
    }

    public void setKeyPair(KeyPair keyPair) {
        this.keyPair = keyPair;
    }

    @JsonProperty("usesPassword")
    @ApiModelProperty(notes = "Whether the device uses an user password", accessMode = ApiModelProperty.AccessMode.READ_ONLY, readOnly = true)
    public boolean hasPassword() {
        return (password != null) && (!password.isEmpty());
    }

    @JsonProperty("usesRSAKey")
    @ApiModelProperty(notes = "Whether the device uses a RSA key", accessMode = ApiModelProperty.AccessMode.READ_ONLY, readOnly = true)
    public boolean hasRSAKey() {
        return (keyPair != null) && (keyPair.hasPrivateKey());
    }

//    @Override
//    public UserEntityPolicy getUserEntityPolicy() {
//        return DEVICE_POLICY;
//    }
}
