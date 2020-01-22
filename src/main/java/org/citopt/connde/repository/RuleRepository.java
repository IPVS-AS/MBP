package org.citopt.connde.repository;

import io.swagger.annotations.*;
import org.citopt.connde.domain.rules.Rule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

/**
 * Repository for rules that were created by the user. The repository is exposed for CRUD operations
 * and provides additional methods for finding certain rules within it.
 */
@RepositoryRestResource(collectionResourceRel = "rules", path = "rules")
@Api(tags = {"Rule entities"}, description = "CRUD for rule entities")
public interface RuleRepository extends UserEntityRepository<Rule> {

    /**
     * Retrieves a rule that is of a certain name from the repository.
     *
     * @param name The name of the rule
     * @return The rule
     */
    @RestResource(exported = false)
    Rule findByName(@Param("name") String name);

    /**
     * Returns a list of all rules which use a rule trigger of a certain id.
     *
     * @param triggerId The id of the rule trigger
     * @return The list of rules
     */
    @RestResource(exported = false)
    List<Rule> findAllByTriggerId(@Param("trigger.id") String triggerId);

    @Override
    @PreAuthorize("@repositorySecurityGuard.checkPermission(#rule, 'delete')")
    @ApiOperation(value = "Deletes a rule entity", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 204, message = "Success"), @ApiResponse(code = 403, message = "Not authorized to delete the rule entity"), @ApiResponse(code = 404, message = "Rule entity not found")})
    void delete(@Param("rule") @ApiParam(value = "The ID of the rule entity to delete", example = "5c97dc2583aeb6078c5ab672", required = true) Rule rule);

    @Override
    @Query("{_id: null}") //Fail fast
    @PostAuthorize("@repositorySecurityGuard.retrieveUserEntities(returnObject, #pageable, @ruleRepository)")
    @ApiOperation(value = "Retrieves all rule entities for which the user is authorized and which fit onto a given page", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success")})
    Page<Rule> findAll(@ApiParam(value = "The page configuration", required = true) Pageable pageable);
}