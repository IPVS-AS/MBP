package org.citopt.connde.repository.projection;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Projection for users which may be useful to display user data that is permitted to be shown to other users.
 */
@ApiModel(description = "Excerpt for user data accessible by other users")
public interface UserExcerpt {
    @ApiModelProperty(notes = "User ID", example = "5c8bdd11cc4ac21d30f80a7f")
    String getId();

    @ApiModelProperty(notes = "User name", example = "admin")
    String getUsername();
}
