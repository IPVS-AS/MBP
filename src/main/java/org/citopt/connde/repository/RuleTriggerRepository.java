package org.citopt.connde.repository;

import io.swagger.annotations.*;
import org.citopt.connde.domain.device.Device;
import org.citopt.connde.domain.rules.Rule;
import org.citopt.connde.domain.rules.RuleTrigger;
import org.citopt.connde.repository.projection.RuleTriggerExcerpt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

/**
 * Repository for rule triggers that were created by the user and are supposed to be able to trigger the execution
 * of rules. The repository is exposed for CRUD operations and provides additional methods for finding certain
 * rule actions within it.
 */
@RepositoryRestResource(collectionResourceRel = "rule-triggers", path = "rule-triggers", excerptProjection = RuleTriggerExcerpt.class)
@Api(tags = {"Rule trigger entities"}, description = "CRUD for rule trigger entities")
public interface RuleTriggerRepository extends UserEntityRepository<RuleTrigger> {

    /**
     * Retrieves a rule trigger that is of a certain name from the repository.
     *
     * @param name The name of the rule trigger
     * @return The rule trigger
     */
    @RestResource(exported = false)
    RuleTrigger findByName(@Param("name") String name);

	
    @Override
    @PreAuthorize("@repositorySecurityGuard.checkPermission(#ruleTrigger, 'delete')")
    @ApiOperation(value = "Deletes a rule trigger entity", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 204, message = "Success"), @ApiResponse(code = 403, message = "Not authorized to delete the rule trigger entity"), @ApiResponse(code = 404, message = "Rule trigger entity not found")})
    void delete(@Param("ruleTrigger") @ApiParam(value = "The ID of the rule trigger entity to delete", example = "5c97dc2583aeb6078c5ab672", required = true) RuleTrigger ruleTrigger);

    @Override
    @Query("{_id: null}") //Fail fast
    @PostAuthorize("@repositorySecurityGuard.retrieveUserEntities(returnObject, #pageable, @ruleTriggerRepository)")
    @ApiOperation(value = "Retrieves all rule trigger entities for which the user is authorized and which fit onto a given page", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success")})
    Page<RuleTrigger> findAll(@ApiParam(value = "The page configuration", required = true) Pageable pageable);
}
