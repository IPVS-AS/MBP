package org.citopt.connde.repository.projection;

import org.springframework.beans.factory.annotation.Value;

import java.util.Set;

/**
 * Basic list projection for user entities. To be extended by more concrete projections for the individual
 * user entity types.
 */
public interface UserEntityExcerpt {
    @Value("#{target.wasModelled()}")
    boolean getWasModelled();

    @Value("#{target.getOwnerName()}")
    String getOwnerName();

    @Value("#{target.getApprovedUsersProjection()}")
    Set<UserExcerpt> getApprovedUsers();

    @Value("#{target.isOwning()}")
    boolean getIsOwning();

    @Value("#{target.isApprovable()}")
    boolean getIsApprovable();

    @Value("#{target.isDeletable()}")
    boolean getIsDeletable();
}
