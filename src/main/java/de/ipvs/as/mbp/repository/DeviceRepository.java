package de.ipvs.as.mbp.repository;

import java.util.List;
import java.util.Optional;

import de.ipvs.as.mbp.domain.device.Device;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceRepository extends UserEntityRepository<Device> {

	Optional<Device> findFirstByName(@Param("name") String name);
	List<Device> findAllByKeyPairId(@Param("keyPair.id") String keyPairId);

}
