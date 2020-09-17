package org.citopt.connde.domain.key_pair;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.citopt.connde.domain.user_entity.UserEntity;
import org.springframework.data.annotation.Id;

import javax.persistence.GeneratedValue;

/**
 * Document class for RSA key pairs. Key pairs are named user entities and consist out of a public and a private key.
 */
@ApiModel(description = "Model for RSA key pairs")
public class KeyPair extends UserEntity {
    @Id
    @GeneratedValue
    @ApiModelProperty(notes = "Key pair ID", example = "5c8f7ad66f9e3c1bacb0fa99", accessMode = ApiModelProperty.AccessMode.READ_ONLY, readOnly = true)
    private String id;

    @ApiModelProperty(notes = "Key pair name", example = "My Key Pair", required = true)
    private String name;

    @JsonProperty(access = JsonProperty.Access.READ_WRITE)
    @ApiModelProperty(notes = "Public RSA key for SSH connections, stored on the device", example = "", required = true)
    private String publicKey;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ApiModelProperty(notes = "Private RSA key for SSH connections", example = "-----BEGIN RSA PRIVATE KEY-----\\nMIIEowIBAAKCAQEA0enPVikCPvsyhKd317r08RPtbkMG0zRhIqJ/ZHIDV8TpRhoR\\n...\\n-----END RSA PRIVATE KEY-----", required = true)
    private String privateKey;

    /**
     * Returns the ID of the key pair.
     *
     * @return The id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the ID of the key pair.
     *
     * @param id The ID to set
     */
    public KeyPair setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Returns the name of the key pair.
     *
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the key pair.
     *
     * @param name The name to set
     */
    public KeyPair setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Returns the public key of the key pair.
     *
     * @return The key to return
     */
    public String getPublicKey() {
        return publicKey;
    }

    /**
     * Sets the public key of the key pair.
     *
     * @param publicKey The public key to set
     */
    public KeyPair setPublicKey(String publicKey) {
        this.publicKey = publicKey;
        return this;
    }

    /**
     * Returns the private key of the key pair.
     *
     * @return The private key
     */
    public String getPrivateKey() {
        return privateKey;
    }

    /**
     * Sets the private key of the key pair.
     *
     * @param privateKey The private key to set
     */
    public KeyPair setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
        return this;
    }

    /**
     * Checks whether a public key is available within this key pair.
     * @return True, if a public key is available; false otherwise
     */
    @JsonProperty("hasPublicKey")
    @ApiModelProperty(notes = "Whether the key pair has a public RSA key", accessMode = ApiModelProperty.AccessMode.READ_ONLY, readOnly = true)
    public boolean hasPublicKey() {
        return (publicKey != null) && (!publicKey.isEmpty());
    }

    /**
     * Checks whether a private key is available within this key pair.
     * @return True, if a private key is available; false otherwise
     */
    @JsonProperty("hasPrivateKey")
    @ApiModelProperty(notes = "Whether the key pair has a private RSA key", accessMode = ApiModelProperty.AccessMode.READ_ONLY, readOnly = true)
    public boolean hasPrivateKey() {
        return (privateKey != null) && (!privateKey.isEmpty());
    }
}
