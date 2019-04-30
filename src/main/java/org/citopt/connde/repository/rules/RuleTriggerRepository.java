package org.citopt.connde.repository.rules;

import org.citopt.connde.domain.rules.RuleTrigger;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path="rule-triggers")
public interface RuleTriggerRepository extends MongoRepository<RuleTrigger, String> {

    RuleTrigger findByName(@Param("name") String name);
}
