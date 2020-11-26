package org.citopt.connde.repository;

import java.util.List;

import org.citopt.connde.domain.device.Device;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceRepository extends UserEntityRepository<Device> {

	List<Device> findAllByKeyPairId(@Param("keyPair.id") String keyPairId);

}
