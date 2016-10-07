package org.citopt.websensor.dao;

import java.util.List;
import org.bson.types.ObjectId;
import org.citopt.websensor.domain.Sensor;
import org.citopt.websensor.repository.SensorRepository;
import org.citopt.websensor.web.exception.IdNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SensorDao {
    
    @Autowired
    private SensorRepository repository;
    
    public Sensor find(ObjectId id) throws IdNotFoundException {
        Sensor result = repository.findOne(id.toString());
        if(result == null) {
            throw new IdNotFoundException();
        }
        return result;
    }

    public List<Sensor> findAll() {
        return repository.findAll();
    }

    public Sensor insert(Sensor sensor) {
        return repository.insert(sensor);
    }

    public Sensor save(Sensor sensor) {
        return repository.save(sensor);
    }

    public void delete(ObjectId id) {
        repository.delete(id.toString());
    }
    
}
