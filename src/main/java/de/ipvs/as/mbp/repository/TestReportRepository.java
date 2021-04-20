package de.ipvs.as.mbp.repository;

import de.ipvs.as.mbp.domain.testing.TestDetails;
import de.ipvs.as.mbp.domain.testing.TestReport;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;
import java.util.Optional;

/**
 * Repository for test details of the tests that were created by the user with the testing-tool.
 */
@RepositoryRestResource(collectionResourceRel = "test-report", path = "test-report")
public interface TestReportRepository extends MongoRepository<TestReport, String>, UserEntityRepository<TestReport> {

    Optional<TestReport> findByName(@Param("name") String name);

    Optional<TestReport> findById(@Param("id") String id);

    List<TestReport> findAllByName(@Param("name") String name);
}