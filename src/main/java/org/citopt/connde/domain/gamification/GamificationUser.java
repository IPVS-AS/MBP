package org.citopt.connde.domain.gamification;

import java.io.Serializable;

import javax.persistence.GeneratedValue;

import org.citopt.connde.domain.user.User;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * GamificationUser entity.
 */
@Document
@ApiModel(description = "Model for gamification user entities")
public class GamificationUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    @ApiModelProperty(notes = "Gamification User ID", example = "5c8f7ad66f9e3c1bacb0fa99", accessMode = ApiModelProperty.AccessMode.READ_ONLY, readOnly = true)
    private String id;

    @DBRef
    private User user;

    @ApiModelProperty(notes = "Accumulated points by user", example = "0", accessMode = ApiModelProperty.AccessMode.READ_ONLY, readOnly = true)
    private Integer points;

    @ApiModelProperty(notes = "User progress", example = "beginner", accessMode = ApiModelProperty.AccessMode.READ_ONLY, readOnly = true)
    private String progress;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public String getProgress() {
        return progress;
    }

    public void setProgress(String progress) {
        this.progress= progress;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        GamificationUser gamificationUser = (GamificationUser) o;

        return id.equals(gamificationUser.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "GamificationUser{" +
                "username='" + user.getUsername() + '\'' +
                ", points='" + points + '\'' +
                ", progress='" + progress + '\'' +
                "}";
    }
}
