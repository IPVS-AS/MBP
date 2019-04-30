package org.citopt.connde.repository.rules;

import org.citopt.connde.domain.rules.Rule;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource
public interface RuleRepository extends MongoRepository<Rule, String> {

    Rule findByName(@Param("name") String name);

    List<Rule> findAllByActionId(@Param("action.id") String actionId);

    List<Rule> findAllByTriggerId(@Param("trigger.id") String triggerId);
}
