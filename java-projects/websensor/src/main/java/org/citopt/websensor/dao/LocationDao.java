package org.citopt.websensor.dao;

import java.util.Collection;
import org.bson.types.ObjectId;
import org.citopt.websensor.domain.Location;
import org.citopt.websensor.repository.LocationRepository;
import org.citopt.websensor.web.exception.IdNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LocationDao {
    
    @Autowired
    LocationRepository repository;

    public Location find(ObjectId id) throws IdNotFoundException {
        Location result = repository.findOne(id.toString());
        if(result == null) {
            throw new IdNotFoundException();
        }
        return result;
    }

    public Collection<Location> findAll() {
        return repository.findAll();
    }

    public Location insert(Location location) {
        return repository.insert(location);
    }

    public Location save(Location location) {
        return repository.save(location);
    }

    public void delete(ObjectId id) {
        repository.delete(id.toString());
    }
    
}
