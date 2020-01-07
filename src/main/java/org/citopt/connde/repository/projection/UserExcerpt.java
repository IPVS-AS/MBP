package org.citopt.connde.repository.projection;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.citopt.connde.domain.user.User;
import org.springframework.data.rest.core.config.Projection;

/**
 * Projection for users which may be useful to display user data that is permitted to be shown to other users.
 */
@Projection(name="list", types = User.class)
@ApiModel(description = "Excerpt for user data accessible by other users")
public interface UserExcerpt {
    @ApiModelProperty(notes = "User ID", example = "5c8bdd11cc4ac21d30f80a7f")
    String getId();

    @ApiModelProperty(notes = "User name", example = "admin")
    String getUsername();
}
