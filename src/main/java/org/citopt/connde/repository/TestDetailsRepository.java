package org.citopt.connde.repository;

import java.util.Optional;

import org.citopt.connde.domain.testing.TestDetails;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 * Repository for test details of the tests that were created by the user with
 * the testing-tool.
 */
@RepositoryRestResource(collectionResourceRel = "test-details", path = "test-details")
public interface TestDetailsRepository extends MongoRepository<TestDetails, String> {
	
	TestDetails findByName(@Param("name") String name);

	Optional<TestDetails> findById(@Param("id") String id);
	
}