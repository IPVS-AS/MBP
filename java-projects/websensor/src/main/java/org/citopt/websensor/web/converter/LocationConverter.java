package org.citopt.websensor.web.converter;

import org.citopt.websensor.domain.Location;
import org.citopt.websensor.repository.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class LocationConverter implements Converter<String, Location> {
    
    private static LocationRepository locationRepository;

    @Autowired
    public void setLocationRepository(LocationRepository locationRepository) {
        System.out.println("autowiring locationRepository to locationConverter");
        LocationConverter.locationRepository = locationRepository;
    }
    
    @Override
    public Location convert(String id) {
        try {
            Location location = locationRepository.findOne(id);
            return location;
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
