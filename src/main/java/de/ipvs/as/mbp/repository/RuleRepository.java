package de.ipvs.as.mbp.repository;

import java.util.List;

import de.ipvs.as.mbp.domain.rules.Rule;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository for rules that were created by the user. The repository is exposed for CRUD operations
 * and provides additional methods for finding certain rules within it.
 */
public interface RuleRepository extends UserEntityRepository<Rule> {
    /**
     * Returns a list of all rules which use a rule trigger of a certain id.
     *
     * @param triggerId The id of the rule trigger
     * @return The list of rules
     */
    @Query("{ 'trigger.id' : :#{#triggerId} }")
    List<Rule> findAllByTriggerId(@Param("trigger.id") String triggerId);
    
    @Query("{ 'actions' : { $elemMatch : { 'id' : :#{#actionId} } } }")
    List<Rule> findAllByActionId(@Param("actionId") String actionId);
}