package de.ipvs.as.mbp.repository.projection;

import de.ipvs.as.mbp.domain.user.User;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.rest.core.config.Projection;

/**
 * Projection for users which may be useful to display user data that is
 * permitted to be shown to other users.
 */
@Projection(name = "list", types = User.class)
@ApiModel(description = "Excerpt for user data accessible by other users")
public interface UserExcerpt {

	@ApiModelProperty(notes = "User ID", example = "5c8bdd11cc4ac21d30f80a7f")
	String getId();

	@ApiModelProperty(notes = "User name", example = "admin")
	String getUsername();
	
}
