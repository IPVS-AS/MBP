package org.citopt.websensor.repository;

import org.citopt.websensor.domain.Sensor;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SensorRepository extends MongoRepository<Sensor, String> {
    
}
