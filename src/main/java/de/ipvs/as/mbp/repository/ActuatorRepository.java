package de.ipvs.as.mbp.repository;

import de.ipvs.as.mbp.domain.component.Actuator;
import de.ipvs.as.mbp.domain.device.Device;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

//@RepositoryRestResource(collectionResourceRel = "actuators", path = "actuators")
//@Api(tags = {"Actuator entities"}, description = "CRUD for actuator entities")
public interface ActuatorRepository extends ComponentRepository<Actuator> {

    Optional<Actuator> findFirstByName(@Param("name") String name);

//    @Override
//    @PreAuthorize("@repositorySecurityGuard.checkPermission(#actuator, 'delete')")
//    @ApiOperation(value = "Deletes an actuator entity", produces = "application/hal+json")
//    @ApiResponses({@ApiResponse(code = 204, message = "Success"), @ApiResponse(code = 403, message = "Not authorized to delete the actuator entity"), @ApiResponse(code = 404, message = "Actuator entity not found")})
//    void delete(@Param("actuator") @ApiParam(value = "The ID of the actuator entity to delete", example = "5c97dc2583aeb6078c5ab672", required = true) Actuator actuator);
//
//    @Override
//    @Query("{_id: null}") //Fail fast
//    @PostAuthorize("@repositorySecurityGuard.retrieveUserEntities(returnObject, #pageable, @actuatorRepository)")
//    @ApiOperation(value = "Retrieves all actuator entities for which the user is authorized and which fit onto a given page", produces = "application/hal+json")
//    @ApiResponses({@ApiResponse(code = 200, message = "Success")})
//    Page<Actuator> findAll(@ApiParam(value = "The page configuration", required = true) Pageable pageable);
}
