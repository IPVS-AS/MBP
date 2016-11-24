package org.citopt.sensmonqtt.repository;

import java.util.List;
import org.citopt.sensmonqtt.domain.Location;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 *
 * @author rafaelkperes
 */
@RepositoryRestResource
public interface LocationRepository
        extends MongoRepository<Location, String> {

    public Location findByName(@Param("name") String name);

    public List<Location> findByDescription(
            @Param("description") String description);

}
