package org.citopt.connde.domain.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.citopt.connde.constants.Constants;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.persistence.GeneratedValue;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY;

/**
 * User entity.
 */
@Document
@ApiModel(description = "Model for user entities")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    @ApiModelProperty(notes = "User ID", example = "5c8f7ad66f9e3c1bacb0fa99", accessMode = ApiModelProperty.AccessMode.READ_ONLY, readOnly = true)
    private String id;

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

    @Size(max = 50)
    @Field("first_name")
    @ApiModelProperty(notes = "First name of the user", example = "John")
    private String firstName;

    @Size(max = 50)
    @Field("last_name")
    @ApiModelProperty(notes = "Last name of the user", example = "Doe")
    private String lastName;

    @JsonIgnore
    private Set<Authority> authorities = new HashSet<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    //Lowercase the username before saving it in database
    public void setUsername(String username) {
        this.username = username.toLowerCase(Locale.ENGLISH);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Set<Authority> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(Set<Authority> authorities) {
        this.authorities = authorities;
    }

    @JsonProperty("isAdmin")
    @ApiModelProperty(notes = "Whether the user is an admin", accessMode = ApiModelProperty.AccessMode.READ_ONLY, readOnly = true)
    public boolean isAdmin() {
        //Create admin authority
        Authority adminAuthority = new Authority(Constants.ADMIN);

        //Check if authority available
        return authorities.contains(adminAuthority);
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
