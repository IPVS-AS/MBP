package de.ipvs.as.mbp.domain.user;

import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.GeneratedValue;
import java.time.Instant;

/**
 * Objects of this class represent sessions of authenticated users.
 */
@Document
public class UserSession {
    @Id
    @GeneratedValue
    @ApiModelProperty(notes = "Session ID", example = "5c8f7ad66f9e3c1bacb0fa99", accessMode = ApiModelProperty.AccessMode.READ_ONLY, readOnly = true)
    private String sessionId;

    @ApiModelProperty(notes = "User ID", example = "5c8f7ad66f9e3c1bacb0fa99", accessMode = ApiModelProperty.AccessMode.READ_ONLY, readOnly = true)
    private String userId;

    @ApiModelProperty(notes = "Creation timestamp", accessMode = ApiModelProperty.AccessMode.READ_ONLY, readOnly = true)
    private Instant created;

    /**
     * Creates a new empty session object.
     */
    public UserSession() {

    }

    /**
     * Creates a new session object for a given user.
     *
     * @param user The user for which the session object is supposed to be created
     */
    public UserSession(User user) {
        //Sanity check
        if (user == null) {
            throw new IllegalArgumentException("User must not be null.");
        }

        //Extract user ID
        this.userId = user.getId();

        //Set creation timestamp
        this.created = Instant.now();
    }

    /**
     * Returns the session ID.
     *
     * @return The session ID
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Returns the ID of the user to which the session belongs.
     *
     * @return The user ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Returns the timestamp of when the session was created.
     *
     * @return The creation timestamp
     */
    public Instant getCreated() {
        return created;
    }
}
