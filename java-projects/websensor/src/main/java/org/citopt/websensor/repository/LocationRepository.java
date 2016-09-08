package org.citopt.websensor.repository;

import java.util.List;
import org.citopt.websensor.domain.Location;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LocationRepository extends MongoRepository<Location, String> {

    public Location findByName(String name);

    public List<Location> findByDescription(String description);
}
