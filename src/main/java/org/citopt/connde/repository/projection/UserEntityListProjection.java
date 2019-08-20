package org.citopt.connde.repository.projection;

import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Set;

/**
 * Basic list projection for user entities. To be extended by more concrete projections for the individual
 * user entity types.
 */
public interface UserEntityListProjection {
    @Value("#{target.getOwnerName()}")
    String getOwnerName();

    @Value("#{target.isApprovable()}")
    boolean getIsApprovable();

    @Value("#{target.getApprovedUsersProjection()}")
    Set<UserProjection> getApprovedUsers();
}
