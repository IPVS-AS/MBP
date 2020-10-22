package org.citopt.connde.domain.user;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY;

import java.io.Serializable;
import java.util.Locale;

import javax.persistence.GeneratedValue;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.citopt.connde.constants.Constants;
import org.citopt.connde.domain.access_control.ACAttributeValue;
import org.citopt.connde.domain.access_control.IACRequestingEntity;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * A user of the MBP.
 */
@Document
@ApiModel(description = "Model for user entities")
public class User implements Serializable, IACRequestingEntity {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    @ApiModelProperty(notes = "User ID", example = "5c8f7ad66f9e3c1bacb0fa99", accessMode = ApiModelProperty.AccessMode.READ_ONLY, readOnly = true)
    private String id;

    @ACAttributeValue
    @NotNull
    @Pattern(regexp = Constants.USERNAME_REGEX)
    @Size(min = 1, max = 100)
    @ApiModelProperty(notes = "User name", example = "MyUser", required = true)
    private String username;

    @JsonProperty(access = WRITE_ONLY)
    @NotNull
    @Size(min = 1, max = 60)
    @ApiModelProperty(notes = "User password", example = "secret", required = true)
    private String password;

    @ACAttributeValue
    @Size(max = 50)
    @Field("first_name")
    @ApiModelProperty(notes = "First name of the user", example = "John")
    private String firstName;

    @ACAttributeValue
    @Size(max = 50)
    @Field("last_name")
    @ApiModelProperty(notes = "Last name of the user", example = "Doe")
    private String lastName;
    
    @ACAttributeValue
    @ApiModelProperty(notes = "Indicates whether the user is an admin user.", required = true)
    private boolean isAdmin;
    
    // - - -

    public String getId() {
        return id;
    }

    public User setId(String id) {
        this.id = id;
		return this;
    }

    public String getUsername() {
        return username;
    }

    public User setUsername(String username) {
    	// Lowercase the username before saving it in database
        this.username = username.toLowerCase(Locale.ENGLISH);
		return this;
    }

    public String getPassword() {
        return password;
    }

    public User setPassword(String password) {
        this.password = password;
		return this;
    }

    public String getFirstName() {
        return firstName;
    }

    public User setFirstName(String firstName) {
        this.firstName = firstName;
		return this;
    }

    public String getLastName() {
        return lastName;
    }

    public User setLastName(String lastName) {
        this.lastName = lastName;
		return this;
    }
    
    public boolean isAdmin() {
    	return isAdmin;
    }
    
    public User setAdmin(boolean isAdmin) {
		this.isAdmin = isAdmin;
		return this;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        User user = (User) o;

        return id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                "}";
    }
}
