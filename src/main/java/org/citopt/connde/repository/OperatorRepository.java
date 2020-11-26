package org.citopt.connde.repository;

import org.citopt.connde.domain.operator.Operator;

//@RepositoryRestResource(collectionResourceRel = "adapters", path = "adapters", excerptProjection = AdapterExcerpt.class)
//@Api(tags = {"Adapter entities"}, description = "CRUD for adapter entities")
public interface OperatorRepository extends UserEntityRepository<Operator> {
//    @RestResource(exported = false)
//    Adapter findByName(@Param("name") String name);
//
//    @Override
//    @PreAuthorize("@repositorySecurityGuard.checkPermission(#adapter, 'delete')")
//    @ApiOperation(value = "Deletes an adapter entity", produces = "application/hal+json")
//    @ApiResponses({@ApiResponse(code = 204, message = "Success"), @ApiResponse(code = 403, message = "Not authorized to delete the adapter entity"), @ApiResponse(code = 404, message = "Adapter entity not found")})
//    void delete(@Param("adapter") @ApiParam(value = "The ID of the adapter entity to delete", example = "5c97dc2583aeb6078c5ab672", required = true) Adapter adapter);
//
//    @Override
//    @Query("{_id: null}") //Fail fast
//    @PostAuthorize("@repositorySecurityGuard.retrieveUserEntities(returnObject, #pageable, @adapterRepository)")
//    @ApiOperation(value = "Retrieves all adapter entities for which the user is authorized and which fit onto a given page", produces = "application/hal+json")
//    @ApiResponses({@ApiResponse(code = 200, message = "Success")})
//    Page<Adapter> findAll(@ApiParam(value = "The page configuration", required = true) Pageable pageable);
}
