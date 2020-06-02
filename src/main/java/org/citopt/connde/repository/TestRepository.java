package org.citopt.connde.repository;

import org.citopt.connde.domain.rules.Rule;
import org.citopt.connde.domain.testing.Testing;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

/**
 * Repository for the values that trigger rules of the tested application trough the simulation.
 */
@RepositoryRestResource(collectionResourceRel = "testing", path = "testing")
public interface TestRepository extends MongoRepository<Testing, String> {
    List<Testing> findAllByTriggerId(@Param("trigger.id") String triggerId);
}