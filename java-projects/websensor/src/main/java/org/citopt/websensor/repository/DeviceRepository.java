package org.citopt.websensor.repository;

import java.util.List;
import org.citopt.websensor.domain.Device;
import org.citopt.websensor.domain.Location;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DeviceRepository extends MongoRepository<Device, String> {
    
    public Device findByMacAddress(String macAddress);
    
    public List<Device> findByLocation(Location location);
    
}
