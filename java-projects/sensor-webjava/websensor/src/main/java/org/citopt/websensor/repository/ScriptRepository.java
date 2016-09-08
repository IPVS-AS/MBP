package org.citopt.websensor.repository;

import org.citopt.websensor.domain.Location;
import org.citopt.websensor.domain.Script;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ScriptRepository extends MongoRepository<Script, String> {

    public Location findByName(String name);
}
