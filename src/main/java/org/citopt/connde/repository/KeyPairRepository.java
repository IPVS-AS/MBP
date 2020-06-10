package org.citopt.connde.repository;

import io.swagger.annotations.*;
import org.citopt.connde.domain.key_pair.KeyPair;
import org.citopt.connde.repository.projection.KeyPairExcerpt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;

@RepositoryRestResource(collectionResourceRel = "key-pairs", path = "key-pairs", excerptProjection = KeyPairExcerpt.class)
@Api(tags = {"Key pair entities"}, description = "CRUD for key pair entities")
public interface KeyPairRepository extends UserEntityRepository<KeyPair> {
    @RestResource(exported = false)
    KeyPair findByName(@Param("name") String name);

    @Override
    @PreAuthorize("@repositorySecurityGuard.checkPermission(#keyPair, 'delete')")
    @ApiOperation(value = "Deletes a key pair entity", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 204, message = "Success"), @ApiResponse(code = 403, message = "Not authorized to delete the key pair entity"), @ApiResponse(code = 404, message = "Key pair entity not found")})
    void delete(@Param("keyPair") @ApiParam(value = "The ID of the key pair entity to delete", example = "5c97dc2583aeb6078c5ab672", required = true) KeyPair keyPair);

    @Override
    @Query("{_id: null}") //Fail fast
    @PostAuthorize("@repositorySecurityGuard.retrieveUserEntities(returnObject, #pageable, @keyPairRepository)")
    @ApiOperation(value = "Retrieves all key pair entities for which the user is authorized and which fit onto a given page", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success")})
    Page<KeyPair> findAll(@ApiParam(value = "The page configuration", required = true) Pageable pageable);
}
