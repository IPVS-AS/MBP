package org.citopt.connde.repository;

import io.swagger.annotations.*;
import org.citopt.connde.domain.rules.RuleAction;
import org.citopt.connde.repository.projection.RuleActionExcerpt;
import org.citopt.connde.service.rules.execution.RuleActionExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Repository for rule actions that were created by the user and are supposed to be executed immediately after a rule
 * was triggered. The repository is exposed for CRUD operations and provides additional methods
 * for finding certain rule actions within it.
 */
@RepositoryRestResource(collectionResourceRel = "rule-actions", path = "rule-actions", excerptProjection = RuleActionExcerpt.class)
@Api(tags = {"Rule action entities"}, description = "CRUD for rule action entities")
public interface RuleActionRepository extends UserEntityRepository<RuleAction> {

    /**
     * Retrieves a rule action that is of a certain name from the repository.
     *
     * @param name The name of the rule action
     * @return The rule action
     */
    @RestResource(exported = false)
    RuleAction findByName(@Param("name") String name);

    @Override
    @PreAuthorize("@repositorySecurityGuard.checkPermission(#ruleAction, 'delete')")
    @ApiOperation(value = "Deletes a rule action entity", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 204, message = "Success"), @ApiResponse(code = 403, message = "Not authorized to delete the rule action entity"), @ApiResponse(code = 404, message = "Rule action entity not found")})
    void delete(@Param("ruleAction") @ApiParam(value = "The ID of the rule action entity to delete", example = "5c97dc2583aeb6078c5ab672", required = true) RuleAction ruleAction);

    @Override
    @Query("{_id: null}") //Fail fast
    @PostAuthorize("@repositorySecurityGuard.retrieveUserEntities(returnObject, #pageable, @ruleActionRepository)")
    @ApiOperation(value = "Retrieves all rule action entities for which the user is authorized and which fit onto a given page", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success")})
    Page<RuleAction> findAll(@ApiParam(value = "The page configuration", required = true) Pageable pageable);
}
