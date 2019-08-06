package org.citopt.connde.repository;

import org.citopt.connde.domain.device.Device;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

public interface DeviceRepository extends UserEntityRepository<Device> {

    Device findByName(@Param("name") String name);
}
