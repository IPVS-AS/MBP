package org.citopt.websensor.dao;

import java.util.List;
import org.bson.types.ObjectId;
import org.citopt.websensor.domain.Device;
import org.citopt.websensor.repository.DeviceRepository;
import org.citopt.websensor.web.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DeviceDao {
    
    @Autowired
    private DeviceRepository repository;
    
    public Device find(ObjectId id) throws NotFoundException {
        Device result = repository.findOne(id.toString());
        if(result == null) {
            throw new NotFoundException();
        }
        return result;
    }

    public List<Device> findAll() {
        return repository.findAll();
    }

    public Device insert(Device device) {
        return repository.insert(device);
    }

    public Device save(Device device) {
        return repository.save(device);
    }

    public void delete(ObjectId id) {
        repository.delete(id.toString());
    }
}
