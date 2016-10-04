package org.citopt.websensor.domain;

import java.util.Collection;
import org.bson.types.ObjectId;
import org.citopt.websensor.domain.Location;
import org.citopt.websensor.repository.LocationRepository;
import org.citopt.websensor.web.exception.IdNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MongoService {
    
    @Autowired
    private LocationRepository locationRepository;
    
    public Location findLocation(ObjectId id) throws IdNotFoundException {
        Location result = locationRepository.findOne(id.toString());
        if(result == null) {
            throw new IdNotFoundException();
        }
        return result;
    }
    
    public Collection<Location> allLocations() {
        return locationRepository.findAll();
    }
    
    public Location save(Location location) {
        return locationRepository.save(location);
    }
    
    public Location insert(Location location) {
        return locationRepository.insert(location);
    }
    
}
