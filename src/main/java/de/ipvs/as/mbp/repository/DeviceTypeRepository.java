package de.ipvs.as.mbp.repository;

import de.ipvs.as.mbp.domain.entity_type.DeviceType;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeviceTypeRepository extends EntityTypeRepository<DeviceType> {

    Optional<DeviceType> findOneByName(String name);
}