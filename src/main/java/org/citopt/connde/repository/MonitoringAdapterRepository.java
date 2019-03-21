package org.citopt.connde.repository;

import org.citopt.connde.domain.monitoring.MonitoringAdapter;
import org.citopt.connde.repository.projection.MonitoringAdapterListProjection;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 * Repository definition interface for monitoring adapters.
 *
 * @author Jan
 */
@RepositoryRestResource(collectionResourceRel = "monitoring-adapters", path = "monitoring-adapters",
        excerptProjection = MonitoringAdapterListProjection.class)
public interface MonitoringAdapterRepository extends MongoRepository<MonitoringAdapter, String> {
    MonitoringAdapter findByName(@Param("name") String name);
}