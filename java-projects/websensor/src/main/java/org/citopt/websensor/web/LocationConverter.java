package org.citopt.websensor.web;

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
        System.out.println("autowiring locRep to locConv");
        LocationConverter.locationRepository = locationRepository;
    }
    
    @Override
    public Location convert(String id) {
        System.out.println("converting:" + id);
        try {
            System.out.println(locationRepository);
            Location location = locationRepository.findOne(id);
            System.out.println("result:" + location);
            return location;
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("result: null");
            return null;
        }
    }
}
