package de.ipvs.as.mbp.domain.device;

import javax.persistence.GeneratedValue;

import de.ipvs.as.mbp.service.DeviceDeleteValidator;
import de.ipvs.as.mbp.domain.access_control.ACAttributeValue;
import de.ipvs.as.mbp.domain.key_pair.KeyPair;
import de.ipvs.as.mbp.domain.user_entity.MBPEntity;
import de.ipvs.as.mbp.domain.user_entity.UserEntity;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@MBPEntity(deleteValidator = DeviceDeleteValidator.class)
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

    public Device setId(String id) {
        this.id = id;
        return this;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public Device setIpAddress(String ip) {
        this.ipAddress = ip;
        return this;
    }

    public String getName() {
        return name;
    }

    public Device setName(String name) {
        this.name = name;
        return this;
    }

    public String getComponentType() {
        return componentType;
    }

    public Device setComponentType(String componentType) {
        this.componentType = componentType;
        return this;
    }

    public String getDate() {
        return date;
    }

    public Device setDate(String date) {
        this.date = date;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public Device setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public Device setPassword(String password) {
        this.password = password;
        return this;
    }

    public KeyPair getKeyPair() {
        return keyPair;
    }

    public Device setKeyPair(KeyPair keyPair) {
        this.keyPair = keyPair;
        return this;
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

}
