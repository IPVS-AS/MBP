package org.citopt.websensor.dao;

import java.util.List;
import org.bson.types.ObjectId;
import org.citopt.websensor.domain.Sensor;
import org.citopt.websensor.repository.SensorRepository;
import org.citopt.websensor.web.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SensorDao {

    @Autowired
    private SensorRepository repository;

    public Sensor find(ObjectId id) throws NotFoundException {
        Sensor result = repository.findOne(id.toString());
        if (result == null) {
            throw new NotFoundException();
        }
        return result;
    }

    public List<Sensor> findAll() {
        return repository.findAll();
    }

    public Sensor insert(Sensor sensor) throws InsertFailureException {
        Sensor result = repository.insert(sensor);
        try {
            this.find(result.getId());
        } catch (NotFoundException e) {
            throw new InsertFailureException("Couldn't insert Sensor.");
        }
        return result;
    }

    public Sensor save(Sensor sensor) {
        return repository.save(sensor);
    }

    public void delete(ObjectId id) {
        repository.delete(id.toString());
    }

}
