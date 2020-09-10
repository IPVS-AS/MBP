package org.citopt.connde.repository.projection;

import java.util.Set;

import org.citopt.connde.domain.access_control.ACPolicy;
import org.springframework.beans.factory.annotation.Value;

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
    
    @Value("#{target.getAccessControlPolicyIds()}")
    Set<ACPolicy> getAccessControlPolicyIds();

    @Value("#{target.isOwning()}")
    boolean getIsOwning();

    @Value("#{target.isApprovable()}")
    boolean getIsApprovable();

    @Value("#{target.isDeletable()}")
    boolean getIsDeletable();
}
