package de.ipvs.as.mbp.repository;

import de.ipvs.as.mbp.domain.entity_type.SensorType;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SensorTypeRepository extends EntityTypeRepository<SensorType> {

    Optional<SensorType> findOneByName(String name);
}