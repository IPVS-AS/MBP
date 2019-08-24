package org.citopt.connde.repository.projection;

import io.swagger.annotations.ApiModelProperty;
import org.springframework.beans.factory.annotation.Value;

import java.util.Set;

/**
 * Basic list projection for user entities. To be extended by more concrete projections for the individual
 * user entity types.
 */
public interface UserEntityExcerpt {
    @Value("#{target.getOwnerName()}")
    String getOwnerName();

    @Value("#{target.isApprovable()}")
    boolean getIsApprovable();

    @Value("#{target.getApprovedUsersProjection()}")
    Set<UserExcerpt> getApprovedUsers();
}
