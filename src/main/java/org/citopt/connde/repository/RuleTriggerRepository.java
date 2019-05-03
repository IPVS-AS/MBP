package org.citopt.connde.repository;

import org.citopt.connde.domain.rules.RuleTrigger;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 * Repository for rule triggers that were created by the user and are supposed to be able to trigger the execution
 * of rules. The repository is exposed for CRUD operations and provides additional methods for finding certain
 * rule actions within it.
 */
@RepositoryRestResource(path="rule-triggers")
public interface RuleTriggerRepository extends MongoRepository<RuleTrigger, String> {

    /**
     * Retrieves a rule trigger that is of a certain name from the repository.
     *
     * @param name The name of the rule trigger
     * @return The rule trigger
     */
    RuleTrigger findByName(@Param("name") String name);
}
