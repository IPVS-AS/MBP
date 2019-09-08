package org.citopt.connde.domain.user;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.citopt.connde.constants.Constants;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

/**
* User entity.
* @author Imeri Amil
*/
@Document
public class User implements Serializable{
	
	private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @NotNull
    @Pattern(regexp = Constants.USERNAME_REGEX)
    @Size(min = 1, max = 100)
    private String username;

    @JsonProperty(access = Access.WRITE_ONLY)
    @NotNull
    @Size(min = 1, max = 60)
    private String password;

    @Size(max = 50)
    @Field("first_name")
    private String firstName;

    @Size(max = 50)
    @Field("last_name")
    private String lastName;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        User user = (User) o;

        return username.equals(user.username);
    }

    @Override
    public int hashCode() {
        return username.hashCode();
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
