package org.citopt.connde.repository;

import org.citopt.connde.domain.rules.RuleAction;

/**
 * Repository for rule actions that were created by the user and are supposed to be executed immediately after a rule
 * was triggered. The repository is exposed for CRUD operations and provides additional methods
 * for finding certain rule actions within it.
 */
//@RepositoryRestResource(collectionResourceRel = "rule-actions", path = "rule-actions", excerptProjection = RuleActionExcerpt.class)
//@Api(tags = {"Rule action entities"}, description = "CRUD for rule action entities")
public interface RuleActionRepository extends UserEntityRepository<RuleAction> {

//    /**
//     * Retrieves a rule action that is of a certain name from the repository.
//     *
//     * @param name The name of the rule action
//     * @return The rule action
//     */
//    @RestResource(exported = false)
//    RuleAction findByName(@Param("name") String name);
//
//    @Override
//    @PreAuthorize("@repositorySecurityGuard.checkPermission(#ruleAction, 'delete')")
//    @ApiOperation(value = "Deletes a rule action entity", produces = "application/hal+json")
//    @ApiResponses({@ApiResponse(code = 204, message = "Success"), @ApiResponse(code = 403, message = "Not authorized to delete the rule action entity"), @ApiResponse(code = 404, message = "Rule action entity not found")})
//    void delete(@Param("ruleAction") @ApiParam(value = "The ID of the rule action entity to delete", example = "5c97dc2583aeb6078c5ab672", required = true) RuleAction ruleAction);
//
//    @Override
//    @Query("{_id: null}") //Fail fast
//    @PostAuthorize("@repositorySecurityGuard.retrieveUserEntities(returnObject, #pageable, @ruleActionRepository)")
//    @ApiOperation(value = "Retrieves all rule action entities for which the user is authorized and which fit onto a given page", produces = "application/hal+json")
//    @ApiResponses({@ApiResponse(code = 200, message = "Success")})
//    Page<RuleAction> findAll(@ApiParam(value = "The page configuration", required = true) Pageable pageable);
}
