package org.citopt.connde.repository;

import org.citopt.connde.domain.rules.RuleAction;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 * Repository for rule actions that were created by the user and are supposed to be executed immediately after a rule
 * was triggered. The repository is exposed for CRUD operations and provides additional methods
 * for finding certain rule actions within it.
 */
@RepositoryRestResource(collectionResourceRel = "rule-actions", path = "rule-actions")
public interface RuleActionRepository extends MongoRepository<RuleAction, String> {

    /**
     * Retrieves a rule action that is of a certain name from the repository.
     *
     * @param name The name of the rule action
     * @return The rule action
     */
    RuleAction findByName(@Param("name") String name);
}
