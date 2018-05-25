package org.citopt.connde.repository;

import java.util.List;
import org.citopt.connde.domain.device.Device;
import org.citopt.connde.repository.projection.DeviceListProjection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 *
 * @author rafaelkperes
 */
@RepositoryRestResource(excerptProjection = DeviceListProjection.class)
public interface DeviceRepository
        extends MongoRepository<Device, String> {

    public Device findByName(@Param("name") String name);

    public Device findByMacAddress(@Param("macAddress") String mac);

    public List<Device> findAllByMacAddress(@Param("macAddress") String mac, Pageable pageable);

    public List<Device> findAllByNameOrMacAddress(@Param("name") String name, @Param("macAddress") String macAddress, Pageable pageable);

}
