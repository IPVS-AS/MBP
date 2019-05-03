package org.citopt.connde.repository;

import org.citopt.connde.domain.rules.Rule;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

/**
 * Repository for rules that were created by the user. The repository is exposed for CRUD operations
 * and provides additional methods for finding certain rules within it.
 */
@RepositoryRestResource
public interface RuleRepository extends MongoRepository<Rule, String> {

    /**
     * Retrieves a rule that is of a certain name from the repository.
     *
     * @param name The name of the rule
     * @return The rule
     */
    Rule findByName(@Param("name") String name);

    /**
     * Returns a list of all rules which use a rule action of a certain id.
     *
     * @param actionId The id of the rule action
     * @return The list of rules
     */
    List<Rule> findAllByActionId(@Param("action.id") String actionId);

    /**
     * Returns a list of all rules which use a rule trigger of a certain id.
     *
     * @param triggerId The id of the rule trigger
     * @return The list of rules
     */
    List<Rule> findAllByTriggerId(@Param("trigger.id") String triggerId);
}