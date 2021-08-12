package de.ipvs.as.mbp.repository;

import de.ipvs.as.mbp.domain.entity_type.ActuatorType;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ActuatorTypeRepository extends EntityTypeRepository<ActuatorType> {

    Optional<ActuatorType> findOneByName(String name);
}