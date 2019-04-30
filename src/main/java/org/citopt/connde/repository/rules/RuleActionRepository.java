package org.citopt.connde.repository.rules;

import org.citopt.connde.domain.rules.RuleAction;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path="rule-actions")
public interface RuleActionRepository extends MongoRepository<RuleAction, String> {

    RuleAction findByName(@Param("name") String name);
}
