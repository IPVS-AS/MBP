package de.ipvs.as.mbp.repository;

import de.ipvs.as.mbp.domain.component.Actuator;
import de.ipvs.as.mbp.domain.component.Sensor;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

//@RepositoryRestResource(collectionResourceRel = "sensors", path = "sensors")
//@Api(tags = {"Sensor entities"}, description = "CRUD for sensor entities")
public interface SensorRepository extends ComponentRepository<Sensor> {

    Optional<Sensor> findFirstByName(@Param("name") String name);
//    @Override
//    @PreAuthorize("@repositorySecurityGuard.checkPermission(#sensor, 'delete')")
//    @ApiOperation(value = "Deletes a sensor entity", produces = "application/hal+json")
//    @ApiResponses({@ApiResponse(code = 204, message = "Success"), @ApiResponse(code = 403, message = "Not authorized to delete the sensor entity"), @ApiResponse(code = 404, message = "Sensor entity not found")})
//    void delete(@Param("sensor") @ApiParam(value = "The ID of the sensor entity to delete", example = "5c97dc2583aeb6078c5ab672", required = true) Sensor sensor);
//
//    @Override
//    @Query("{_id: null}") //Fail fast
//    @PostAuthorize("@repositorySecurityGuard.retrieveUserEntities(returnObject, #pageable, @sensorRepository)")
//    @ApiOperation(value = "Retrieves all sensor entities for which the user is authorized and which fit onto a given page", produces = "application/hal+json")
//    @ApiResponses({@ApiResponse(code = 200, message = "Success")})
//    Page<Sensor> findAll(@ApiParam(value = "The page configuration", required = true) Pageable pageable);
}