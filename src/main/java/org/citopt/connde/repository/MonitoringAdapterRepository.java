package org.citopt.connde.repository;

import io.swagger.annotations.*;
import org.citopt.connde.domain.adapter.Adapter;
import org.citopt.connde.domain.monitoring.MonitoringAdapter;
import org.citopt.connde.repository.projection.MonitoringAdapterExcerpt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Repository definition interface for monitoring adapters.
 */
@RepositoryRestResource(collectionResourceRel = "monitoring-adapters", path = "monitoring-adapters",
        excerptProjection = MonitoringAdapterExcerpt.class)
@Api(tags = {"Monitoring adapter entities"}, description = "CRUD for monitoring adapter entities")
public interface MonitoringAdapterRepository extends UserEntityRepository<MonitoringAdapter> {
    @RestResource(exported = false)
    MonitoringAdapter findByName(@Param("name") String name);

    @RestResource(exported = false)
    MonitoringAdapterExcerpt findById(@Param("id") String id);

    @Override
    @PreAuthorize("@repositorySecurityGuard.checkPermission(#adapter, 'delete')")
    @ApiOperation(value = "Deletes a monitoring adapter entity", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 204, message = "Success"), @ApiResponse(code = 403, message = "Not authorized to delete the monitoring adapter entity"), @ApiResponse(code = 404, message = "Monitoring adapter entity not found")})
    void delete(@Param("adapter") @ApiParam(value = "The ID of the monitoring adapter entity to delete", example = "5c97dc2583aeb6078c5ab672", required = true) MonitoringAdapter adapter);

    @Override
    @Query("{_id: null}") //Fail fast
    @PostAuthorize("@repositorySecurityGuard.retrieveUserEntities(returnObject, #pageable, @monitoringAdapterRepository)")
    @ApiOperation(value = "Retrieves all monitoring adapter entities for which the user is authorized and which fit onto a given page", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success")})
    Page<MonitoringAdapter> findAll(@ApiParam(value = "The page configuration", required = true) Pageable pageable);
}